
import org.json.simple.JSONObject;

import java.net.http.HttpClient;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
//URL: https://api.github.com/repos/{owner}/{repo}/statuses/{sha}

public class GitHubNotification {
    public static int setStatus(JSONObject status, String URL) throws IOException, InterruptedException {
        return postRequest(URL, status);
    }

    /**
     * Posts a HTTP Post request with the given body to the given url.
     * @param URL
     * @param body
     * @return the status code of the response
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
