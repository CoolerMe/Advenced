# Advenced

#### 这个是正式开课前的 week2 的作业

使用的 MySQL 数据库，连接的远程数据库，功能简单

利用 ServletContextLister 启动的回调方法，获取到 context.xml 里面配置的 DataSource，并保存起来
然后在Servlet doGet 方法里面再重复使用 DataSource,避免每次都需要查找
