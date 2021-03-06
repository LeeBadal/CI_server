import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;


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
        if(request.getMethod().equals("GET")) response.sendRedirect("http://expr.link/builds/list/all");
        else {
            try {
                requestInfo = validateRequest(request);
                JSONObject ciResults = new JSONObject();

                if (requestInfo == null) return;
                if (requestInfo.get("head_commit") == null) return;

                 //Unpack requestInfo to strings used in cloneProject
                String git_https = (String) ((JSONObject) requestInfo.get("repository")).get("clone_url");
                String ref = (String) requestInfo.get("ref");
                String branch = ref.substring(ref.lastIndexOf("/") + 1);

                localRepo = cloneProject(git_https, branch);
                if (localRepo == null) return;

                notifyBrowser(requestInfo, "pending");
                buildAndTestProject(localRepo);
                ciResults = readLogFile();
                insertDB(requestInfo, ciResults);
                notifyBrowser(requestInfo, ciResults.get("state").toString());
                cleanUpFromCloneAndBuild();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (GitAPIException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the first line of a file as a String.
     * @param filePath the path to the file.
     * @return the first line of the file as a String
     * @throws IOException
     */
    public String readFirstLineOfFile(String filePath) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
        return fileReader.readLine();
    }

    /**
     * Checks and parses the data from the webhook into a JSON object.
     * @param request
     * @return JSONObject is an object with all the parsed data.
     * @throws IOException
     * @throws ParseException
     */
    public JSONObject validateRequest(HttpServletRequest request) throws IOException, ParseException {
        if(!request.getMethod().equals("POST") || request.getHeader("X-GitHub-Event").equals(null) || !request.getHeader("X-GitHub-Event").equals("push")) return null;
        String requestData = request.getReader().lines().collect(Collectors.joining());
        JSONObject object = (JSONObject) new JSONParser().parse(requestData);
        return object;
    }

    /**
     * Clones the project from Github into the folder Git.
     * @param git_https is the string to the git repository.
     * @param branch is the string to the branch name.
     * @return cloned repository from git.
     * @throws GitAPIException
     * @throws IOException
     */
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

    /**
     * Builds the project and puts the result in a log.txt file in the root folder of the project.
     * @param file the cloned repository.
     * @throws IOException
     * @throws InterruptedException
     */
    public void buildAndTestProject(File file) throws IOException, InterruptedException {
        String path = file.getAbsolutePath();
        Runtime.getRuntime().exec("mvn -f " + path + " test --log-file log.txt").waitFor();
    }

    /**
     * Updates the status of the commit for the git repository.
     * @param githubData is data from the webhook created by the commit
     * @param evaluationStatus The status of the build.
     * @throws IOException
     * @throws InterruptedException
     */
    private void notifyBrowser(JSONObject githubData, String evaluationStatus) throws IOException, InterruptedException {

        String token = readFirstLineOfFile("token.txt");
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
            case "error":
                object.put("state","error");
                object.put("description", "The CI server has experienced an error.");
                break;
            default:
                object.put("state","pending");
                object.put("description","CI test status unknown.");
        }
        return object;
    }

    /**
     * Method for reading the log-file and creating a JSONObject with the results
     * @return JSONObject with log
     * @throws IOException
     */
    public JSONObject readLogFile() throws IOException {
        File logFile = new File("log.txt");
        BufferedReader logReader = new BufferedReader(new FileReader(logFile));
        JSONObject logObject = createJSONLog(logReader);
        return logObject;
    }

    /**
     * Helpmethod for creating a JSONObject from the logfile.
     * @param logReader
     * @return JSONObject with log
     * @throws IOException
     */
    public JSONObject createJSONLog(BufferedReader logReader) throws IOException {
        StringBuilder log = new StringBuilder();
        String s;
        JSONObject logObject = new JSONObject();
        Boolean fail = false;
        Boolean buildSuccess = false;
        while ((s = logReader.readLine()) != null) {
            log.append(s);
            log.append("<br>");
            if (s.matches("^\\[INFO\\]  T E S T S.*") && !fail) {
                buildSuccess = true;
            }
            if(s.matches("^\\[ERROR\\].*")  && !fail) {
                fail = true;
                if (buildSuccess) {
                    logObject.put("state", "test_failure");
                } else {
                    logObject.put("state", "build_failure");
                }
            };
        }
        if (!fail) {
            logObject.put("state", "success");
        }
        logObject.put("log", log.toString());
        return logObject;
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