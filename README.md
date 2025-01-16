# Overview

This is a Spring Boot WebFlux application that sends emails through two different providers (e.g., Mailgun and SendGrid). If the primary provider fails (due to an error or timeout), the service automatically retries the next provider in the list.

### Problem Statement

1. We need to send emails (plain-text only) to multiple recipients (supporting To, CC, and BCC).
2. Failover: If one email service is down or unresponsive, the system should seamlessly fall back to another provider.
3. No third-party Mailgun/SendGrid client libraries are allowed. We must make handcrafted HTTP requests using a simple HTTP client (Spring’s WebClient).
4. Minimal authentication is required for this exercise (none for the REST endpoint), but we handle authentication to the email providers themselves (via API keys).
5. Validation is needed to ensure the request body is well-formed (valid email addresses, non-empty subject/body, etc.).

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

### Response (on success):
`"Email request accepted."`

### Error Response (e.g., invalid email)
`
{
  "error": "Validation Failed",
  "details": [
  "to: must be a well-formed email address (rejected value: rgylan74@@gmail.com)"
  ]
}
`