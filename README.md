# EnrichmentMap Microservice

## Build with Maven

```
mvn clean package
```

## Build and Run with Docker

```
docker build -t enrichmentmap-service .
docker run --rm -p 8080:8080 -it enrichmentmap-service:latest
```
