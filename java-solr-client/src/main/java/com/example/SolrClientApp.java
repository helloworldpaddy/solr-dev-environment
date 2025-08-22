package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SolrClientApp {
    private static final Logger logger = LoggerFactory.getLogger(SolrClientApp.class);
    
    private static final String SOLR_BASE_URL = "http://localhost:8983/solr";
    private static final String COLLECTION_NAME = "books";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void main(String[] args) {
        logger.info("Starting Solr HTTP REST Client Application");
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            
            logger.info("Connected to Solr at: {}", SOLR_BASE_URL);
            logger.info("Using collection: {}", COLLECTION_NAME);
            
            addBookDocuments(httpClient);
            
            commitChanges(httpClient);
            
            queryBooks(httpClient);
            
            deleteDocument(httpClient, "book1");
            
            commitChanges(httpClient);
            
            queryBooks(httpClient);
            
            logger.info("Solr HTTP REST Client Application completed successfully");
            
        } catch (Exception e) {
            logger.error("Error in Solr HTTP REST Client Application", e);
            System.exit(1);
        }
    }
    
    private static void addBookDocuments(CloseableHttpClient httpClient) throws IOException {
        logger.info("Adding book documents to collection via HTTP REST");
        
        String updateUrl = SOLR_BASE_URL + "/" + COLLECTION_NAME + "/update/json/docs";
        
        String book1Json = """
            {
                "id": "book1",
                "title": "Solr in Action",
                "author": "Trey Grainger"
            }
            """;
        
        HttpPost post1 = new HttpPost(updateUrl);
        post1.setEntity(new StringEntity(book1Json, ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response1 = httpClient.execute(post1)) {
            int statusCode1 = response1.getCode();
            String responseBody1 = new String(response1.getEntity().getContent().readAllBytes());
            JsonNode jsonResponse1 = objectMapper.readTree(responseBody1);
            
            logger.info("Added book1 - Status: {}, QTime: {}ms", 
                       statusCode1, jsonResponse1.path("responseHeader").path("QTime").asInt());
        }
        
        String book2Json = """
            {
                "id": "book2",
                "title": "Solr Cookbook",
                "author": "Rafal Kuc"
            }
            """;
        
        HttpPost post2 = new HttpPost(updateUrl);
        post2.setEntity(new StringEntity(book2Json, ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response2 = httpClient.execute(post2)) {
            int statusCode2 = response2.getCode();
            String responseBody2 = new String(response2.getEntity().getContent().readAllBytes());
            JsonNode jsonResponse2 = objectMapper.readTree(responseBody2);
            
            logger.info("Added book2 - Status: {}, QTime: {}ms", 
                       statusCode2, jsonResponse2.path("responseHeader").path("QTime").asInt());
        }
    }
    
    private static void commitChanges(CloseableHttpClient httpClient) throws IOException {
        logger.info("Committing changes to Solr via HTTP REST");
        
        String commitUrl = SOLR_BASE_URL + "/" + COLLECTION_NAME + "/update?commit=true";
        
        HttpPost commitPost = new HttpPost(commitUrl);
        commitPost.setEntity(new StringEntity("{}", ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response = httpClient.execute(commitPost)) {
            int statusCode = response.getCode();
            String responseBody = new String(response.getEntity().getContent().readAllBytes());
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            
            logger.info("Commit completed - Status: {}, QTime: {}ms", 
                       statusCode, jsonResponse.path("responseHeader").path("QTime").asInt());
        }
    }
    
    private static void queryBooks(CloseableHttpClient httpClient) throws IOException {
        logger.info("Querying books with title matching 'Solr*' via HTTP REST");
        
        String query = URLEncoder.encode("title:Solr*", StandardCharsets.UTF_8);
        String fields = URLEncoder.encode("id,title,author", StandardCharsets.UTF_8);
        String queryUrl = SOLR_BASE_URL + "/" + COLLECTION_NAME + "/select?q=" + query + "&fl=" + fields + "&wt=json";
        
        HttpGet get = new HttpGet(queryUrl);
        
        try (CloseableHttpResponse response = httpClient.execute(get)) {
            int statusCode = response.getCode();
            String responseBody = new String(response.getEntity().getContent().readAllBytes());
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            
            JsonNode docs = jsonResponse.path("response").path("docs");
            int numFound = jsonResponse.path("response").path("numFound").asInt();
            int qTime = jsonResponse.path("responseHeader").path("QTime").asInt();
            
            logger.info("Query completed - Found {} documents, QTime: {}ms", numFound, qTime);
            
            System.out.println("\n=== Query Results ===");
            System.out.println("Found " + numFound + " documents matching 'Solr*':");
            
            for (JsonNode doc : docs) {
                System.out.println("ID: " + doc.path("id").asText());
                
                JsonNode titleNode = doc.path("title");
                String title = titleNode.isArray() ? titleNode.get(0).asText() : titleNode.asText();
                System.out.println("Title: " + title);
                
                JsonNode authorNode = doc.path("author");
                String author = authorNode.isArray() ? authorNode.get(0).asText() : authorNode.asText();
                System.out.println("Author: " + author);
                
                System.out.println("---");
            }
            
            System.out.println("=== End Results ===\n");
        }
    }
    
    private static void deleteDocument(CloseableHttpClient httpClient, String documentId) throws IOException {
        logger.info("Deleting document with ID: {} via HTTP REST", documentId);
        
        String deleteUrl = SOLR_BASE_URL + "/" + COLLECTION_NAME + "/update";
        
        String deleteJson = """
            {
                "delete": {
                    "id": "%s"
                }
            }
            """.formatted(documentId);
        
        HttpPost deletePost = new HttpPost(deleteUrl);
        deletePost.setEntity(new StringEntity(deleteJson, ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response = httpClient.execute(deletePost)) {
            int statusCode = response.getCode();
            String responseBody = new String(response.getEntity().getContent().readAllBytes());
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            
            logger.info("Deleted document {} - Status: {}, QTime: {}ms", 
                       documentId, statusCode, jsonResponse.path("responseHeader").path("QTime").asInt());
        }
    }
}
