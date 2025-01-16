package com.rgy.email.provider;

import com.rgy.email.model.EmailRequest;
import reactor.core.publisher.Mono;

public interface EmailProvider {
    /**
     * Sends an email using the respective provider.
     *
     * @param emailRequest the email request with subject, body, recipients, etc.
     * @return Mono<Void> indicating success or an error.
     */
    Mono<Void> sendEmail(EmailRequest emailRequest);

}
