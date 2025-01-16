package com.rgy.email.service;

import com.rgy.email.exception.AllProvidersFailedException;
import com.rgy.email.model.EmailRequest;
import com.rgy.email.provider.EmailProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;

class EmailServiceTest {

    private EmailProvider providerA;
    private EmailProvider providerB;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        providerA = Mockito.mock(EmailProvider.class);
        providerB = Mockito.mock(EmailProvider.class);

        // Create an instance of EmailService with two providers in order
        emailService = new EmailService(List.of(providerA, providerB));
    }

    @Test
    void testSendEmail_FirstProviderSucceeds() {
        // Mock providerA to succeed
        Mockito.when(providerA.sendEmail(any())).thenReturn(Mono.empty());
        // providerB shouldn't be called in this scenario
        Mockito.when(providerB.sendEmail(any()))
                .thenReturn(Mono.error(new RuntimeException("Should not be called")));

        EmailRequest request = createEmailRequest();

        // Test
        StepVerifier.create(emailService.sendEmail(request))
                .expectComplete()
                .verify();

        // Verify that providerA was called once
        Mockito.verify(providerA, Mockito.times(1)).sendEmail(any());
        // Verify providerB was never called
        Mockito.verify(providerB, Mockito.never()).sendEmail(any());
    }

    @Test
    void testSendEmail_FirstProviderFails_SecondProviderSucceeds() {
        // Mock providerA to fail
        Mockito.when(providerA.sendEmail(any()))
                .thenReturn(Mono.error(new RuntimeException("Provider A failure")));

        // Mock providerB to succeed
        Mockito.when(providerB.sendEmail(any()))
                .thenReturn(Mono.empty());

        EmailRequest request = createEmailRequest();

        // Test
        StepVerifier.create(emailService.sendEmail(request))
                .expectComplete()
                .verify();

        // Verify providerA was called
        Mockito.verify(providerA, Mockito.times(1)).sendEmail(any());
        // Verify providerB was also called
        Mockito.verify(providerB, Mockito.times(1)).sendEmail(any());
    }

    @Test
    void testSendEmail_AllProvidersFail() {
        // Both providers fail
        Mockito.when(providerA.sendEmail(any()))
                .thenReturn(Mono.error(new RuntimeException("Provider A failure")));
        Mockito.when(providerB.sendEmail(any()))
                .thenReturn(Mono.error(new RuntimeException("Provider B failure")));

        EmailRequest request = createEmailRequest();

        // Test
        StepVerifier.create(emailService.sendEmail(request))
                .expectError(AllProvidersFailedException.class)
                .verify();

        // Verify both providers were called
        Mockito.verify(providerA, Mockito.times(1)).sendEmail(any());
        Mockito.verify(providerB, Mockito.times(1)).sendEmail(any());
    }

    private EmailRequest createEmailRequest() {
        EmailRequest request = new EmailRequest();
        request.setSubject("Test Subject");
        request.setBody("Test Body");
        request.setTo(List.of("test@example.com"));
        return request;
    }
}

