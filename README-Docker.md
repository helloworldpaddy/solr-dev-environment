# Docker Setup for Solr 9.9 Development Environment

This directory contains Docker configuration to run the complete Solr 9.9 development environment with Spring Boot client in containers.

## Prerequisites
- Docker
- Docker Compose

## Quick Start

### 1. Build and Run with Docker Compose
```bash
cd ~/solr-dev
docker-compose up --build
```

This will:
- Start ZooKeeper container
- Start 3 Solr nodes (ports 8983, 8984, 8985)
- Create the `books` collection automatically
- Build and run the Spring Boot client application

### 2. Run Individual Services

#### Start Solr Environment Only
```bash
docker-compose up zookeeper solr1 solr2 solr3
```

#### Build and Run Client Only (after Solr is running)
```bash
cd java-solr-client
docker build -t solr-spring-client .
docker run --network solr-dev_solr-network -e SOLR_URL=http://solr1:8983/solr/books solr-spring-client
```

## Docker Services

### ZooKeeper
- **Container**: `solr-zookeeper`
- **Port**: 2181
- **Image**: zookeeper:3.8

### Solr Nodes
- **solr1**: Port 8983 (primary node, creates collection)
- **solr2**: Port 8984
- **solr3**: Port 8985
- **Image**: solr:9.9
- **Collection**: `books` (2 shards, replication factor 2)

### Spring Boot Client
- **Container**: `solr-spring-client`
- **Built from**: `java-solr-client/Dockerfile`
- **Environment**: `SOLR_URL=http://solr1:8983/solr/books`

## Configuration

### Environment Variables
- `SOLR_URL`: Solr endpoint URL (default: http://localhost:8983/solr/books)

### Volumes
- `zookeeper_data`: ZooKeeper data persistence
- `zookeeper_logs`: ZooKeeper logs
- `solr1_data`, `solr2_data`, `solr3_data`: Solr node data persistence

## Troubleshooting

### Check Container Status
```bash
docker-compose ps
```

### View Logs
```bash
# All services
docker-compose logs

# Specific service
docker-compose logs solr1
docker-compose logs solr-client
```

### Access Solr Admin UI
- Node 1: http://localhost:8983/solr
- Node 2: http://localhost:8984/solr  
- Node 3: http://localhost:8985/solr

### Clean Up
```bash
# Stop all containers
docker-compose down

# Remove volumes (data will be lost)
docker-compose down -v

# Remove images
docker-compose down --rmi all
```

## Development

### Rebuild Client Only
```bash
docker-compose build solr-client
docker-compose up solr-client
```

### Run Client Interactively
```bash
docker-compose run --rm solr-client bash
```
