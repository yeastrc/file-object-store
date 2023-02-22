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
<div >
  <a href="admin/processScanFileThread_Status?<%= AdminPageConstants.ADMIN_KEY_QUERY_STRING_PARAMETER_NAME %>=${ adminKey }" 
  	target="_blank">Process Scan Thread: Status</a>
</div>
<div >
  <a href="admin/processScanFileThread_Stop?<%= AdminPageConstants.ADMIN_KEY_QUERY_STRING_PARAMETER_NAME %>=${ adminKey }" 
  	target="_blank">Process Scan Thread: Stop after processing current file</a>
</div>
<div >
  <a href="admin/processScanFileThread_Start?<%= AdminPageConstants.ADMIN_KEY_QUERY_STRING_PARAMETER_NAME %>=${ adminKey }" 
  	target="_blank">Process Scan Thread: Start or Clear Stop after processing current file</a>
</div>

</body>

</html>