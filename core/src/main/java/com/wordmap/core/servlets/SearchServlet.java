package com.wordmap.core.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.wordmap.core.util.ServiceUtil;

@SuppressWarnings("serial")
@SlingServlet(paths = "/bin/wordmap/search")
public class SearchServlet  extends SlingSafeMethodsServlet  {
	
	private static final Logger LOG = LoggerFactory.getLogger(SearchServlet.class);	
	
	@Reference
    private QueryBuilder builder;
	
	@Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {	
		
		String queryString = req.getParameter("queryString");
		String[] queries = queryString.split(" ");
		
		LOG.debug("query: " + queryString);
		
		String proto = req.getScheme();
    	String host = req.getServerName();
    	int port = req.getServerPort();
    	String serverUrl = proto.toLowerCase() + "://" + host + ":" + port;
		
    	boolean success = true;
    	int total = 0;
    	int results = 0;
    	JSONArray hits = new JSONArray();	
    	Map<String,JSONObject> hitMap = new HashMap<String,JSONObject>();
    	
		try {

			
			for (String q : queries) {

				String pageTitle, pagePath;
				Map<String,String> params = new HashMap<String,String>();
				params.put("path", "/content/dam/wordmap");
				//params.put("type" , "dam:Asset");
				params.put("fulltext" , q);
				params.put("orderby" , "@jcr:score");
				params.put("1_property", "content-type");	
				params.put("1_property.operation", "like");
				
				ResourceResolver resourceResolver = req.getResourceResolver();
				//ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
				Session session = resourceResolver.adaptTo(Session.class);		
				
				Query query = builder.createQuery(PredicateGroup.create(params), session);
				//for pagination
				query.setStart(0); // same as params["p.offset"] = startPage.toString()
				query.setHitsPerPage(100); // same as params["p.limit"] = offset.toString()
	
				SearchResult searchResult = query.getResult();
				Long totalHits = searchResult.getTotalMatches();
				
				for (Hit hit : searchResult.getHits()) {
				 
				   // Asset page = hit.getResource().adaptTo(Asset.class);
				   // pageTitle = page.getName();
				   // pagePath = page.getPath();
					
					pageTitle = hit.getTitle();
					pagePath = hit.getPath();
					
					String type = hit.getResource().getResourceType();
				    LOG.debug("result: " + pagePath);
					//if ("dam:Asset".equals(type)) {
				    if (pagePath.indexOf("jcr:content/metadata") >= 0) {
				    	
				    	pagePath = pagePath.replace("/jcr:content/metadata", "");
				   
					    JSONObject h = new JSONObject();
						h.putOpt("path", serverUrl + pagePath);
						h.putOpt("excerpt", "");
						h.putOpt("name", pageTitle);
						h.putOpt("title", pageTitle);
						h.put("totalHits", totalHits);
						//hits.put(h);
						hitMap.put(pagePath, h);
						total++;
						results++;
						/*
					    if (!pageTitle) {
					        pageTitle = pagePath.substring(pagePath.lastIndexOf('/') + 1, pagePath.length())
					 
					    }
					    searchHit.put("title", pageTitle)
					 
					    resultArray.put(searchHit)*/
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			success = false;
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			success = false;
			e.printStackTrace();
		}		
		
		JSONObject json = new JSONObject();    	
    	json.putOpt("success", success);
    	json.putOpt("results", hitMap.keySet().size());
    	json.putOpt("total", hitMap.keySet().size());
    	json.putOpt("more", false);
    	json.putOpt("offset", 0);	
    	for (String key : hitMap.keySet()) {
    		hits.put(hitMap.get(key));
    	}
		json.putOpt("hits", hits);

    	resp.setContentType("application/json");
        resp.getOutputStream().println(json.toString());	
	}

}
