package com.example;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class SolrClientApp {
    private static final Logger logger = LoggerFactory.getLogger(SolrClientApp.class);
    
    private static final String ZK_HOST = "127.0.0.1:9983";
    private static final String COLLECTION_NAME = "books";
    
    public static void main(String[] args) {
        logger.info("Starting Solr Client Application");
        
        try (CloudSolrClient solrClient = new CloudSolrClient.Builder(Arrays.asList(ZK_HOST), Optional.empty())
                .build()) {
            
            solrClient.setDefaultCollection(COLLECTION_NAME);
            
            logger.info("Connected to SolrCloud via ZooKeeper: {}", ZK_HOST);
            logger.info("Using collection: {}", COLLECTION_NAME);
            
            addBookDocuments(solrClient);
            
            commitChanges(solrClient);
            
            queryBooks(solrClient);
            
            logger.info("Solr Client Application completed successfully");
            
        } catch (Exception e) {
            logger.error("Error in Solr Client Application", e);
            System.exit(1);
        }
    }
    
    private static void addBookDocuments(SolrClient solrClient) throws SolrServerException, IOException {
        logger.info("Adding book documents to collection");
        
        SolrInputDocument book1 = new SolrInputDocument();
        book1.addField("id", "book1");
        book1.addField("title", "Solr in Action");
        book1.addField("author", "Trey Grainger");
        
        SolrInputDocument book2 = new SolrInputDocument();
        book2.addField("id", "book2");
        book2.addField("title", "Solr Cookbook");
        book2.addField("author", "Rafal Kuc");
        
        UpdateResponse response1 = solrClient.add(book1);
        UpdateResponse response2 = solrClient.add(book2);
        
        logger.info("Added book1 - Status: {}, QTime: {}ms", response1.getStatus(), response1.getQTime());
        logger.info("Added book2 - Status: {}, QTime: {}ms", response2.getStatus(), response2.getQTime());
    }
    
    private static void commitChanges(SolrClient solrClient) throws SolrServerException, IOException {
        logger.info("Committing changes to Solr");
        
        UpdateResponse commitResponse = solrClient.commit();
        logger.info("Commit completed - Status: {}, QTime: {}ms", 
                   commitResponse.getStatus(), commitResponse.getQTime());
    }
    
    private static void queryBooks(SolrClient solrClient) throws SolrServerException, IOException {
        logger.info("Querying books with title matching 'Solr*'");
        
        SolrQuery query = new SolrQuery();
        query.setQuery("title:Solr*");
        query.setFields("id", "title", "author");
        
        QueryResponse response = solrClient.query(query);
        SolrDocumentList documents = response.getResults();
        
        logger.info("Query completed - Found {} documents, QTime: {}ms", 
                   documents.getNumFound(), response.getQTime());
        
        System.out.println("\n=== Query Results ===");
        System.out.println("Found " + documents.getNumFound() + " documents matching 'Solr*':");
        
        for (SolrDocument doc : documents) {
            System.out.println("ID: " + doc.getFieldValue("id"));
            System.out.println("Title: " + doc.getFieldValue("title"));
            System.out.println("Author: " + doc.getFieldValue("author"));
            System.out.println("---");
        }
        
        System.out.println("=== End Results ===\n");
    }
}
