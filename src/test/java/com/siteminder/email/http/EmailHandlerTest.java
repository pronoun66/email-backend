package com.siteminder.email.http;

import com.siteminder.email.model.EmailHandlerResult;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class EmailHandlerTest {

    private EmailHandler emailHandler = new MockEmailHandler();
    private HttpResponse httpResponse;

    @BeforeEach
    void beforeEach() throws IOException {
        this.httpResponse = mock(HttpResponse.class);
        HttpEntity httpEntity = mock(HttpEntity.class);
        InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };
        doReturn(inputStream).when(httpEntity).getContent();
        doReturn(httpEntity).when(this.httpResponse).getEntity();
    }

    @Test
    void convertResponseToEmailResult_ShouldReturnSuccessWhenOK() throws IOException {

        // Before
        StatusLine statusLine = mock(StatusLine.class);
        doReturn(statusLine).when(this.httpResponse).getStatusLine();
        doReturn(HttpStatus.OK.value()).when(statusLine).getStatusCode();

        // When
        EmailHandlerResult emailHandlerResult =
                this.emailHandler.convertResponseToEmailResult(EmailHandler.Type.Mailgun, httpResponse);

        // Verify
        assertEquals(EmailHandlerResult.Type.SUCCESS, emailHandlerResult.getType());
    }

    @Test
    void convertResponseToEmailResult_ShouldReturnSuccessWhenBadRequest() throws IOException {

        // Before
        StatusLine statusLine = mock(StatusLine.class);
        doReturn(statusLine).when(this.httpResponse).getStatusLine();
        doReturn(HttpStatus.BAD_REQUEST.value()).when(statusLine).getStatusCode();

        // When
        EmailHandlerResult emailHandlerResult =
                this.emailHandler.convertResponseToEmailResult(EmailHandler.Type.Mailgun, httpResponse);

        // Verify
        assertEquals(EmailHandlerResult.Type.ILLEGAL_ARGUMENT, emailHandlerResult.getType());
    }

    @Test
    void convertResponseToEmailResult_ShouldReturnSuccessWhenInternalServerError() throws IOException {

        // Before
        StatusLine statusLine = mock(StatusLine.class);
        doReturn(statusLine).when(this.httpResponse).getStatusLine();
        doReturn(HttpStatus.INTERNAL_SERVER_ERROR.value()).when(statusLine).getStatusCode();

        // When
        EmailHandlerResult emailHandlerResult =
                this.emailHandler.convertResponseToEmailResult(EmailHandler.Type.Mailgun, httpResponse);

        // Verify
        assertEquals(EmailHandlerResult.Type.FAIL, emailHandlerResult.getType());
    }

}