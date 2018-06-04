<%@page contentType="text/html"
            pageEncoding="utf-8"%><%
%><%@include file="/libs/foundation/global.jsp"%><div>
 
<div>
    <h3>Wordmap Services Settings</h3>   
    <ul>
        <li><div class="li-bullet"><strong>accountID: </strong><%= xssAPI.encodeForHTML(properties.get("accountID", "")) %></div></li>
    </ul>
</div>