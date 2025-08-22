# Solr 9.9 Development Environment with Java SolrJ Client

This project sets up a complete Solr 9.9 development environment in SolrCloud mode with a Java client using SolrJ without Jetty.

## Environment Setup

### Prerequisites
- Java 17
- Maven 3.x
- Linux/Unix environment

### Architecture
- **Solr 9.9** in SolrCloud mode with 3 nodes (ports 8983, 8984, 8985)
- **ZooKeeper ensemble** with 3 nodes (ports 2181, 2182, 2183)
- **Books collection** with 2 shards and replication factor 2
- **Java client** using SolrJ 9.9.0 with Apache HttpClient5 (Jetty dependencies included but not explicitly used)

## Directory Structure
```
~/solr-dev/
├── solr-9.9.0/                    # Solr installation
│   ├── bin/                       # Solr binaries
│   ├── server/                    # Solr server files
│   └── zookeeper/                 # ZooKeeper configuration
│       ├── node1/
│       ├── node2/
│       └── node3/
└── java-solr-client/              # Maven Java project
    ├── pom.xml
    ├── src/main/java/com/example/
    │   └── SolrClientApp.java
    └── src/main/resources/
        └── logback.xml
```

## Starting the Environment

### 1. Start ZooKeeper Ensemble
```bash
cd ~/solr-dev/solr-9.9.0

# Start ZooKeeper nodes in background
java -cp server/solr-webapp/webapp/WEB-INF/lib/*:server/lib/ext/* org.apache.zookeeper.server.quorum.QuorumPeerMain zookeeper/node1/zoo.cfg &
java -cp server/solr-webapp/webapp/WEB-INF/lib/*:server/lib/ext/* org.apache.zookeeper.server.quorum.QuorumPeerMain zookeeper/node2/zoo.cfg &
java -cp server/solr-webapp/webapp/WEB-INF/lib/*:server/lib/ext/* org.apache.zookeeper.server.quorum.QuorumPeerMain zookeeper/node3/zoo.cfg &
```

### 2. Start Solr Nodes
```bash
# Start first Solr node
bin/solr start -c -p 8983 -s server/solr

# Start second Solr node
bin/solr start -c -p 8984 -s server/solr -z localhost:2181

# Start third Solr node
bin/solr start -c -p 8985 -s server/solr -z localhost:2181
```

### 3. Create Books Collection
```bash
bin/solr create -c books -shards 2 -replicationFactor 2
```

### 4. Verify Setup
```bash
# Check Solr status
bin/solr status

# Check collection health
bin/solr healthcheck -c books
```

## Java Client Application

### Dependencies
The Maven project includes:
- **Java 17** compilation target
- **Apache HttpClient5** for HTTP REST calls to Solr
- **Jackson** for JSON processing
- **SLF4J + Logback** for logging

### Key Features
- Connects directly to Solr via HTTP REST API (http://localhost:8983/solr)
- Uses direct HTTP POST/GET requests to Solr endpoints
- Adds sample book documents with fields: id, title, author
- Commits changes to ensure persistence
- Queries documents where title matches "Solr*"
- Deletes documents by ID
- Prints formatted results to console

### Building and Running

#### Build the Project
```bash
cd ~/solr-dev/java-solr-client
mvn clean package
```

#### Run the Application
```bash
mvn exec:java -Dexec.mainClass="com.example.SolrClientApp"
```

### Expected Output
```
01:58:28.419 [com.example.SolrClientApp.main()] INFO  com.example.SolrClientApp - Starting Solr Client Application
01:58:28.646 [com.example.SolrClientApp.main()] INFO  com.example.SolrClientApp - Connected to SolrCloud via ZooKeeper: 127.0.0.1:9983
01:58:28.647 [com.example.SolrClientApp.main()] INFO  com.example.SolrClientApp - Using collection: books
01:58:28.647 [com.example.SolrClientApp.main()] INFO  com.example.SolrClientApp - Adding book documents to collection
01:58:29.320 [com.example.SolrClientApp.main()] INFO  com.example.SolrClientApp - Added book1 - Status: 0, QTime: 582ms
01:58:29.320 [com.example.SolrClientApp.main()] INFO  com.example.SolrClientApp - Added book2 - Status: 0, QTime: 17ms
01:58:29.321 [com.example.SolrClientApp.main()] INFO  com.example.SolrClientApp - Committing changes to Solr
01:58:29.460 [com.example.SolrClientApp.main()] INFO  com.example.SolrClientApp - Commit completed - Status: 0, QTime: 130ms
01:58:29.460 [com.example.SolrClientApp.main()] INFO  com.example.SolrClientApp - Querying books with title matching 'Solr*'
01:58:29.512 [com.example.SolrClientApp.main()] INFO  com.example.SolrClientApp - Query completed - Found 2 documents, QTime: 46ms

=== Query Results ===
Found 2 documents matching 'Solr*':
ID: book2
Title: [Solr Cookbook]
Author: [Rafal Kuc]
---
ID: book1
Title: [Solr in Action]
Author: [Trey Grainger]
---
=== End Results ===

01:58:29.518 [com.example.SolrClientApp.main()] INFO  com.example.SolrClientApp - Solr Client Application completed successfully
```

## Technical Notes

### HTTP REST API Implementation
- Uses Apache HttpClient5 for direct HTTP communication with Solr
- No SolrJ dependencies - pure HTTP REST calls to Solr endpoints
- Jackson library handles JSON serialization/deserialization
- Connects directly to Solr node at http://localhost:8983/solr

### Solr REST Endpoints Used
- **Add Documents**: `POST /solr/books/update/json/docs`
- **Commit Changes**: `POST /solr/books/update?commit=true`
- **Query Documents**: `GET /solr/books/select?q=title:Solr*&fl=id,title,author&wt=json`
- **Delete Documents**: `POST /solr/books/update` with delete JSON payload

### ZooKeeper Configuration
- External ZooKeeper ensemble runs on ports 2181, 2182, 2183
- Solr also runs embedded ZooKeeper on port 9983
- Java client connects directly to Solr HTTP endpoint (no ZooKeeper client needed)

### Collection Configuration
- **Name**: books
- **Shards**: 2 (shard1, shard2)
- **Replication Factor**: 2 (each shard has 2 replicas)
- **Total Cores**: 4 (2 shards × 2 replicas)

## Stopping the Environment

```bash
# Stop Solr nodes
cd ~/solr-dev/solr-9.9.0
bin/solr stop -all

# Stop ZooKeeper processes
pkill -f QuorumPeerMain
```

## Troubleshooting

### Common Issues
1. **Collection not found**: Ensure all Solr nodes are connected to the same ZooKeeper instance
2. **Connection refused**: Verify ZooKeeper and Solr processes are running
3. **Build failures**: Ensure Java 17 and Maven are properly installed

### Verification Commands
```bash
# Check running processes
ps aux | grep solr
ps aux | grep zookeeper

# Test Solr connectivity
curl "http://localhost:8983/solr/admin/collections?action=LIST"

# Check collection status
bin/solr healthcheck -c books
```
