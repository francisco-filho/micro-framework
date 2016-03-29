package server.middleware;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import server.App;
import server.AppRequest;
import server.AppResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by francisco on 25/03/16.
 */
public class Logger extends AbstractHandler implements AppMiddleware{
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        System.out.println("Handler -> " + this.getClass().getSimpleName());
        AppRequest jettyRequest = new AppRequest(request);
        Request req = jettyRequest.getRequest();

        System.out.println(
                String.format("%s %s %s %s %s",
                        sdf.format(new Date()), req.getMethod(), httpServletResponse.getStatus(),
                        req.getContentType() != null ? req.getContentType() : "NONE",
                        req.getRequestURI()));

        request.setHandled(false);
    }

    @Override
    public void init(App app) {
        app.get("/logging", (req, res) -> {
            System.out.println("just logging..." + this.getClass().getSimpleName());
            req.end();
        });
    }

    @Override
    public String toString() {
        return Logger.class.getSimpleName();
    }
}
