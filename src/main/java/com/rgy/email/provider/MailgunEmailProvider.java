package com.rgy.email.provider;

import com.rgy.email.model.EmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class MailgunEmailProvider implements EmailProvider {

    private final WebClient webClient;
    private final String apiKey;
    private final String domain;
    private final String mailgunBaseUrl;

    public MailgunEmailProvider(WebClient webClient,
                                @Value("${email.mailgun.apiKey}") String apiKey,
                                @Value("${email.mailgun.domain}") String domain,
                                @Value("${email.mailgun.url}") String mailgunBaseUrl) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.domain = domain;
        this.mailgunBaseUrl = mailgunBaseUrl;
    }

    @Override
    public Mono<Void> sendEmail(EmailRequest emailRequest) {
        // Build the form data for Mailgun
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("from", "no-reply@" + domain);
        formData.addAll("to", emailRequest.getTo());

        if (emailRequest.getCc() != null && !emailRequest.getCc().isEmpty()) {
            formData.addAll("cc", emailRequest.getCc());
        }
        if (emailRequest.getBcc() != null && !emailRequest.getBcc().isEmpty()) {
            formData.addAll("bcc", emailRequest.getBcc());
        }

        formData.add("subject", emailRequest.getSubject());
        formData.add("text", emailRequest.getBody());

        String mailgunUrl = mailgunBaseUrl + "/" + domain + "/messages";

        return webClient.post()
                .uri(mailgunUrl)
                .header(HttpHeaders.AUTHORIZATION, "Basic " +
                        java.util.Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes(StandardCharsets.UTF_8)))
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                // Fail if no response within 5 seconds (example)
                .timeout(Duration.ofSeconds(5))
                // If an error occurs (timeout, HTTP 4xx/5xx, etc.), convert it to a RuntimeException
                .onErrorMap(throwable ->
                        new RuntimeException("Mailgun send failed: " + throwable.getMessage(), throwable)
                )
                // We only care about completion, not the response body
                .then();
    }

}
