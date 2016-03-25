package server.middleware;

import server.AppRequest;
import server.AppResponse;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by francisco on 25/03/16.
 */
public class Logger implements Middleware{
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @Override
    public boolean execute(AppRequest request, AppResponse response) {
        request.middlewares.put(this.getClass().getName(), this);
        HttpServletRequest req = request.getRequest();

        System.out.println(
                String.format("%s %s %s %s %s",
                sdf.format(new Date()), req.getMethod(), response.getHttpServletResponse().getStatus(),
                req.getContentType() != null ? req.getContentType() : "NONE",
                req.getRequestURI()));

        return true;
    }
}
