package server;

import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by francisco on 21/03/16.
 */
public class AppResponse {

    HttpServletResponse response;

    public AppResponse(HttpServletResponse res){
        this.response = res;
    }

    public void write(String text){
        try {
            response.getWriter().println(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void json(Object obj){
        try {
            response.setContentType("application/json");
            response.getWriter().println(JSONValue.toJSONString(obj));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(Object obj){
        try {
            response.setContentType("text/html; charset=utf-8");
            response.getWriter().println(obj.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AppResponse status(int sts){
        response.setStatus(sts);
        return this;
    }

    public AppResponse contentType(String contentType){
        response.setContentType(contentType);
        return this;
    }

    public HttpServletResponse getHttpServletResponse(){
        return response;
    }

    public void redirectTo(String loginURI) {
        try {
            response.sendRedirect(loginURI);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
