package com.example;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SolrService {
    private static final Logger logger = LoggerFactory.getLogger(SolrService.class);
    
    private static final String COLLECTION_NAME = "books";
    
    public void runSolrOperations() {
        logger.info("Starting Solr SolrJ Client Application with Spring Boot");
        
        String solrUrl = System.getenv().getOrDefault("SOLR_URL", "http://localhost:8983/solr");
        String collectionUrl = solrUrl;
        
        try (HttpSolrClient solrClient = new HttpSolrClient.Builder(collectionUrl).build()) {
            
            logger.info("Connected to Solr at: {}", collectionUrl);
            logger.info("Using collection: {}", COLLECTION_NAME);
            
            addBookDocuments(solrClient);
            
            commitChanges(solrClient);
            
            queryBooks(solrClient);
            
            deleteDocument(solrClient, "book1");
            
            commitChanges(solrClient);
            
            queryBooks(solrClient);
            
            logger.info("Solr SolrJ Client Application completed successfully");
            
        } catch (Exception e) {
            logger.error("Error in Solr SolrJ Client Application", e);
            throw new RuntimeException("Solr operations failed", e);
        }
    }
    
    private void addBookDocuments(SolrClient solrClient) throws SolrServerException, IOException {
        logger.info("Adding book documents to collection via SolrJ");
        
        SolrInputDocument book1 = new SolrInputDocument();
        book1.addField("id", "book1");
        book1.addField("title", "Solr in Action");
        book1.addField("author", "Trey Grainger");
        
        SolrInputDocument book2 = new SolrInputDocument();
        book2.addField("id", "book2");
        book2.addField("title", "Solr Cookbook");
        book2.addField("author", "Rafal Kuc");
        
        UpdateResponse response1 = solrClient.add(COLLECTION_NAME, book1);
        UpdateResponse response2 = solrClient.add(COLLECTION_NAME, book2);
        
        logger.info("Added book1 - Status: {}, QTime: {}ms", response1.getStatus(), response1.getQTime());
        logger.info("Added book2 - Status: {}, QTime: {}ms", response2.getStatus(), response2.getQTime());
    }
    
    private void commitChanges(SolrClient solrClient) throws SolrServerException, IOException {
        logger.info("Committing changes to Solr via SolrJ");
        
        UpdateResponse commitResponse = solrClient.commit(COLLECTION_NAME);
        logger.info("Commit completed - Status: {}, QTime: {}ms", 
                   commitResponse.getStatus(), commitResponse.getQTime());
    }
    
    private void queryBooks(SolrClient solrClient) throws SolrServerException, IOException {
        logger.info("Querying books with title matching 'Solr*' via SolrJ");
        
        SolrQuery query = new SolrQuery();
        query.setQuery("title:Solr*");
        query.setFields("id", "title", "author");
        
        QueryResponse response = solrClient.query(COLLECTION_NAME, query);
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
    
    private void deleteDocument(SolrClient solrClient, String documentId) throws SolrServerException, IOException {
        logger.info("Deleting document with ID: {} via SolrJ", documentId);
        
        UpdateResponse deleteResponse = solrClient.deleteById(COLLECTION_NAME, documentId);
        logger.info("Deleted document {} - Status: {}, QTime: {}ms", 
                   documentId, deleteResponse.getStatus(), deleteResponse.getQTime());
    }
}
