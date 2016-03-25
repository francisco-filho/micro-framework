package server;

import org.eclipse.jetty.server.Request;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AppRouter {

    Map<String, List<Route>> routes = new HashMap<>();
    private final String[] acceptedMethods  = new String[]{"GET", "POST", "PUT", "DELETE"};

    public AppRouter(){
        for (String method : acceptedMethods) {
            routes.put(method, new LinkedList<>());
        }
    }

    public AppRouter add(String method, Route route){
        routes.get(method).add(route);
        return this;
    }

    public Route getRoute(Request request){
        List<Route> routeList = routes.get(request.getMethod().trim());
        for(Route route: routeList){
            if (route.test(request.getRequestURI())){
                return route;
            }
        }
        return null;
    }
}
