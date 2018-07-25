package com.wordmap.core.servlets;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Session;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.tagging.JcrTagManagerFactory;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.github.slugify.Slugify;
import com.wordmap.core.models.Field;
import com.wordmap.core.models.Schema;
import com.wordmap.core.models.TaxonomyNode;
import com.wordmap.core.models.Value;
import com.wordmap.core.util.JcrUtil;
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
	
	private static final Logger LOG = LoggerFactory.getLogger(JcrUtil.class);	
	
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
    	
    	String serverUrl = req.getParameter("serverUrl");//http://webservices.wordmap.com/wappkrn104g
    	String sessionToken = req.getParameter("sessionToken");
    	String taxonomyId = req.getParameter("taxonomyId");
    	
    	String requestUrl = serverUrl + "/tms/json/wordset/root";
		String url = serverUrl + "/tms/json/taxonomy/" + taxonomyId;
		ServiceUtil.getInstance().setTaxonomy(url, sessionToken);
    	
    	JSONObject json = ServiceUtil.getInstance().getJson(requestUrl, sessionToken);
    	
        int parentWordsetId = json.optInt("id");
        String rootName = json.optString("leadword");
        
    	Schema schema = new Schema(rootName);        
        
        TaxonomyNode rootNode = new TaxonomyNode();
        rootNode.setId(parentWordsetId);
        rootNode.setName(rootName);
        
        String ep = serverUrl + "/tms/json/wordset/" + parentWordsetId + "/parent/";
        JSONArray json3 = ServiceUtil.getInstance().getJsonArray(ep, sessionToken);
        
        ResourceResolver resourceResolver = req.getResourceResolver();
 		Session session = resourceResolver.adaptTo(Session.class);        
        
        for (int x=0; x < json3.length(); x++) {
        	JSONObject j = json3.getJSONObject(x);
        	JSONObject wordset = j.optJSONObject("wordset");
        	String leadword = wordset.optString("leadword");
        	int wordsetId = wordset.optInt("id");
        	
        	String featureUrl = "http://webservices.wordmap.com/wappkrn104g/tms/json/wordset/" + wordsetId + "/features";
        	JSONArray featuresJson = ServiceUtil.getInstance().getJsonArray(featureUrl, sessionToken);

        	String aemId = null;
        	String taggedInAemId = null;
        	for (int y = 0 ; y < featuresJson.length(); y++) {
        		JSONObject feature = featuresJson.optJSONObject(y);
        		String featureId = feature.optString("FeatureId");
        		if (feature != null) {
        			JSONObject featureType = feature.optJSONObject("FeatureType");
        			String value = feature.optString("Value");
        			if (featureType != null) {
        				String name = featureType.optString("name");
        				if ("AEMFacetSEQ".equals(name)) {
        					aemId = value;
        				} else if ("TaggedinAEM".equals(name)) {
        					taggedInAemId = featureId;
        				}
        			}
        		}
        	}
        	
        	if (aemId != null) {
        		Field f = new Field(leadword);
        		f.setId(aemId);
        		schema.addField(f);
        		
            	//check for documents that are tagged
            	boolean taggedDocuments = checkForTaggedDocuments(leadword, null, resourceResolver);
        		
            	//update wordmap service
            	updateTaggedDocumentsFlag(serverUrl, sessionToken, wordsetId, taggedInAemId, taggedDocuments);
        		
	        	TaxonomyNode child = new TaxonomyNode(rootNode);
	        	child.setId(wordsetId);
	        	child.setName(leadword);
	        	rootNode.addChild(child);
	        	processChildren(parentWordsetId, wordsetId, child, sessionToken,f,resourceResolver);
        	}
        }



		String tagName = "Wordmap:" + rootNode.getName();		
		
		TagManager tMgr = tmf.getTagManager(session);
		Tag tag = tMgr.resolve(tagName);
		if (tag != null) {
			tMgr.deleteTag(tag, true);
		}

		TagUtil.getInstance().buildTaxonomy(rootNode, tagName, tMgr); 		
		
		JcrUtil.getInstance().buildSchema(schema, session);
		
        resp.getOutputStream().println(rootNode.toString());
       
    }
    
    private void processChildren(int parentWordsetId, int wordsetId, TaxonomyNode parentNode, String sessionToken, Field field, ResourceResolver resourceResolver) {
    	
    	String endpoint = "http://webservices.wordmap.com/wappkrn104g/tms/json/wordset/" + wordsetId + "/parent/" + parentWordsetId + "/relationshipTypeNames;name=is%20a";
    	
    	JSONArray json3 = ServiceUtil.getInstance().getJsonArray(endpoint, sessionToken);
        
        for (int x=0; x < json3.length(); x++) {
        	JSONObject j = json3.getJSONObject(x);
        	JSONObject wordset = j.optJSONObject("wordset");
        	String leadword = wordset.optString("leadword");
        	int id = wordset.optInt("id");
        	Value value = new Value(String.valueOf(id), leadword);
        	field.addValue(value);
        	TaxonomyNode child = new TaxonomyNode(parentNode);
        	child.setId(id);
        	child.setName(leadword);
        	parentNode.addChild(child);
        	
        	//check for documents that are tagged
        	boolean taggedDocuments = checkForTaggedDocuments(parentNode.getName(), String.valueOf(id), resourceResolver);
        	
        	String featureUrl = "http://webservices.wordmap.com/wappkrn104g/tms/json/wordset/" + id + "/features";
        	JSONArray featuresJson = ServiceUtil.getInstance().getJsonArray(featureUrl, sessionToken);
    		
        	String taggedInAemId = null;
        	for (int y = 0 ; y < featuresJson.length(); y++) {
        		JSONObject feature = featuresJson.optJSONObject(y);
        		String featureId = feature.optString("FeatureId");
        		if (feature != null) {
        			JSONObject featureType = feature.optJSONObject("FeatureType");
        			if (featureType != null) {
        				String name = featureType.optString("name");
        				if ("TaggedinAEM".equals(name)) {
        					taggedInAemId = featureId;
        				}
        			}
        		}
        	}        	
        	
        	//update wordmap service
        	updateTaggedDocumentsFlag("http://webservices.wordmap.com/wappkrn104g", sessionToken, id, taggedInAemId, taggedDocuments);
        	
        }
    	
    }
    
    private boolean checkForTaggedDocuments(String fieldName, String valueId, ResourceResolver resourceResolver) {
    	boolean result = false;
    	
    	try {
			Slugify slug = new Slugify();
			fieldName = slug.slugify(fieldName);   
			fieldName = "dam:" + fieldName;

			LOG.debug("checking for " + fieldName + " " + valueId);
			
			DamUtil util = new DamUtil();
			Resource res = resourceResolver.resolve("/content/dam/wordmap");
			Iterator<Asset> assets = util.getAssets(res);
			
			while (assets.hasNext()) {
				Asset asset = assets.next();
				LOG.debug("->asset " + asset.getName());
				//Map<String,Object> metadata = asset.getMetadata();
				//for (String field : metadata.keySet()) {
				//	LOG.debug("--> " + field);
				//}
				
				String value = asset.getMetadataValue(fieldName);
				if (value != null && !value.isEmpty()) {
					LOG.debug("->asset value " + value);
					if (valueId == null) {
						if (value != null) {
							result = true;
						}    	
					} else {
						if (value.equals(valueId)){
							LOG.debug("<-value match->");
							result = true;
						}
					}
				}	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return result;
    }
    
    private void updateTaggedDocumentsFlag(String serverUrl, String sessionToken, int wordset, String featureId, boolean flag) {
    	String value = "0";
    	if (flag) {
    		value = "1";
    	}    	
    	JSONObject json = new JSONObject();
    	json.putOpt("value", value);
    	
    	String requestUrl = serverUrl + "/tms/xml/wordset/" + wordset + "/feature/" + featureId + ";value=" + value + ";";  
    	//String requestUrl = serverUrl + "/tms/json/wordset/" + wordset + "/feature/" + featureId;  
    	ServiceUtil.getInstance().doPut(requestUrl, sessionToken, json);
    }
}
