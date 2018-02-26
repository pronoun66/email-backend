package com.siteminder.email.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siteminder.email.model.Email;
import com.siteminder.email.model.EmailHandlerResult;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class EmailHandler {

    private ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(EmailHandler.class);

    enum Type {
        Mailgun,
        SendGrid,
        ;
    }

    /**
     * Send email via underlying email handler
     * @param email Email
     * @return emailHandlerResult EmailHandlerResult
     * @throws IOException
     */
    public abstract EmailHandlerResult send(Email email) throws IOException;


    /**
     * Convert HttpResponse into EmailHandlerResult
     * @param type
     * @param response
     * @return
     * @throws IOException
     */
    protected EmailHandlerResult convertResponseToEmailResult(Type type, HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        logger.info(type.toString() + " " + response.getStatusLine().getStatusCode() +": " + result);

        if (response.getStatusLine().getStatusCode() < 300) {
            return new EmailHandlerResult(EmailHandlerResult.Type.SUCCESS);
        } else if (response.getStatusLine().getStatusCode() < 500) {
            if (result.length() > 0) {
                JsonNode jsonNode = objectMapper.readTree(result.toString());
                return new EmailHandlerResult(EmailHandlerResult.Type.ILLEGAL_ARGUMENT, jsonNode.toString());
            } else {
                return new EmailHandlerResult(EmailHandlerResult.Type.ILLEGAL_ARGUMENT);
            }
        } else {
            return new EmailHandlerResult(EmailHandlerResult.Type.FAIL);
        }
    }
}
