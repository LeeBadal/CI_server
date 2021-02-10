
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    /**
     * Checks if the generated object is a JSONObject.
     */
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

    /**
     * Checks if the generated JSONObject is not null.
     * @throws IOException
     * @throws ParseException
     */
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
        JSONObject repo = new JSONObject();
        repo.put("full_name","LeeBadal/CI_webhook");

        JSONObject headCommit = new JSONObject();
        headCommit.put("id","ff674cb9a662dd565040618ee8a9cb3031d4a2f3"); //sha

        JSONObject object = new JSONObject();
        object.put("repository",repo);
        object.put("head_commit", headCommit);
        String token = "5e94ab893ade18b1304dc04dc41f0e384b94be5f";
        ContinuousIntegrationServer CIS = new ContinuousIntegrationServer();
        String gitTargetURL = "https://api.github.com/repos/LeeBadal/CI_webhook/statuses/ff674cb9a662dd565040618ee8a9cb3031d4a2f3?access_token=5e94ab893ade18b1304dc04dc41f0e384b94be5f";
        assertEquals(gitTargetURL, CIS.createURL(object, token));
    }

    ///Check that createJSONlog sets status to fail when there are errors, and that the log is correct.
    @Test
    void readLogFileFailTest() throws IOException {
        ContinuousIntegrationServer CIS = new ContinuousIntegrationServer();
        String log = "[INFO] Scanning for projects...\n[ERROR] Failed to execute goal\n[ERROR] -> [Help 1]\n";
        BufferedReader reader = new BufferedReader(new StringReader(log));
        JSONObject logObject = CIS.createJSONLog(reader);
        assertEquals("fail", logObject.get("status"));
        assertEquals(log, logObject.get("log"));
    }

    //Check that createJSONlog sets status to pass when there are no errors, and that the log is correct.
    @Test
    void readLogFilePassTest() throws IOException {
        ContinuousIntegrationServer CIS = new ContinuousIntegrationServer();
        String log = "[INFO] Scanning for projects...\n[INFO] BUILD SUCCESS\n[INFO] Total time:  4.306 s\n";
        BufferedReader reader = new BufferedReader(new StringReader(log));
        JSONObject logObject = CIS.createJSONLog(reader);
        assertEquals("pass", logObject.get("status"));
        assertEquals(log, logObject.get("log"));
    }

    //Test that the createStatus method returns the correct object when the tests fail.
    @Test
    void createStatusTestFailureTest() {
        ContinuousIntegrationServer CIS = new ContinuousIntegrationServer();
        JSONObject testObj = new JSONObject();
        String inputStatus = "test_failure";
        testObj.put("state", "failure");
        testObj.put("description", "The commit failed at the test stage.");
        assertEquals(testObj, CIS.createStatus(inputStatus));
    }

    //Test that cleanup from clone & build works and that files "Git" and log.txt is removed
    @Test
    void cleanUpFromCloneAndBuildTrue() throws IOException {
        new File("Git").mkdir();
        new File("log.txt").createNewFile();
        ContinuousIntegrationServer CIS = new ContinuousIntegrationServer();
        CIS.cleanUpFromCloneAndBuild();
        assertFalse(Files.exists(Paths.get("Git")));
        assertFalse(Files.exists(Paths.get("log.txt")));
    }
    //Test that the readFirstLineOfFile works
    @Test
    void readFirstLineOfFileCorrect() {
        File testFile = new File("test.txt");
        String testString = "Testing.";
        try {
            FileWriter fWriter = new FileWriter(testFile);
            fWriter.write(testString);
            fWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ContinuousIntegrationServer CIS = new ContinuousIntegrationServer();
        try {
            assertEquals(testString, CIS.readFirstLineOfFile("test.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //delete the test file
        testFile.delete();

    }
}