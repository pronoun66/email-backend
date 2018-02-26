package com.siteminder.email.controller;

import com.siteminder.email.model.Email;
import com.siteminder.email.model.EmailHandlerResult;
import com.siteminder.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;

    public EmailController() {
    }

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email")
    public ResponseEntity<EmailHandlerResult> sendEmail(@RequestBody Email email) {
        EmailHandlerResult emailSendingResult = emailService.send(email);
        return new ResponseEntity<>(emailSendingResult, emailSendingResult.getType().getHttpStatus());
    }
}
