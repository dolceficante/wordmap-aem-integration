package com.wordmap.core.util;

import org.apache.sling.api.resource.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.wordmap.core.models.TaxonomyNode;

public class TagUtil  {
	
	private static final Logger LOG = LoggerFactory.getLogger(TagUtil.class);	
	
	public static TagUtil getInstance() {
		return new TagUtil();
	}
	
	public void buildTaxonomy(TaxonomyNode rootNode, String namespace, TagManager tMgr) {
		


		try {
			 
			for (TaxonomyNode child : rootNode.getChildren()) {
				String name = child.getName();
				String tagName = namespace + "/" + name;
				Tag tag = tMgr.createTag(tagName, name, name, true);
				buildTaxonomy(child, tagName, tMgr);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
	}
	
	
}
