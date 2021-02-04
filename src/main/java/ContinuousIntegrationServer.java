import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.File;
import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
 */
public class ContinuousIntegrationServer extends AbstractHandler {
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);

        // 1st clone your repository
        File gitRepo = cloneProject("Git-Https-String");// TODO: git https

        // 2nd compile the code
        buildProject(gitRepo);

        // 3rd testProject
        testProject(new File("path")); //TODO: add path.

        // 4th Notify status on browser
        notifyBrowser();

        //TODO: move to notifyBrowser method.
        response.getWriter().println("CI job done");
    }

    private File cloneProject(String git_https) {
        /*
            TODO: Unimplemented method.
            Clones a repository from https and returns the result as a file.
        */
        return null;
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

    private void notifyBrowser() {
        /*
            TODO: Unimplemented method.
            Updates the browsers content with necessary information according to the lab description.
        */
    }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}