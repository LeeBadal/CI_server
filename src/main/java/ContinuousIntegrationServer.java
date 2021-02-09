import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            if(requestInfo==null) return;

            //2nd clone repo
             localRepo = cloneProject("Git-Https-String", "branch");
            if (localRepo == null) return;

            buildProject(localRepo);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3nd compile the code


        // 4rd testProject
        testProject(new File("path")); //TODO: add path.

        // 5th Notify status on browser

        //TODO: move to notifyBrowser method.
        response.getWriter().println("CI job done");

        cleanUpFromCloneAndBuild();

    }

    public JSONObject validateRequest(HttpServletRequest request) throws IOException, ParseException {
        if(!request.getMethod().equals("POST") || request.getHeader("X-GitHub-Event").equals(null) || !request.getHeader("X-GitHub-Event").equals("push")) return null;
        String requestData = request.getReader().lines().collect(Collectors.joining());
        JSONObject object = (JSONObject) new JSONParser().parse(requestData);

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
        String token = "de8cc35a5232329c01d24e4ce378108085968eab";

        String gitTargetURL = createURL(githubData, token);
        JSONObject commitStatus = createStatus(evaluationStatus);
        //Update Github commit status
        Http.makePost(gitTargetURL, commitStatus);
        //TODO: add additional test/build data?
    }
    /**
     * Modifies a JSONObject to include: commit SHA, link to commit, status, date, commitUser and log.
     * Makes a post request to expr.link API endpoint inserting the data in database
     * @param githubData A JSONObject based on a GitHub commit webhook.
     * @return void
     */
    private void insertDB(JSONObject githubData) throws IOException, InterruptedException {
        String targetURL = "https://expr-link.herokuapp.com/CI_Server";
        JSONObject dbData = new JSONObject();
        //TODO fill dbData with data, see HttpTest for requirements
        Http.makePost(targetURL,githubData);
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
        String sha = (String) requestInfo.get("sha");
        Object repoName = requestInfo.get("name");
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

    /**
     * Cleans up the repo from "log.txt" and "Git" directory after Clone and Build methods.
     * @return void
     */
    public void cleanUpFromCloneAndBuild() throws IOException {
        Path pathToDir = Paths.get("Git/");
        if (Files.exists(pathToDir)) FileUtils.deleteDirectory(new File("Git"));
        Path pathToLog = Paths.get("log.txt");
        if (Files.exists(pathToLog)) (new File("log.txt")).delete();
    }


    // used to start the CI server in command line
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}