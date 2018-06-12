package com.wordmap.core.util;

import org.apache.sling.api.resource.ResourceResolver;

import com.wordmap.core.models.TaxonomyNode;

/**
 * A simple service interface
 */
public interface HelloService {
     
    /**
     * @return the name of the underlying JCR repository implementation
     */
    public String getRepositoryName();
     
    public void buildTaxonomy(TaxonomyNode rootNode, String namespace); 
 
}

