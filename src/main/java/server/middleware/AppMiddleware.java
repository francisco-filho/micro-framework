package server.middleware;

import server.App;

/**
 * Created by francisco on 25/03/16.
 */
public interface AppMiddleware extends Middleware {
    public void init(App app);
}
