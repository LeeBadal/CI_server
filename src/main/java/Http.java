import org.json.simple.JSONObject;

import java.net.http.HttpClient;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


//HTTP class, contains makePost(post request url, json object)
public class Http {
    public static int makePost(String URL, JSONObject body) throws IOException, InterruptedException {
        return postRequest(URL, body);
    }



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