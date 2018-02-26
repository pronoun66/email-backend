package com.siteminder.email.http;

import com.siteminder.email.config.ApplicationProperties;
import com.siteminder.email.model.Email;
import com.siteminder.email.model.EmailHandlerResult;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


@Component
public class MailgunEmailHandler extends EmailHandler {

    @Autowired
    private ApplicationProperties applicationProperties;

    private String serverUrl;
    private Type type = Type.Mailgun;
    private int httpClientTimeout = 10000;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        assert (applicationProperties.getProperty("mailgun.serverUrl") != null);

        this.serverUrl = applicationProperties.getProperty("mailgun.serverUrl");

        if (applicationProperties.getProperty("http.client.timeout") != null) {
            this.httpClientTimeout = Integer.valueOf(applicationProperties.getProperty("http.client.timeout"));
        }
    }

    @Override
    public EmailHandlerResult send(Email email) throws IOException {
        String senderDomain = email.getFrom().split("@")[1];
        String requestUrl = this.serverUrl + "/" + senderDomain + "/messages";

        RequestConfig config = RequestConfig.custom().setSocketTimeout(this.httpClientTimeout).build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        HttpPost post = new HttpPost(requestUrl);
        setPostFormEntity(post, email);
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");

        HttpResponse response = client.execute(post);
        return convertResponseToEmailResult(this.type, response);
    }

    /**
     * setPostFormEntity (Content-Type: application/x-www-form-urlencoded)
     * form: String
     * to: [String]
     * cc: [String]
     * bcc: [String]
     * subject: String
     * test: String
     *
     * @param post
     * @param email
     * @throws UnsupportedEncodingException
     */
    private void setPostFormEntity(HttpPost post, Email email) throws UnsupportedEncodingException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("from", email.getFrom()));
        email.getTo().forEach((value) -> urlParameters.add(new BasicNameValuePair("to", value)));
        email.getCc().forEach((value) -> urlParameters.add(new BasicNameValuePair("cc", value)));
        email.getBcc().forEach((value) -> urlParameters.add(new BasicNameValuePair("bcc", value)));
        urlParameters.add(new BasicNameValuePair("subject", email.getSubject()));
        urlParameters.add(new BasicNameValuePair("text", email.getContent()));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));
    }
}
