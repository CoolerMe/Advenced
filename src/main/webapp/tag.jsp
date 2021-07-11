<%--
  Created by IntelliJ IDEA.
  User: coolme
  Date: 2021/7/6
  Time: 9:15 下午
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="/WEB-INF/cache.tld" prefix="cache" %>
<html>
<head>
    <title>这个是标题哈</title>
</head>
<body>


这个是内容.我在里面设置缓存相关的属性
<cache:cacheTag expires="-1" cache_control="no-cache" pragma="no-cache"/>

</body>
</html>
