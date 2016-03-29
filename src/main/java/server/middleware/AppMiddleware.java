package server.middleware;

import server.App;

/**
 * Created by francisco on 25/03/16.
 */
public interface AppMiddleware  {
    public void init(App app);
}
