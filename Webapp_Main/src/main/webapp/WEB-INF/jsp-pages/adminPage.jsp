<%@page import="org.yeastrc.file_object_storage.web_app.constants_enums.AdminPageConstants"%>
<html>
<head>
 <title>Admin Page</title>
</head>
<body>

<div >
  <a href="admin/reloadConfig?<%= AdminPageConstants.ADMIN_KEY_QUERY_STRING_PARAMETER_NAME %>=${ adminKey }" 
  	target="_blank">Reload Config</a>
</div>

</body>

</html>