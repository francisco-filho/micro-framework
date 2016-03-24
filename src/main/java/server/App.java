package server;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Created by f3445038 on 21/03/16.
 */
public class App extends AbstractHandler{

    private Server server = null;
    private AppRouter appRouter = new AppRouter();

    public App(){}

    public void listen(int port) throws Exception {
        server = new Server(port);
        server.setHandler(this);
        server.start();
        server.join();
    }

    @Override
    public void handle(String s, Request jettyRequest, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        System.out.println(new Date() + " " + req.getRequestURI());

        AppResponse response = new AppResponse(res);
        AppRequest request = new AppRequest(jettyRequest);

        appRouter
                .get(request.getRequest().getRequestURI())
                .ifPresent((route) -> route.execute(request, response));

        jettyRequest.setHandled(true);
    }


    public Object get(String uri, BiFunction<AppRequest, AppResponse, Object> fn){
        return appRouter.add(new Route(uri, fn));
    }

    public void get(String uri, BiConsumer<AppRequest, AppResponse> fn){
        appRouter.add(new Route(uri, fn));
    }
}
