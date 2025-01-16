package com.rgy.email.service;

import com.rgy.email.exception.AllProvidersFailedException;
import com.rgy.email.model.EmailRequest;
import com.rgy.email.provider.EmailProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final List<EmailProvider> providers;

    /**
     * Inject a list of providers in the order you want to try them:
     * e.g. [MailgunEmailProvider, SendGridEmailProvider].
     */
    public EmailService(List<EmailProvider> providers) {
        this.providers = providers;
    }

    /**
     * Public method to send email. Starts with the first provider,
     * and if it fails, tries the next.
     */
    public Mono<Void> sendEmail(EmailRequest request) {
        logger.info("Starting to send email to: {}", request.getTo());
        return tryProvider(0, request)
                .doOnSuccess(ignored -> logger.info("Done processing email request for: {}", request.getTo()))
                .doOnError(e -> logger.error("Failed to send email after trying all providers: {}", e.getMessage()));
    }

    /**
     * Recursively attempts each provider. If the current provider fails,
     * we log an error and move on to the next. If we exhaust all providers,
     * we throw an AllProvidersFailedException.
     */
    private Mono<Void> tryProvider(int index, EmailRequest request) {
        if (index >= providers.size()) {
            // No remaining providers, so fail
            return Mono.error(new AllProvidersFailedException("All email providers failed."));
        }

        EmailProvider currentProvider = providers.get(index);
        logger.info("Attempting to send with provider: {}", currentProvider.getClass().getSimpleName());

        return currentProvider.sendEmail(request)
                // If the provider completes successfully, log success
                .then(Mono.defer(() -> {
                    logger.info("Email successfully sent via provider: {}", currentProvider.getClass().getSimpleName());
                    return Mono.empty();
                }))
                // If there's an error, log it and try the next provider
                .onErrorResume(e -> {
                    logger.error("Provider {} failed with error: {}", currentProvider.getClass().getSimpleName(), e.getMessage());
                    return tryProvider(index + 1, request);
                }).then();
    }
}
