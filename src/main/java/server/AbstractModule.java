package server;


import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Created by francisco on 14/04/16.
 */
public abstract class AbstractModule {

    protected String routeName;

    private AppRouter appRouter = new AppRouter();

    protected AbstractModule app = this;

    public AbstractModule(){}

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

    /*public void init(String name){
        if (!name.startsWith("/")) name = "/"+name;
        this.routeName = name;
        this.routes();
    }*/

    public abstract void setup(App app);

    //public abstract void routes();

    public Map<String, List<Route>> getRoutes(){
        return appRouter.routes;
    }
}
