package com.wordmap.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServiceUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(ServiceUtil.class);	
	
	public static ServiceUtil getInstance() {
		return new ServiceUtil();
	}
	
	public void setTaxonomy(String requestUrl, String sessionToken) {
		try {
			LOG.debug("requestUrl: " + requestUrl);
			LOG.debug("sessionToken: " + sessionToken);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			
			HttpPost post = new HttpPost(requestUrl);
			post.addHeader("Session-token", sessionToken);
			post.addHeader("Accept","*/*");
			
			HttpResponse response = httpClient.execute(post);

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			 
			String output;
			String myJSON="" ;
			while ((output = br.readLine()) != null) {
			    //System.out.println(output);
			    myJSON = myJSON + output;
			}
			LOG.debug("JSON: " + myJSON);
			
			httpClient.getConnectionManager().shutdown();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	
	public JSONObject getJson(String requestUrl, String sessionToken) {
		JSONObject json = new JSONObject();
    	try {
    		LOG.debug("requestUrl: " + requestUrl);
    		LOG.debug("sessionToken: " + sessionToken);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			
			HttpGet get = new HttpGet(requestUrl);
			get.addHeader("Session-token", sessionToken);
			get.addHeader("Accept","*/*");
			
			HttpResponse response = httpClient.execute(get);

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			 
			String output;
			String myJSON="" ;
			while ((output = br.readLine()) != null) {
			    //System.out.println(output);
			    myJSON = myJSON + output;
			}
			LOG.debug("JSON: " + myJSON);
			json = new JSONObject(myJSON);
			httpClient.getConnectionManager().shutdown();
		} catch (Exception e) {
			// TODO Auto-generated catch block			
			json.putOpt("error", e.toString());
			e.printStackTrace();
		}
        
    	return json;
	}

	public JSONArray getJsonArray(String requestUrl, String sessionToken) {
		JSONArray json = new JSONArray();
    	try {
    		LOG.debug("requestUrl: " + requestUrl);
    		LOG.debug("sessionToken: " + sessionToken);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			
			HttpGet get = new HttpGet(requestUrl);
			get.addHeader("Session-token", sessionToken);
			get.addHeader("Accept","*/*");
			
			HttpResponse response = httpClient.execute(get);

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			 
			String output;
			String myJSON="" ;
			while ((output = br.readLine()) != null) {
			    //System.out.println(output);
			    myJSON = myJSON + output;
			}
			LOG.debug("JSON: " + myJSON);
			json = new JSONArray(myJSON);
			httpClient.getConnectionManager().shutdown();
		} catch (Exception e) {
			// TODO Auto-generated catch block		
			JSONObject j = new JSONObject();
			j.putOpt("error", e.toString());
			json.put(j);
			e.printStackTrace();
		}
        
    	return json;
	}	
	
}
