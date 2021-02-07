import org.json.simple.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GitHubNotificationTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void setStatus201() throws IOException, InterruptedException {
        String gitTargetURL = "https://api.github.com/repos/LeeBadal/CI_webhook/statuses/ff674cb9a662dd565040618ee8a9cb3031d4a2f3?access_token=5e94ab893ade18b1304dc04dc41f0e384b94be5f";
        //String gitTargetURL = "https://api.github.com/repos/LeeBadal/CI_webhook/statuses/ff674cb9a662dd565040618ee8a9cb3031d4a2f3";
        JSONObject testStatus = new JSONObject();

        testStatus.put("state","success");
        testStatus.put("description","This is a test commit status update");
        assertEquals(201,GitHubNotification.setStatus(testStatus,gitTargetURL));

    }

    @Test
    void setStatusFailure() throws IOException, InterruptedException {
        String gitTargetURL = "https://api.github.com/repos/LeeBadal/CI_webhook/statuses/ff674cb9a662dd565040618ee8a9cb3031d4a2f3?access_token=5e94ab893ade18b1304dc04dc41f0e384b94be5f";
        JSONObject testStatus = new JSONObject();

        testStatus.put("state","failure");
        testStatus.put("description","This is a test commit status update");
        assertEquals(201,GitHubNotification.setStatus(testStatus,gitTargetURL));

    }
}