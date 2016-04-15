package server;


import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Created by francisco on 14/04/16.
 */
public class AppController {

    protected String routeName;

    protected App app;

    private AppRouter appRouter = new AppRouter();

    public AppController(){}

    public void get(String uri, BiConsumer<AppRequest, AppResponse> fn){
        if (uri.startsWith("/") && routeName.endsWith("/")) {
            uri.replace("^/", "");
        }
        appRouter.add("GET", new Route(routeName + uri, fn));
    }

    public void post(String uri, BiConsumer<AppRequest, AppResponse> fn){
        if (uri.startsWith("/") && routeName.endsWith("/")) {
            uri.replace("^/", "");
        }
        appRouter.add("POST", new Route(routeName + uri, fn));
    }

    public Map<String, List<Route>> getRoutes(){
        return appRouter.routes;
    }

    public void setApp(App app) {
        this.app = app;
    }
}
