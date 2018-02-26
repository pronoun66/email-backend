package com.siteminder.email.http;

import com.siteminder.email.model.Email;
import com.siteminder.email.model.EmailHandlerResult;

import java.io.IOException;

public class MockEmailHandler extends EmailHandler {
    @Override
    public EmailHandlerResult send(Email email) throws IOException {
        return null;
    }
}
