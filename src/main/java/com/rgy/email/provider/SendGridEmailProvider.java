package com.rgy.email.provider;

import com.rgy.email.model.EmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.stream.Collectors;

@Component
public class SendGridEmailProvider implements EmailProvider {

    private final WebClient webClient;
    private final String apiKey;
    private final String sendGridUrl;

    public SendGridEmailProvider(WebClient webClient,
                                 @Value("${email.sendgrid.apiKey}") String apiKey,
                                 @Value("${email.sendgrid.url}") String sendGridUrl) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.sendGridUrl = sendGridUrl;
    }

    @Override
    public Mono<Void> sendEmail(EmailRequest emailRequest) {
        // Construct the request payload for SendGrid
        // We are focusing on "to", "cc", "bcc", "subject", "text content"
        String jsonBody = buildRequestBody(emailRequest);

        return webClient.post()
                .uri(sendGridUrl + "/mail/send")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .retrieve()
                .bodyToMono(String.class)
                // Fail if no response within 20 seconds
                .timeout(Duration.ofSeconds(20))
                .onErrorMap(e -> new RuntimeException("SendGrid send failed: " + e.getMessage(), e))
                .then();
    }

    private String buildRequestBody(EmailRequest emailRequest) {
        // Minimal SendGrid structure:
        // {
        //   "personalizations": [{
        //     "to": [{"email": "..."}],
        //     "cc": [{"email": "..."}],
        //     "bcc": [{"email": "..."}]
        //   }],
        //   "from": {"email": "no-reply@example.com"},
        //   "subject": "...",
        //   "content": [{"type": "text/plain", "value": "..."}]
        // }

        // For simplicity, let's define everything in a raw String approach (in real code, you'd use JSON libraries).

        String toJsonArray = emailRequest.getTo().stream()
                .map(to -> String.format("{\"email\":\"%s\"}", to))
                .collect(Collectors.joining(","));

        String ccJsonArray = CollectionUtils.isEmpty(emailRequest.getCc()) ? "" :
                "\"cc\": [" +
                        emailRequest.getCc().stream()
                                .map(cc -> String.format("{\"email\":\"%s\"}", cc))
                                .collect(Collectors.joining(",")) +
                        "],";

        String bccJsonArray = CollectionUtils.isEmpty(emailRequest.getBcc()) ? "" :
                "\"bcc\": [" +
                        emailRequest.getBcc().stream()
                                .map(bcc -> String.format("{\"email\":\"%s\"}", bcc))
                                .collect(Collectors.joining(",")) +
                        "],";

        return "{"
                + "\"personalizations\": ["
                + "{"
                + "\"to\": [" + toJsonArray + "],"
                + ccJsonArray
                + bccJsonArray
                + "}"
                + "],"
                + "\"from\": {\"email\": \"rgylan74@gmail.com\"},"
                + "\"subject\": \"" + emailRequest.getSubject() + "\","
                + "\"content\": [{\"type\": \"text/plain\", \"value\": \"" + emailRequest.getBody() + "\"}]"
                + "}";
    }

}
