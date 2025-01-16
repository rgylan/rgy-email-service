package com.rgy.email.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class EmailRequest {

    @NotEmpty(message = "Subject must not be empty.")
    private String subject;

    @NotEmpty(message = "Body must not be empty.")
    private String body;

    /**
     * Demonstrates how to validate a list of emails.
     */
    @NotEmpty(message = "At least one 'to' recipient is required.")
    private List<@Email(message = "Invalid recipient email format.") String> to;

    private List<@Email(message = "Invalid CC email format.") String> cc;
    private List<@Email(message = "Invalid BCC email format.") String> bcc;
}
