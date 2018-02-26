package com.siteminder.email.service;

import com.siteminder.email.http.EmailHandler;
import com.siteminder.email.model.Email;
import com.siteminder.email.model.EmailHandlerResult;
import com.siteminder.email.type.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private EmailService emailService;
    private EmailHandler emailHandler1;
    private EmailHandler emailHandler2;
    private Email email;

    @BeforeEach
    void beforeEach() {
        this.emailHandler1 = mock(EmailHandler.class);
        this.emailHandler2 = mock(EmailHandler.class);
        Queue<EmailHandler> emailHandlerQueue = new LinkedList<>();
        emailHandlerQueue.add(emailHandler1);
        emailHandlerQueue.add(emailHandler2);
        this.emailService = new EmailService(emailHandlerQueue);

        this.email = new Email();
        this.email.setFrom("user@gmail.com");
        this.email.setTo(Arrays.asList("user@gmail.com"));
        this.email.setSubject("subject");
        this.email.setContent("content");
    }

    @Test
    void send_ShouldCallFirstEmailHandler() throws IOException {
        // Before
        doReturn(new EmailHandlerResult(EmailHandlerResult.Type.SUCCESS)).when(this.emailHandler1).send(any());

        // When
        EmailHandlerResult emailHandlerResult = this.emailService.send(this.email);

        // Verify
        assertEquals(EmailHandlerResult.Type.SUCCESS, emailHandlerResult.getType());

        ArgumentCaptor<Email> argument = ArgumentCaptor.forClass(Email.class);
        verify(this.emailHandler1, times(1)).send(argument.capture());
        assertEquals(this.email, argument.getValue());

        verify(this.emailHandler2, times(0)).send(any());
    }

    @Test
    void send_ShouldCallSecondEmailHandlerWhenFirstFailed() throws IOException {
        // Before
        doReturn(new EmailHandlerResult(EmailHandlerResult.Type.FAIL)).when(this.emailHandler1).send(any());
        doReturn(new EmailHandlerResult(EmailHandlerResult.Type.SUCCESS)).when(this.emailHandler2).send(any());
        ArgumentCaptor<Email> argument = ArgumentCaptor.forClass(Email.class);

        // When
        EmailHandlerResult emailHandlerResult = this.emailService.send(this.email);

        // Verify
        assertEquals(EmailHandlerResult.Type.SUCCESS, emailHandlerResult.getType());

        verify(this.emailHandler1, times(1)).send(any());

        verify(this.emailHandler2, times(1)).send(argument.capture());
        assertEquals(this.email, argument.getValue());

        // When
        emailHandlerResult = this.emailService.send(this.email);

        // Verify
        assertEquals(EmailHandlerResult.Type.SUCCESS, emailHandlerResult.getType());

        verify(this.emailHandler1, times(1)).send(any());

        verify(this.emailHandler2, times(2)).send(argument.capture());
        assertEquals(this.email, argument.getValue());
    }

    @Test
    void send_ShouldReturnWhenAllEmailHandlerFailed() throws IOException {
        // Before
        doReturn(new EmailHandlerResult(EmailHandlerResult.Type.FAIL)).when(this.emailHandler1).send(any());
        doReturn(new EmailHandlerResult(EmailHandlerResult.Type.FAIL)).when(this.emailHandler2).send(any());

        // When
        EmailHandlerResult emailHandlerResult = this.emailService.send(this.email);

        // Verify
        assertEquals(EmailHandlerResult.Type.FAIL, emailHandlerResult.getType());

        verify(this.emailHandler1, times(1)).send(any());
        verify(this.emailHandler2, times(1)).send(any());

        // When
        emailHandlerResult = this.emailService.send(this.email);

        // Verify
        assertEquals(EmailHandlerResult.Type.FAIL, emailHandlerResult.getType());

        verify(this.emailHandler1, times(2)).send(any());
        verify(this.emailHandler2, times(2)).send(any());
    }

    @Test
    void send_ShouldReturnIllegalArgumentWhenNoFrom() throws IOException {
        // Before
        this.email.setFrom(null);

        // When
        EmailHandlerResult emailHandlerResult = this.emailService.send(this.email);

        // Verify
        assertEquals(EmailHandlerResult.Type.ILLEGAL_ARGUMENT, emailHandlerResult.getType());
        assertEquals(ErrorType.FROM_EMPTY_OR_INVALID.getMessage(), emailHandlerResult.getMessage());
    }

    @Test
    void send_ShouldReturnIllegalArgumentWhenFromIsIllegal() throws IOException {
        // Before
        this.email.setFrom("@com");

        // When
        EmailHandlerResult emailHandlerResult = this.emailService.send(this.email);

        // Verify
        assertEquals(EmailHandlerResult.Type.ILLEGAL_ARGUMENT, emailHandlerResult.getType());
        assertEquals(ErrorType.FROM_EMPTY_OR_INVALID.getMessage(), emailHandlerResult.getMessage());
    }

    @Test
    void send_ShouldReturnIllegalArgumentWhenNoTo() throws IOException {
        // Before
        this.email.setTo(new ArrayList<>());

        // When
        EmailHandlerResult emailHandlerResult = this.emailService.send(this.email);

        // Verify
        assertEquals(EmailHandlerResult.Type.ILLEGAL_ARGUMENT, emailHandlerResult.getType());
        assertEquals(ErrorType.TO_EMPTY_OR_INVALID.getMessage(), emailHandlerResult.getMessage());
    }

    @Test
    void send_ShouldReturnIllegalArgumentWhenToIsIllegal() throws IOException {
        // Before
        this.email.setTo(Arrays.asList("@com"));

        // When
        EmailHandlerResult emailHandlerResult = this.emailService.send(this.email);

        // Verify
        assertEquals(EmailHandlerResult.Type.ILLEGAL_ARGUMENT, emailHandlerResult.getType());
        assertEquals(ErrorType.TO_EMPTY_OR_INVALID.getMessage(), emailHandlerResult.getMessage());
    }

    @Test
    void send_ShouldReturnIllegalArgumentWhenCcIsIllegal() throws IOException {
        // Before
        this.email.setCc(Arrays.asList("@com"));

        // When
        EmailHandlerResult emailHandlerResult = this.emailService.send(this.email);

        // Verify
        assertEquals(EmailHandlerResult.Type.ILLEGAL_ARGUMENT, emailHandlerResult.getType());
        assertEquals(ErrorType.CC_INVALID.getMessage(), emailHandlerResult.getMessage());
    }

    @Test
    void send_ShouldReturnIllegalArgumentWhenBccIsIllegal() throws IOException {
        // Before
        this.email.setBcc(Arrays.asList("@com"));

        // When
        EmailHandlerResult emailHandlerResult = this.emailService.send(this.email);

        // Verify
        assertEquals(EmailHandlerResult.Type.ILLEGAL_ARGUMENT, emailHandlerResult.getType());
        assertEquals(ErrorType.BCC_INVALID.getMessage(), emailHandlerResult.getMessage());
    }

    @Test
    void send_ShouldReturnIllegalArgumentWhenNoSubject() throws IOException {
        // Before
        this.email.setSubject(null);

        // When
        EmailHandlerResult emailHandlerResult = this.emailService.send(this.email);

        // Verify
        assertEquals(EmailHandlerResult.Type.ILLEGAL_ARGUMENT, emailHandlerResult.getType());
        assertEquals(ErrorType.SUBJECT_EMPTY.getMessage(), emailHandlerResult.getMessage());
    }

    @Test
    void send_ShouldReturnIllegalArgumentWhenNoContent() throws IOException {
        // Before
        this.email.setContent(null);

        // When
        EmailHandlerResult emailHandlerResult = this.emailService.send(this.email);

        // Verify
        assertEquals(EmailHandlerResult.Type.ILLEGAL_ARGUMENT, emailHandlerResult.getType());
        assertEquals(ErrorType.CONTENT_EMPTY.getMessage(), emailHandlerResult.getMessage());
    }
}