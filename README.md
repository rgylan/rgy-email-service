# Overview

A Spring Boot WebFlux application that sends email via two providers—Mailgun and SendGrid. The first provider is used by default, and if it fails, we fail over to the second provider.

### Requirements Met

1. REST API endpoint (POST /emails) that accepts JSON.
2. Reactive (Spring WebFlux).
3. Failover strategy via fallback.
4. No 3rd party libraries for Mailgun/SendGrid; we use plain WebClient.

### Setup
1. Configure your credentials in application.yml (or as environment variables).
2. Run with mvn spring-boot:run or package and run the JAR.

### Usage
`curl -X POST http://localhost:8080/emails \
-H "Content-Type: application/json" \
-d '{
    "subject": "Test Subject",
    "body": "Hello from Spring WebFlux!",
    "to": ["someone@example.com"],
    "cc": ["another@example.com"],
    "bcc": ["hidden@example.com"]
}'`

