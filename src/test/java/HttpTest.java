import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Random;

import static org.junit.Assert.*;
//TODO research appropiate testing method
public class HttpTest {
    Utils util = new Utils();

    @Before
    public void setUp() throws Exception {



    }

    @After
    public void tearDown() throws Exception {
    }
    /**
    Make a post request to the DB. responses is test by check code 200
    The DB insertion can be verified by going to http://www.expr.link/builds/list/all. View Date for specific test.
     **/
    @Test
    public void makePostDBInsert() throws IOException, InterruptedException {
        String targetURL = "https://expr-link.herokuapp.com/CI_Server";
        JSONObject body = new JSONObject();
        String randomString = util.generateRandomString();
        body.put("SHA",randomString);
        body.put("status","pass");
        body.put("link","https://github.com/LeeBadal/CI_webhook");
        body.put("date", Instant.now().toString());
        body.put("commiter","LeeTest");
        body.put("log","big log this is what happened yada yada");
        assertEquals(200,Http.makePost(targetURL,body));
    }




}