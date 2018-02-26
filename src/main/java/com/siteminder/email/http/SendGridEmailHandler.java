package com.siteminder.email.http;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siteminder.email.config.ApplicationProperties;
import com.siteminder.email.model.Email;
import com.siteminder.email.model.EmailHandlerResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SendGridEmailHandler extends EmailHandler {

    @Autowired
    private ApplicationProperties applicationProperties;

    private String serverUrl;
    private String key;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Type type = Type.SendGrid;
    private int httpClientTimeout;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        assert (applicationProperties.getProperty("sendgrid.serverUrl") != null);
        assert (applicationProperties.getProperty("sendgrid.key") != null);

        this.serverUrl = applicationProperties.getProperty("sendgrid.serverUrl");
        this.key = applicationProperties.getProperty("sendgrid.key");
        this.objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

        if (applicationProperties.getProperty("http.client.timeout") != null) {
            this.httpClientTimeout = Integer.valueOf(applicationProperties.getProperty("http.client.timeout"));
        }
    }

    @Override
    public EmailHandlerResult send(Email email) throws IOException {
        String requestUrl = this.serverUrl + "/v3/mail/send";

        RequestConfig config = RequestConfig.custom().setSocketTimeout(this.httpClientTimeout).build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        HttpPost post = new HttpPost(requestUrl);
        setPostJsonEntity(post, email);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Authorization", "Bearer " + this.key);

        HttpResponse response = client.execute(post);
        return convertResponseToEmailResult(this.type, response);
    }

    /**
     * setPostJsonEntity (Content-Type: application/json)
     * {
     *   "personalizations": [
     *     {
     *       "to": [
     *         {
     *           "email": "john.doe@example.com",
     *           "name": "John Doe"
     *         }
     *       ]
     *     }
     *   ],
     *   "from": {
     *     "email": "jerry@podtrackers.com",
     *     "name": "Sam Smith"
     *   },
     *   "cc": [
     *     {
     *       "email": "jerry@podtrackers.com",
     *       "name": "Sam Smith"
     *     }
     *   ],
     *   "bcc": [
     *     {
     *       "email": "jerry@podtrackers.com",
     *       "name": "Sam Smith"
     *     }
     *   ],
     *   "subject": "test",
     *   "content": [
     *     {
     *       "type": "text/plain",
     *       "value": "content"
     *     }
     *   ]
     * }
     *
     * @param post
     * @param email
     * @throws UnsupportedEncodingException
     * @throws JsonProcessingException
     */
    private void setPostJsonEntity(HttpPost post, Email email) throws UnsupportedEncodingException, JsonProcessingException {
        Map<String, Object> jsonMap = new HashMap<>();
        Map<String, Object> personalizationsMap = new HashMap<>();
        List<Map<String, String>> toList = new ArrayList<>();
        for (int i = 0; i < email.getTo().size(); i++) {
            toList.add(emailValueToMap(email.getTo().get(i)));
        }
        personalizationsMap.put("to", toList);
        List<Map<String, Object>> personalizationList = new ArrayList<>();
        personalizationList.add(personalizationsMap);
        jsonMap.put("personalizations", personalizationList);

        jsonMap.put("from", emailValueToMap(email.getFrom()));

        if (email.getCc().size() > 0) {
            List<Map<String, String>> ccList = new ArrayList<>();
            for (int i = 0; i < email.getCc().size(); i++) {
                ccList.add(emailValueToMap(email.getCc().get(i)));
            }
            jsonMap.put("cc", ccList);
        }

        if (email.getBcc().size() > 0) {
            List<Map<String, String>> bccList = new ArrayList<>();
            for (int i = 0; i < email.getBcc().size(); i++) {
                bccList.add(emailValueToMap(email.getBcc().get(i)));
            }
            jsonMap.put("cc", bccList);
        }

        jsonMap.put("subject", email.getSubject());

        List<Map<String, String>> contentList = new ArrayList<>();
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("type", "text/plain");
        contentMap.put("value", email.getContent());
        contentList.add(contentMap);

        jsonMap.put("content", contentList);

        String json = objectMapper.writeValueAsString(jsonMap);
        post.setEntity(new StringEntity(
                json,
                ContentType.APPLICATION_JSON)
        );
    }

    private Map<String, String> emailValueToMap(String value) {
        Map<String, String> map = new HashMap<>();
        if (value.contains(" ")) {
            String[] array = value.split(" ");
            map.put("name", array[0]);
            map.put("email", array[1]);
        } else {
            map.put("email", value);
        }
        return map;
    }
}
