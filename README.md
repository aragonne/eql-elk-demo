# Exercise: Application Observability with the ELK Stack

This guide walks you step by step through installing, configuring, and using an ELK stack (Elasticsearch, Logstash, Kibana, Filebeat) to centralize and visualize logs from a Spring Boot application.

## Prerequisites

- Docker and Docker Compose installed
- Java 11+ and Maven for the Spring Boot application

## 1. Clone the project

```bash
git clone https://github.com/aragonne/eql-elk-demo.git
cd eql-elk-demo
```

## 2. Start the ELK stack

```bash
docker-compose up -d
```

Make sure the `elasticsearch`, `logstash`, `kibana`, and `filebeat` containers are running:

```bash
docker-compose ps
```

## 3. Build and run the Spring Boot application

In another terminal:

```bash
cd my-ecommerce-app
mvn package
java -jar target/*.jar
```

The application will automatically generate logs in the `logs/` folder.

## 4. Verify log collection

- Log files should appear in `my-ecommerce-app/logs/`.
- Filebeat collects these files and sends them to Logstash.
- Logstash processes and indexes the logs in Elasticsearch.

To check:

```bash
docker-compose logs filebeat
```

## 5. Access Kibana

Open your browser at: [http://localhost:5601](http://localhost:5601)

- Go to **Stack Management > kibana > index patterns**
- Create a data view with the pattern `logs-*`
- Go to **Discover** to explore your logs

## 6. Generate application logs

Test the application (endpoints, actions, errors) to generate different types of logs:

- HTTP access logs
- Business logs
- Error logs

## 7. Create visualizations in Kibana

- Number of requests per type
- Top errors
- Time-based analysis
- Custom dashboards

## 8. Use a sample application

In kibana, go to **kibana > Add Data > Sample Data** and add a new sample application.
Go inside the dashboard and explore the data.

## 9. Use the dev tools in Kibana

In Kibana, go to **Kibana > Dev Tools** and explore the data.

### Example queries

#### List all indices

```json
GET _cat/indices?v
```

#### Search for all logs

```json
GET logs-*/_search
{
  "query": {
    "match_all": {}
  },
  "size": 10
}
```

#### Search for error logs

```json
GET logs-*/_search
{
  "query": {
    "match": {
      "log_level": "ERROR"
    }
  },
  "size": 10
}
```

#### Aggregate logs per log level

```json
GET logs-*/_search
{
  "size": 0,
  "aggs": {
    "by_level": {
      "terms": {
        "field": "log_level.keyword"
      }
    }
  }
}
```

#### Search logs for a specific user

```json
GET logs-*/_search
{
  "query": {
    "match": {
      "user": "alice"
    }
  },
  "size": 10
}
```

## 10. (Optional) Troubleshooting

- Check permissions on log files
- Check Filebeat and Logstash logs if something goes wrong
- Verify path configuration in `docker-compose.yml` and `filebeat.yml`
