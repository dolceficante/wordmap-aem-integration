package com.wordmap.core.servlets;

import java.io.IOException;

import javax.jcr.Session;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import com.day.cq.tagging.JcrTagManagerFactory;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.wordmap.core.models.TaxonomyNode;
import com.wordmap.core.util.ServiceUtil;
import com.wordmap.core.util.TagUtil;

/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
@SuppressWarnings("serial")
@SlingServlet(paths = "/bin/wordmap/taxonomy-synch")
public class SimpleServlet extends SlingSafeMethodsServlet {
	
	@Reference
    private SlingRepository repository;
      
    public void bindRepository(SlingRepository repository) {
           this.repository = repository; 
           }	
    
    @Reference
    JcrTagManagerFactory tmf;    

    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
    	
    	String serverUrl = req.getParameter("serverUrl");
    	String sessionToken = req.getParameter("sessionToken");
    	String taxonomyId = req.getParameter("taxonomyId");
    	
    	//http://webservices.wordmap.com/wappkrn104g/tms/json/wordset/root
    	String requestUrl = "http://webservices.wordmap.com/wappkrn104g/tms/json/wordset/root";
    	//String requestUrl = serverUrl + "/tms/json/taxonomy/" + taxonomyId;
    	
    	JSONObject json = ServiceUtil.getInstance().getJson(requestUrl, sessionToken);
    	
    	//resp.getOutputStream().println(json.toString());
    	
        int parentWordsetId = json.optInt("id");
        String rootName = json.optString("leadword");
        
        TaxonomyNode rootNode = new TaxonomyNode();
        rootNode.setId(parentWordsetId);
        rootNode.setName(rootName);
        
        String ep = "http://webservices.wordmap.com/wappkrn104g/tms/json/wordset/" + parentWordsetId + "/parent/";
        JSONArray json3 = ServiceUtil.getInstance().getJsonArray(ep, sessionToken);
        //resp.getOutputStream().println(json3.toString());
        
        for (int x=0; x < json3.length(); x++) {
        	JSONObject j = json3.getJSONObject(x);
        	JSONObject wordset = j.optJSONObject("wordset");
        	String leadword = wordset.optString("leadword");
        	int wordsetId = wordset.optInt("id");
        	TaxonomyNode child = new TaxonomyNode(rootNode);
        	child.setId(wordsetId);
        	child.setName(leadword);
        	rootNode.addChild(child);
        	processChildren(parentWordsetId, wordsetId, child, sessionToken);
        }
        
        /*
    	String endpoint = "http://webservices.wordmap.com/wappkrn104g/tms/json/wordset/" + rootId + "/relationships";
    	JSONObject json2 = ServiceUtil.getInstance().getJson(endpoint, sessionToken);
		
    	resp.getOutputStream().println(json2.toString());
    	*/
    	
        /*
        processChildren(rootId, rootNode);
    	   */      
        //final Resource resource = req.getResource();

        ResourceResolver resourceResolver = req.getResourceResolver();
        //ResourceResolver resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
		Session session = resourceResolver.adaptTo(Session.class);

		String tagName = "wordmap:" + rootNode.getName();		
		
		TagManager tMgr = tmf.getTagManager(session);
		Tag tag = tMgr.resolve(tagName);
		if (tag != null) {
			tMgr.deleteTag(tag, true);
		}

		TagUtil.getInstance().buildTaxonomy(rootNode, tagName, tMgr); 		
		

		
        resp.getOutputStream().println(rootNode.toString());
       
    }
    
    private void processChildren(int parentWordsetId, int wordsetId, TaxonomyNode parentNode, String sessionToken) {
    	
    	String endpoint = "http://webservices.wordmap.com/wappkrn104g/tms/json/wordset/" + wordsetId + "/parent/" + parentWordsetId;
    	
    	JSONArray json3 = ServiceUtil.getInstance().getJsonArray(endpoint, sessionToken);
        
        for (int x=0; x < json3.length(); x++) {
        	JSONObject j = json3.getJSONObject(x);
        	JSONObject wordset = j.optJSONObject("wordset");
        	String leadword = wordset.optString("leadword");
        	int id = wordset.optInt("id");
        	TaxonomyNode child = new TaxonomyNode(parentNode);
        	child.setId(id);
        	child.setName(leadword);
        	parentNode.addChild(child);
        	processChildren(wordsetId, id, child, sessionToken);
        }
    	
    }
}
