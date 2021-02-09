
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ContinuousIntegrationServerTest {

    private HttpServletRequest request;
    @BeforeEach
    void setUp() throws IOException {
        request = new Mockito().mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-GitHub-Event")).thenReturn("push");
        BufferedReader mockReader = new BufferedReader(new StringReader("{\"ref\":\"refs/heads/find_branch\"}"));
        when(request.getReader()).thenReturn(mockReader);
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

    @Test
    void validateRequestFalse() throws IOException, ParseException {
        ContinuousIntegrationServer CIS = new ContinuousIntegrationServer();
        when(request.getMethod()).thenReturn("GET");
        assertNull(CIS.validateRequest(request));
    }

    //Testing if the cloned file exists.
    @Test
    void cloneProjectTrue() throws GitAPIException, IOException {
        ContinuousIntegrationServer CIS = new ContinuousIntegrationServer();
        File file = CIS.cloneProject("https://github.com/LeeBadal/CI_server.git", "main");
        assertNotNull(file);
    }

    //Test that the createURL method returns the correct URL
    @Test
    void createURLTestCorrect() throws ParseException {
        String objectString = "{\n" +
                "  \"id\": 12095185365,\n" +
                "  \"sha\": \"ff674cb9a662dd565040618ee8a9cb3031d4a2f3\",\n" +
                "  \"name\": \"LeeBadal/CI_webhook\",\n" +
                "  \"target_url\": null,\n" +
                "}";
        JSONObject object = (JSONObject) new JSONParser().parse(objectString);
        String token = "5e94ab893ade18b1304dc04dc41f0e384b94be5f";
        ContinuousIntegrationServer CIS = new ContinuousIntegrationServer();
        String gitTargetURL = "https://api.github.com/repos/LeeBadal/CI_webhook/statuses/ff674cb9a662dd565040618ee8a9cb3031d4a2f3?access_token=5e94ab893ade18b1304dc04dc41f0e384b94be5f";
        assertEquals(gitTargetURL, CIS.createURL(object, token));
    }

    //Test that the createStatus method returns the correct object when the tests fail.
    @Test
    void createStatusTestFailureTest() {
        ContinuousIntegrationServer CIS = new ContinuousIntegrationServer();
        JSONObject testObj = new JSONObject();
        String inputStatus = "test_failure";
        testObj.put("state","failure");
        testObj.put("description","The commit failed at the test stage.");
        assertEquals(testObj, CIS.createStatus(inputStatus));
    }

}