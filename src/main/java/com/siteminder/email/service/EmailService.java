package com.siteminder.email.service;

import com.siteminder.email.http.EmailHandler;
import com.siteminder.email.http.MailgunEmailHandler;
import com.siteminder.email.model.Email;
import com.siteminder.email.model.EmailHandlerResult;
import com.siteminder.email.model.EmailHandlerResult.Type;
import com.siteminder.email.type.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

@Component
public class EmailService {

    private Queue<EmailHandler> emailHandlers = new LinkedList<>();
    private boolean isChangeEmailHandlersOrder = false;

    @Autowired
    private MailgunEmailHandler mailgunEmailHandler;

    @Autowired
    private EmailHandler sendGridEmailHandler;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        emailHandlers.add(mailgunEmailHandler);
        emailHandlers.add(sendGridEmailHandler);
    }

    public EmailService() {
    }

    public EmailService(Queue<EmailHandler> emailHandlers) {
        this.emailHandlers = emailHandlers;
    }

    /**
     * Validate and send emails via one of emailHandlers. It chooses emailHandler which is called successfully last
     * time. When chosen emailHandler is failed to call, it will choose to next one until successful call or
     * all emailHandlers are failed.
     * @param email
     * @return
     */
    public EmailHandlerResult send(Email email) {
        try {
            validEmail(email);
        } catch (IllegalArgumentException e) {
            return new EmailHandlerResult(Type.ILLEGAL_ARGUMENT, e.getMessage());
        }

        for (int i = 0; i < emailHandlers.size(); i++) {
            try {
                EmailHandlerResult result = emailHandlers.peek().send(email);
                if (result.getType().getHttpStatus().equals(HttpStatus.OK)) {
                    return result;
                } else {
                    changeEmailHandlersOrders();
                }
            } catch (IOException e) {
                changeEmailHandlersOrders();
            }
        }

        return new EmailHandlerResult(Type.FAIL, ErrorType.REQUEST_FAILED.getMessage());
    }

    /**
     * Allow change once in the same period of time
     */
    private void changeEmailHandlersOrders() {
        if (!isChangeEmailHandlersOrder) {
            synchronized(this) {
                if (!isChangeEmailHandlersOrder) {
                    isChangeEmailHandlersOrder = true;
                    EmailHandler emailHandler = emailHandlers.poll();
                    if (emailHandler != null) {
                        emailHandlers.add(emailHandler);
                    }
                    isChangeEmailHandlersOrder = false;
                }
            }
        }
    }

    private void validEmail(Email email) {
        if (email.getFrom() == null || !validateEmail(email.getFrom())) {
            throw new IllegalArgumentException(ErrorType.FROM_EMPTY_OR_INVALID.getMessage());
        }

        if (email.getTo().size() == 0 || !validateEmails(email.getTo())) {
            throw new IllegalArgumentException(ErrorType.TO_EMPTY_OR_INVALID.getMessage());
        }

        if (email.getCc().size() != 0 && !validateEmails(email.getCc())) {
            throw new IllegalArgumentException(ErrorType.CC_INVALID.getMessage());
        }

        if (email.getBcc().size() != 0 && !validateEmails(email.getBcc())) {
            throw new IllegalArgumentException(ErrorType.BCC_INVALID.getMessage());
        }

        if (email.getSubject() == null) {
            throw new IllegalArgumentException(ErrorType.SUBJECT_EMPTY.getMessage());
        }

        if (email.getContent() == null) {
            throw new IllegalArgumentException(ErrorType.CONTENT_EMPTY.getMessage());
        }
    }

    private boolean validateEmail(String value) {
        return Pattern.compile(".+@.+").matcher(value).matches();
    }

    private boolean validateEmails(List<String> values) {
        for (int i = 0; i < values.size(); i++) {
            if (!validateEmail(values.get(i))) {
                return false;
            }
        }
        return true;
    }
}
