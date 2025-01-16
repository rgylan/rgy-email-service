package com.rgy.email.controller;

import com.rgy.email.model.EmailRequest;
import com.rgy.email.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/emails")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<String> sendEmail(@Valid @RequestBody EmailRequest emailRequest) {
        return emailService.sendEmail(emailRequest)
                .then(Mono.just("Email request accepted."));
    }
}
