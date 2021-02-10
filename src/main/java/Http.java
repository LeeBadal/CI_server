import org.json.simple.JSONObject;

import java.net.http.HttpClient;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * HTTP class, contains makePost(post request url, json object) method used by notifyBrowser in CIServer class.
 */
public class Http {

    /**
     * Makes a post and calls post request.
     * @param URL
     * @param body
     * @return postRequest
     * @throws IOException
     * @throws InterruptedException
     */
    public static int makePost(String URL, JSONObject body) throws IOException, InterruptedException {
        return postRequest(URL, body);
    }


    /**
     * Creates a post request.
     * @param URL
     * @param body
     * @return an int which is a statuscode by the responserequest.
     * @throws IOException
     * @throws InterruptedException
     */
    private static int postRequest(String URL, Object body) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();


    }
}