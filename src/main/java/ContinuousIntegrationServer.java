import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.File;
import java.io.IOException;
import java.net.*;
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
        try {
            //1st validate
            requestInfo = validateRequest(request);
            if(requestInfo==null) return;

            //2nd clone repo
             localRepo = cloneProject("Git-Https-String", "branch");
            if (localRepo == null) return;

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        // 3nd compile the code
        buildProject(localRepo);

        // 4rd testProject
        testProject(new File("path")); //TODO: add path.

        // 5th Notify status on browser
        //notifyBrowser();


        //TODO: move to notifyBrowser method.
        response.getWriter().println("CI job done");
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

    private void buildProject(File testFile) {
        /*
            TODO: Unimplemented method.
            Builds the project from the a file.
        */
    }

    private void testProject(File projectFile) {
        /*
            TODO: Unimplemented method.
            The building of the project will create a folder containing the test results which needs to be parsed in this method.
        */
    }


    private void notifyBrowser() throws IOException, InterruptedException {
        String gitTargetURL;
        JSONObject commitStatus;

        /*
            TODO: Unimplemented method.
            Updates the browsers content with necessary information according to the lab description.
        */
        //TODO set variables
        //GitHubNotification.setStatus(commitStatus,gitTargetURL);

    }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}