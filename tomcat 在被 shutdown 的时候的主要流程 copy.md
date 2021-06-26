* 在 `sh shutdown.sh` 被调用后，实际上调用的是 `catalina.sh`  + `stop` 参数
  ```bash
  exec "$PRGDIR"/"$EXECUTABLE" stop "$@"''
  ```

* 再然后调用的是 `catalina.sh` 的如下命令,实际上调用的 `org.apache.catalina.startup.Bootstrap` 的 `org.apache.catalina.startup.Bootstrap#main` 方法 + `start` 参数

  ```bash
  eval "\"$_RUNJAVA\"" $LOGGING_MANAGER "$JAVA_OPTS" \
    -D$ENDORSED_PROP="\"$JAVA_ENDORSED_DIRS\"" \
    -classpath "\"$CLASSPATH\"" \
    -Dcatalina.base="\"$CATALINA_BASE\"" \
    -Dcatalina.home="\"$CATALINA_HOME\"" \
    -Djava.io.tmpdir="\"$CATALINA_TMPDIR\"" \
    org.apache.catalina.startup.Bootstrap "$@" stop
  ```
  
* 如下代码是   `org.apache.catalina.startup.Bootstrap#main` 方法 实际上调用的是 `org.apache.catalina.startup.Catalina` 的 `org.apache.catalina.startup.Catalina#stopServer(java.lang.String[])` 方法

```java
  public static void main(String args[]) {
  
      synchronized (daemonLock) {
  			...略
  
      try {
          String command = "start";
          if (args.length > 0) {
              command = args[args.length - 1];
          }
  
          } else if (command.equals("stop")) {
        			// 此处就是被调用的方法
              daemon.stopServer(args);
          } else if (command.equals("configtest")) {
             ...略
      } catch (Throwable t) {
        ...略
      }
  }
```

* 实际调用 `org.apache.catalina.core.StandardServer` 的 `stop`(), `destory`()方法

  ```java
  public void stopServer(String[] arguments) {
      ...略
      Server s = getServer();
      if (s == null) {
          ...略
      } else {
          // Server object already present. Must be running as a service
          try {
            	// 因为此时 StandardServer 已经不为 null，所以stop()
              s.stop();
              s.destroy();
          } catch (LifecycleException e) {
              log.error("Catalina.stop: ", e);
          }
          return;
      }
  		...略
  }
  ```

* 在 `StandardServer` 继承了`org.apache.catalina.util.LifecycleBase`  会在 `stop()` 方法中调用自身的`org.apache.catalina.core.StandardServer#stopInternal` 方法

  ```java
      @Override
      protected void stopInternal() throws LifecycleException {
  				// 设置自身状态为 STOPPING
          setState(LifecycleState.STOPPING);
        	// 触发 CONFIGURE_STOP_EVENT 事件，
        	// 调用注册的LifecycleListener 的 lifecycleEvent 方法
          fireLifecycleEvent(CONFIGURE_STOP_EVENT, null);
  
          // Stop our defined Services
          for (Service service : services) {
            	// 停止 Server 里面的 StandardServer
              service.stop();
          }
  				// 停止 JNDI 服务
          globalNamingResources.stop();
  				// 停止监听 8005 端口
          stopAwait();
      }
          
      @Override
      protected void destroyInternal() throws LifecycleException {
          // Destroy our defined Services
          for (Service service : services) {
            	// 调用 Service 的 destory() 方法
              service.destroy();
          }
  				// 销毁 JNDI 服务
          globalNamingResources.destroy();
  				// 注销 JMX
          unregister(onameMBeanFactory);
  				
          unregister(onameStringCache);
  				// 父类的 destroyInternal() 方法
          super.destroyInternal();
      }
  ```

* 目前关注的是 `org.apache.catalina.core.StandardService` 的`stopInternal`方法

  ```java
  @Override
  protected void stopInternal() throws LifecycleException {
  
      // Pause connectors first
      // 先暂停 Connector 从调试信息来看，是 HTTP1.1 的 Connector
      synchronized (connectorsLock) {
          for (Connector connector: connectors) {
              try {
                	// 调用的org.apache.tomcat.util.net.AbstractEndpoint#pause方法
               		// 使用虚假连接解锁服务器 Socket 连接的是 8080 端口
                  connector.pause();
              } catch (Exception e) {
                  log.error(sm.getString(
                          "standardService.connector.pauseFailed",
                          connector), e);
              }
              // Close server socket if bound on start
              // Note: test is in AbstractEndpoint
            	// 关闭 Http11NioProtocol 
              // 的 org.apache.tomcat.util.net.AbstractEndpoint里的绑定的 ServerSocket
            	// 实际调用的 org.apache.tomcat.util.net.NioEndpoint#doCloseServerSocket 方法
              connector.getProtocolHandler().closeServerSocketGraceful();
          }
      }
  
      ...略
      // 设置状态
      setState(LifecycleState.STOPPING);
  
      // Stop our defined Container second
      if (engine != null) {
          synchronized (engine) {
            	// 停止 Servlet 引擎
              engine.stop();
          }
      }
  
      // Now stop the connectors
      synchronized (connectorsLock) {
          for (Connector connector: connectors) {
              ...略
      						//org.apache.tomcat.util.net.NioEndpoint#stopInternal方法
            			// 里面释放资源,关闭线程池
                	// 关闭 ServerSocket()[没有关闭就再次关闭]
                  connector.stop();
              ...略
          }
      }
  
      // If the Server failed to start, the mapperListener won't have been
      // started
          // 处理 mapperListener,从Service 的 Engine中递归移除 Container 里面的自身
    			// 即org.apache.catalina.Container#removeContainerListener
    			// org.apache.catalina.Lifecycle#removeLifecycleListener
          mapperListener.stop();
      }
  
      synchronized (executors) {
          for (Executor executor: executors) {
            	// 停止任务 
              executor.stop();
          }
      }
  }
  ```

* 现在关注`org.apache.catalina.core.StandardEngine` 的 `stop`() 方法 最终调用的`org.apache.catalina.core.ContainerBase#stopInternal` 

```java
@Override
protected synchronized void stopInternal() throws LifecycleException {

    // Stop our thread
  	// 停止后台线程
    threadStop();

    setState(LifecycleState.STOPPING);

    // Stop the Valves in our pipeline (including the basic), if any
    if (pipeline instanceof Lifecycle &&
            ((Lifecycle) pipeline).getState().isAvailable()) {
        ((Lifecycle) pipeline).stop();
    }

    // Stop our child containers, if any
    Container children[] = findChildren();
    List<Future<Void>> results = new ArrayList<>();
    for (Container child : children) {
      	// 让Child Container 也 stop(),这时会异步停止所有的子容器
        results.add(startStopExecutor.submit(new StopChild(child)));
    }

    boolean fail = false;
    for (Future<Void> result : results) {
        try {
          	// 阻塞获取结果
            result.get();
        } catch (Exception e) {
            log.error(sm.getString("containerBase.threadedStopFailed"), e);
            fail = true;
        }
    }
  	// 检查子容器是否都停止了
    if (fail) {
        throw new LifecycleException(
                sm.getString("containerBase.threadedStopFailed"));
    }

    // Stop our subordinate components, if any
    Realm realm = getRealmInternal();
    if (realm instanceof Lifecycle) {
        ((Lifecycle) realm).stop();
    }
  	// 停止 Cluster，目前为空
    Cluster cluster = getClusterInternal();
    if (cluster instanceof Lifecycle) {
        ((Lifecycle) cluster).stop();
    }
}
```

* `StandarEngine` 最后是调用了`org.apache.catalina.core.StandardContext#stopInternal` 方法

  ```java
  @Override
  protected synchronized void stopInternal() throws LifecycleException {
  
    	// 发送 stoping Notification
      if (this.getObjectName() != null) {
          Notification notification =
              new Notification("j2ee.state.stopping", this.getObjectName(),
                               sequenceNumber.getAndIncrement());
          broadcaster.sendNotification(notification);
      }
  
  		...略
      setState(LifecycleState.STOPPING);
  
      // Binding thread
      ClassLoader oldCCL = bindThread();
  
      try {
          // Stop our child containers, if any
          final Container[] children = findChildren();
  
          // Stop ContainerBackgroundProcessor thread
          threadStop();
  
          for (Container child : children) {
              child.stop();
          }
  
          // Stop our filters
        	// 停止 Filter
          filterStop();
  				// 停止 Session 持久化的 Mananger
          Manager manager = getManager();
          if (manager instanceof Lifecycle && ((Lifecycle) manager).getState().isAvailable()) {
              ((Lifecycle) manager).stop();
          }
  
          // 停止所有的监听器
        	// 先停止ServletContextListener/HttpSessionListener Context 生命周期监听器
        	// 再停止ServletContextAttributeListener 等时间监听器
          listenerStop();
  
          // Finalize our character set mapper
          setCharsetMapper(null);
  
          // stop JNDI 服务
          if (namingResources != null) {
              namingResources.stop();
          }
  
          fireLifecycleEvent(Lifecycle.CONFIGURE_STOP_EVENT, null);
  
          // 停止 Pipeline 的 valve
          if (pipeline instanceof Lifecycle &&
                  ((Lifecycle) pipeline).getState().isAvailable()) {
              ((Lifecycle) pipeline).stop();
          }
  
          // 清除Context 属性
          if (context != null) {
              context.clearAttributes();
          }
  
          Realm realm = getRealmInternal();
          if (realm instanceof Lifecycle) {
              ((Lifecycle) realm).stop();
          }
          Loader loader = getLoader();
          if (loader instanceof Lifecycle) {
              ClassLoader classLoader = loader.getClassLoader();
              ((Lifecycle) loader).stop();
              if (classLoader != null) {
                  InstanceManagerBindings.unbind(classLoader);
              }
          }
  
          // Stop resources
          resourcesStop();
  
      } finally {
  
          // Unbinding thread
        	// 将当前线程和 Context 解绑
          unbindThread(oldCCL);
  
      }
  
      // ...发送 stop notification 略
  
      // Reset application context
      context = null;
  
      // This object will no longer be visible or used.
      try {
          resetContext();
      } catch( Exception ex ) {
          log.error( "Error resetting context " + this + " " + ex, ex );
      }
  
      //reset the instance manager
      setInstanceManager(null);
  
      ...略
  
  }
  ```

* 使用 `Socket`连接 8005 端口，发送 `SHUTDOWN` 就能停止 `Tomcat`

  ```java
  public class ShutdownTask {
  
      public static void main(String[] args) throws IOException {
          Socket socket = new Socket();
          SocketAddress address = new InetSocketAddress("127.0.0.1", 8005);
          socket.connect(address);
  
          String content = "SHUTDOWN";
          ByteBuffer buffer = ByteBuffer.allocate(content.getBytes(StandardCharsets.UTF_8).length);
          buffer.put(content.getBytes(StandardCharsets.UTF_8));
          socket.getOutputStream().write(buffer.array());
  
      }
  }
  ```