# Solr 9.9 Development Environment with Spring Boot SolrJ Client

This project sets up a complete Solr 9.9 development environment in SolrCloud mode with a Spring Boot 3.5.3 Java client using SolrJ 9.9.0.

## Environment Setup

### Prerequisites
- Java 17
- Maven 3.x
- Linux/Unix environment

### Architecture
- **Solr 9.9** in SolrCloud mode with 3 nodes (ports 8983, 8984, 8985)
- **ZooKeeper ensemble** with 3 nodes (ports 2181, 2182, 2183)
- **Books collection** with 2 shards and replication factor 2
- **Spring Boot 3.5.3 client** using SolrJ 9.9.0 with Spring Boot framework

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
└── java-solr-client/              # Spring Boot Maven project
    ├── pom.xml
    ├── src/main/java/com/example/
    │   ├── SolrSpringBootApplication.java
    │   └── SolrService.java
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

## Spring Boot Java Client Application

### Dependencies
The Spring Boot Maven project includes:
- **Spring Boot 3.5.3** framework
- **Java 17** compilation target
- **SolrJ 9.9.0** client library
- **SLF4J + Logback** for logging (via Spring Boot)

### Key Features
- Spring Boot application with @SpringBootApplication main class
- SolrService as a Spring @Service component
- Connects to Solr via SolrJ HttpSolrClient (http://localhost:8983/solr/books)
- Uses SolrJ API for all Solr operations (add, commit, query, delete)
- Adds sample book documents with fields: id, title, author
- Commits changes to ensure persistence
- Queries documents where title matches "Solr*"
- Deletes documents by ID
- Prints formatted results to console
- CommandLineRunner executes Solr operations on application startup

### Building and Running

#### Build the Project
```bash
cd ~/solr-dev/java-solr-client
mvn clean package
```

#### Run the Spring Boot Application
```bash
mvn spring-boot:run
```

Or run the packaged JAR:
```bash
java -jar target/solr-client-1.0-SNAPSHOT.jar
```

### Expected Output
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.5.3)

17:03:01.159 [main] INFO  c.example.SolrSpringBootApplication - Starting SolrSpringBootApplication using Java 17.0.13
17:03:01.565 [main] INFO  c.example.SolrSpringBootApplication - Started SolrSpringBootApplication in 0.654 seconds
17:03:01.567 [main] INFO  com.example.SolrService - Starting Solr SolrJ Client Application with Spring Boot
17:03:01.707 [main] INFO  com.example.SolrService - Connected to Solr at: http://localhost:8983/solr/books
17:03:01.708 [main] INFO  com.example.SolrService - Using collection: books
17:03:01.708 [main] INFO  com.example.SolrService - Adding book documents to collection via SolrJ
17:03:02.300 [main] INFO  com.example.SolrService - Added book1 - Status: 0, QTime: 523ms
17:03:02.300 [main] INFO  com.example.SolrService - Added book2 - Status: 0, QTime: 17ms
17:03:02.300 [main] INFO  com.example.SolrService - Committing changes to Solr via SolrJ
17:03:02.409 [main] INFO  com.example.SolrService - Commit completed - Status: 0, QTime: 105ms
17:03:02.409 [main] INFO  com.example.SolrService - Querying books with title matching 'Solr*' via SolrJ
17:03:02.473 [main] INFO  com.example.SolrService - Query completed - Found 2 documents, QTime: 58ms

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

17:03:02.475 [main] INFO  com.example.SolrService - Deleting document with ID: book1 via SolrJ
17:03:02.486 [main] INFO  com.example.SolrService - Deleted document book1 - Status: 0, QTime: 8ms
17:03:02.521 [main] INFO  com.example.SolrService - Solr SolrJ Client Application completed successfully
```

## Technical Notes

### Spring Boot SolrJ Implementation
- Uses Spring Boot 3.5.3 framework with dependency injection
- SolrJ 9.9.0 client library for all Solr operations
- HttpSolrClient connects directly to Solr node at http://localhost:8983/solr/books
- Spring @Service component encapsulates Solr operations
- CommandLineRunner executes operations on application startup

### SolrJ Operations Used
- **Add Documents**: `SolrClient.add(SolrInputDocument)`
- **Commit Changes**: `SolrClient.commit()`
- **Query Documents**: `SolrClient.query(SolrQuery)` with `title:Solr*`
- **Delete Documents**: `SolrClient.deleteById(String)`

### ZooKeeper Configuration
- External ZooKeeper ensemble runs on ports 2181, 2182, 2183
- Solr also runs embedded ZooKeeper on port 9983
- Spring Boot client connects directly to Solr HTTP endpoint via SolrJ (no ZooKeeper client needed)

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

## Docker Setup

### Prerequisites for Docker
- Docker
- Docker Compose

### Quick Start with Docker
```bash
cd ~/solr-dev
docker-compose up --build
```

This will:
- Start ZooKeeper container
- Start 3 Solr nodes (ports 8983, 8984, 8985)
- Create the `books` collection automatically
- Build and run the Spring Boot client application

### Docker Services
- **ZooKeeper**: Port 2181
- **Solr Node 1**: Port 8983 (primary, creates collection)
- **Solr Node 2**: Port 8984
- **Solr Node 3**: Port 8985
- **Spring Boot Client**: Connects to Solr via internal Docker network

### Docker Commands
```bash
# Build and start all services
docker-compose up --build

# Start only Solr environment
docker-compose up zookeeper solr1 solr2 solr3

# View logs
docker-compose logs solr-client

# Clean up
docker-compose down
```

See <ref_file file="/home/ubuntu/solr-dev/README-Docker.md" /> for detailed Docker documentation.
