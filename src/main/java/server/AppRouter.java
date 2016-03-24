package server;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AppRouter {

    List<Route> routes = new LinkedList<>();

    public AppRouter(){}

    public AppRouter add(Route route){
        routes.add(route);
        return this;
    }

    public Optional<Route> get(String uri){
        for(Route route: routes){
            if (route.test(uri)){
                return Optional.of(route);
            }
        }
        return null;
    }
}
