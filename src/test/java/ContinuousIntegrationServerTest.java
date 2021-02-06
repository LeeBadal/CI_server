
import javax.servlet.http.HttpServletRequest;


import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ContinuousIntegrationServerTest {

    private HttpServletRequest request;
    @BeforeEach
    void setUp() {
        request = new Mockito().mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-GitHub-Event")).thenReturn("push");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void validateRequestTrue() {
        ContinuousIntegrationServer CIS = new ContinuousIntegrationServer();
        try {
            assertTrue(CIS.validateRequest(request) instanceof JSONObject);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


}