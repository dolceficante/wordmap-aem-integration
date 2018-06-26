package com.wordmap.core.util;

import java.io.IOException;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.slugify.Slugify;
import com.wordmap.core.models.Field;
import com.wordmap.core.models.Schema;
import com.wordmap.core.models.Value;

public class JcrUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(JcrUtil.class);	
	
	public static JcrUtil getInstance() {
		return new JcrUtil();
	}
	
	public void buildSchema(Schema schema, Session session) {
		LOG.debug("writing JCR Structure");
		
		try {
			Slugify slug = new Slugify();
			String schemaSlug = slug.slugify(schema.getName());
			String schemaNodePath = "/conf/global/settings/dam/adminui-extension/metadataschema/" + schemaSlug;

			session.refresh(true);
			Node wordmapNode = null;
			if (session.nodeExists(schemaNodePath)) {
				wordmapNode = session.getNode(schemaNodePath);
			} else {
				Node node = session.getNode("/conf/global/settings/dam/adminui-extension/metadataschema");
				wordmapNode = node.addNode(schemaSlug);
			}
			wordmapNode.setProperty("jcr:title", schema.getName());
			
			Node items = getNode(wordmapNode,"items");
			items.setPrimaryType("nt:unstructured");
			Node tabs = getNode(items,"tabs");
			tabs.setProperty("size", "L");
			tabs.setProperty("sling:resourceType", "granite/ui/components/coral/foundation/tabs");
			tabs.setPrimaryType("nt:unstructured");
			Node items2 = getNode(tabs,"items");
			items2.setPrimaryType("nt:unstructured");
			Node tab1 = getNode(items2,"tab1");
			tab1.setProperty("granite:rel", "aem-assets-metadata-form-tab");
			tab1.setPrimaryType("nt:unstructured");
			tab1.setProperty("jcr:title", "Properties");
			tab1.setProperty("listOrder", "0");
			tab1.setProperty("sling:resourceType", "granite/ui/components/coral/foundation/container");
			Node graniteData = getNode(tab1,"granite:data");
			graniteData.setPrimaryType("nt:unstructured");
			graniteData.setProperty("tabid",UUID.randomUUID().toString());
			Node items3 = getNode(tab1,"items");
			items3.setPrimaryType("nt:unstructured");
			
			int count = 0;
			for (Field field : schema.getFields()) {
				count++;
				String fieldNameSlug = slug.slugify(field.getName());
				Node col = getNode(items3,"col" + count);
				col.setPrimaryType("nt:unstructured");
				col.setProperty("granite:rel","aem-assets-metadata-form-column");
				col.setProperty("listOrder",String.valueOf(count));
				col.setProperty("oice-options","on");
				col.setProperty("sling:resourceType","granite/ui/components/coral/foundation/container");
				
				Node items4 = getNode(col,"items");
				items4.setPrimaryType("nt:unstructured");
				Node aemId = getNode(items4,field.getId());
				aemId.setProperty("emptyText","Select Option");
				aemId.setProperty("fieldLabel",field.getName());
				aemId.setProperty("name","./jcr:content/metadata/" + fieldNameSlug);
				aemId.setProperty("resourceType","granite/ui/components/coral/foundation/form/select");
				aemId.setProperty("sling:resourceType","dam/gui/components/admin/schemafield");
				aemId.setPrimaryType("nt:unstructured");
				
				Node graniteData2 = getNode(aemId,"granite:data");
				graniteData2.setProperty("choicesCascading","default");
				graniteData2.setPrimaryType("nt:unstructured");
				graniteData2.setProperty("metaType","dropdown");
				graniteData2.setProperty("requiredCascading","default");
				graniteData2.setProperty("visibilityCascading","default");
				
				Node cascadeitems = getNode(aemId,"cascadeitems");
				cascadeitems.setPrimaryType("nt:unstructured");
				cascadeitems.setProperty("sling:resourceType","dam/gui/coral/components/admin/schemaforms/formbuilder/cascadeitems");
				
				Node items5 = getNode(aemId,"items");
				items5.setPrimaryType("nt:unstructured");
				
				for (Value value : field.getValues()) {
					Node valueNode = getNode(items5,value.getId());
					valueNode.setPrimaryType("nt:unstructured");
					valueNode.setProperty("text",value.getText());
					valueNode.setProperty("value",value.getId());
				}
				
			}
			
			session.save();
			LOG.debug("Done");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error(e.toString());
			e.printStackTrace();
		} catch (PathNotFoundException e) {
			// TODO Auto-generated catch block
			LOG.error(e.toString());
			e.printStackTrace();
		} catch (RepositoryException e) {
			LOG.error(e.toString());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Node getNode(Node parent, String nodeName) throws RepositoryException {
		if (parent.hasNode(nodeName)) {
			return parent.getNode(nodeName);
		} else {
			return parent.addNode(nodeName);
		}
	}

}
