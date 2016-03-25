package server.middleware;

import server.AppRequest;
import server.AppResponse;

/**
 * Created by francisco on 25/03/16.
 */
@FunctionalInterface
public interface Middleware {
    public boolean execute(AppRequest request, AppResponse response);
}
