<%@page contentType="text/html"
            pageEncoding="utf-8"%>
<%@ page import="java.util.Map,
				java.util.HashMap,
				org.json.JSONObject,
				org.apache.sling.api.resource.ModifiableValueMap" %>            
            <%@include file="/libs/foundation/global.jsp"%>
<%

	String serverUrl = properties.get("serverUrl","not set");
	String sessionToken = properties.get("sessionToken","not set");
	String taxonomyId = properties.get("taxonomyId","not set");
	//String secretKey = properties.get("secretKey",null);
	//String baseUrl = properties.get("serverUrl",null);
	//String uuid = null;
	
	//if (clientName != null && clientId == null){
	//	ContentHubUtil chUtil = new ContentHubUtil(apiKey, secretKey, baseUrl);
	//	uuid = chUtil.registerClient(clientName);

	//	ModifiableValueMap map = resource.adaptTo(ModifiableValueMap.class);
	//	map.put("clientId", uuid);
	//	resource.getResourceResolver().commit();
	//}
	//if (uuid != null){
	///	clientId = uuid;
	//}
%>            


<div>
    <h3>Wordmap Settings</h3>   
    <ul>
        <li><div class="li-bullet"><strong>Server Url: </strong><br><%= serverUrl %></div></li>
        <li><div class="li-bullet"><strong>Session Token: </strong><br><%= sessionToken %></div></li>
        <li><div class="li-bullet"><strong>Taxonomy Id: </strong><br><%= taxonomyId %></div></li>
    </ul>
</div>

