import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;


import junit.textui.TestRunner;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;





/**
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
 */
public class ContinuousIntegrationServer extends AbstractHandler {

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);

        JSONObject requestInfo = null;
        File localRepo = null;
        try {
            //1st validate
            requestInfo = validateRequest(request);
            JSONObject ciResults = new JSONObject();
            ciResults.put("state", "success");
            ciResults.put("log", "Logging operation successful.");
            insertDB(requestInfo, ciResults);
            notifyBrowser(requestInfo,"success");
            if(requestInfo==null) return;

            //2nd clone repo
             localRepo = cloneProject("Git-Https-String", "branch");
            if (localRepo == null) return;

            //buildProject(localRepo); //TODO: remove comment out
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3nd compile the code


        // 4rd testProject
        //testProject(new File("path")); //TODO: add path.

        // 5th Notify status on browser

        //TODO: move to notifyBrowser method.
        response.getWriter().println("CI job done");
    }

    public JSONObject validateRequest(HttpServletRequest request) throws IOException, ParseException {
        if(!request.getMethod().equals("POST") || request.getHeader("X-GitHub-Event").equals(null) || !request.getHeader("X-GitHub-Event").equals("push")) return null;
        String requestData = request.getReader().lines().collect(Collectors.joining());
        JSONObject object = (JSONObject) new JSONParser().parse(requestData);
        System.out.println("RequestInfo = " + object.toJSONString());
        return object;
    }
    public File cloneProject(String git_https, String branch) throws GitAPIException, IOException {
        File file = new File("Git");
        FileUtils.deleteDirectory(file);
        file = new File("Git");

        Git.cloneRepository()
                .setURI(git_https)
                .setBranch(branch)
                .setDirectory(file)
                .call();

        return file;
    }

    public void buildProject(File file) throws IOException, InterruptedException {
        String path = file.getAbsolutePath();
        Runtime.getRuntime().exec("mvn -f " + path + " test --log-file log.txt").waitFor();
    }

    private void testProject(File projectFile) {
        /*
            TODO: Unimplemented method.
            The building of the project will create a folder containing the test results which needs to be parsed in this method.
        */
    }

    private void notifyBrowser(JSONObject githubData, String evaluationStatus) throws IOException, InterruptedException {
        String token = "token"; //TODO: hidden variable in file
        String gitTargetURL = createURL(githubData, token);
        JSONObject commitStatus = createStatus(evaluationStatus);
        System.out.println(commitStatus);
        System.out.println(gitTargetURL);
        //Update Github commit status
        int statusCode = Http.makePost(gitTargetURL, commitStatus);
        System.out.println(statusCode);
        //TODO: add additional test/build data?
    }
    /**
     * Modifies a JSONObject to include: commit SHA, link to commit, status, date, commitUser and log.
     * Makes a post request to expr.link API endpoint inserting the data in database
     * @param githubData A JSONObject based on a GitHub commit webhook.
     * @return void
     */
    private void insertDB(JSONObject githubData, JSONObject CIData) throws IOException, InterruptedException {
        String targetURL = "https://expr-link.herokuapp.com/CI_Server";
        JSONObject body = new JSONObject();
        JSONObject commit = (JSONObject) githubData.get("head_commit");
        body.put("SHA",commit.get("id"));
        body.put("status",CIData.get("state"));
        body.put("link",commit.get("url"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z"); //https://mkyong.com/java/java-how-to-get-current-date-time-date-and-calender/
        Date date = new Date(System.currentTimeMillis());
        body.put("date", dateFormat.format(date));

        JSONObject author = (JSONObject) commit.get("author");
        body.put("commiter", author.get("username"));
        System.out.println(body.toString());
        body.put("log",CIData.get("log"));
        System.out.println(body.toString());

        //TODO fill dbData with data, see HttpTest for requirements
        Http.makePost(targetURL,body);
    }

    /**
     * Returns the URL needed to set the status of a commit on github.
     * @param requestInfo A JSONObject based on a GitHub commit webhook.
     * @param token the personal access token
     * @return the target URL for the setStatus Github API request
     */
    public String createURL(JSONObject requestInfo, String token) {
        String gitTargetURL;
        //TODO: Check - is this always the SHA for the commit or can it be something else such as the branch/pull request?
        JSONObject commit = (JSONObject) requestInfo.get("head_commit");
        String sha = (String) commit.get("id");
        JSONObject repository = (JSONObject) requestInfo.get("repository");
        String repoName = (String) repository.get("full_name");
        gitTargetURL = "https://api.github.com/repos/" + repoName + "/statuses/" + sha + "?access_token=" + token;
        return gitTargetURL;
    }

    /**
     * Create a JSONObject with state and description tags based on CI status.
     * @param ciEvaluation a string describing the results of the CI process, either "success", "build_failure" or "test_failure".
     * @return a JSONObject with state and description keys.
     */
    public JSONObject createStatus(String ciEvaluation) {
        JSONObject object = new JSONObject();
        switch (ciEvaluation) {
            case "success":
                object.put("state","success");
                object.put("description","The build and tests were successful.");
                break;
            case "build_failure":
                object.put("state","failure");
                //TODO: add extra build information if possible
                object.put("description","The commit failed at the build stage.");
                break;
            case "test_failure":
                object.put("state","failure");
                //TODO: add extra test information if possible
                object.put("description","The commit failed at the test stage.");
                break;
            default:
                object.put("state","pending");
                object.put("description","CI test status unknown.");
        }
        return object;
    }


    // used to start the CI server in command line
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}