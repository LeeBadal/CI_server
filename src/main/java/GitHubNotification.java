
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

    //Token: 5e94ab893ade18b1304dc04dc41f0e384b94be5f

    private static int postRequest(String URL, Object body) throws IOException, InterruptedException {

        String token = "5e94ab893ade18b1304dc04dc41f0e384b94be5f";

        //String url = "https://api.github.com/repos/LeeBadal/CI-server/statuses/{sha}";

        // HttpClient Method to get Private Github content with Basic OAuth token
        //getGithubContentUsingHttpClient(token, url);

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