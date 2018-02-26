package com.siteminder.email.controller;

import com.siteminder.email.model.EmailHandlerResult;
import com.siteminder.email.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailControllerTest {
    @Test
    void retrieveCoursesForStudent() {
        // Before
        EmailService emailService = mock(EmailService.class);
        EmailHandlerResult emailHandlerResult = new EmailHandlerResult(EmailHandlerResult.Type.SUCCESS, "message");
        doReturn(emailHandlerResult).when(emailService).send(any());
        EmailController emailController = new EmailController(emailService);

        // When
        ResponseEntity<EmailHandlerResult> resultResponseEntity = emailController.sendEmail(any());

        // Verify
        verify(emailService).send(any());

        assertEquals(emailHandlerResult, resultResponseEntity.getBody());
        assertEquals(HttpStatus.OK.value(), resultResponseEntity.getStatusCodeValue());
    }
}