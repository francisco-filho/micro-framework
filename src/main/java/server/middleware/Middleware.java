package server.middleware;

import server.App;
import server.AppRequest;
import server.AppResponse;

import java.io.IOException;

/**
 * Created by francisco on 25/03/16.
 */
@FunctionalInterface
public interface Middleware {
    public boolean execute(AppRequest request, AppResponse response) throws IOException;
}
