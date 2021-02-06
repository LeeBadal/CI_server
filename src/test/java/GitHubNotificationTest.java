import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class GitHubNotificationTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void setStatus200() throws IOException, InterruptedException {
        String gitTargetURL = "https://api.github.com/repos/LeeBadal/CI_webhook/statuses/ff674cb9a662dd565040618ee8a9cb3031d4a2f3";
        JSONObject testStatus = new JSONObject();
        testStatus.put("state","success");
        testStatus.put("description","This is a test commit status update");
        assertEquals(200,GitHubNotification.setStatus(testStatus,gitTargetURL));
    }

}