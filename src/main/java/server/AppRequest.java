package server;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.session.JDBCSessionManager;
import org.eclipse.jetty.util.MultiMap;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import server.middleware.Middleware;
import util.TypedHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by francisco on 21/03/16.
 */
public class AppRequest {

    private Request request;
    private HttpServletRequest httpRequest;
    public TypedHashMap params = new TypedHashMap();
    public List<FileItem> files = new ArrayList<>();
    public Map<String, Middleware> middlewares = new HashMap<>();

    public AppRequest(Request request){
        this.request = request;

        if (true){
//        if (request.getContentType() == null){
            request.getParameterMap();
            MultiMap<String> parameters = request.getQueryParameters();
            if (parameters != null){
                parameters.forEach((k, v) -> {
                    params.put(k, v.get(0));
                });
                //params.putAll(parameters);
            }

            return;
        }

        if (request.getContentType().startsWith("application/json")){
            BufferedReader reader = null;
            StringBuffer sb = new StringBuffer();
            String line;
            try {
                JSONObject json = (JSONObject)JSONValue.parse(request.getReader());
                params.putAll(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
        if (request.getContentType().startsWith("text/html")){
            MultiMap<String> parameters = request.getQueryParameters();
            for(String key : parameters.keySet()){
                List list = (List)parameters.get(key);
                if (list.size() == 1) {
                    params.put(key, list.get(0));
                }else if (list.size() > 1){
                    params.put(key, list);
                }
            }
        }
    }

    public AppRequest(Request request, HttpServletRequest httpRequest){
        this(request);
        this.httpRequest = httpRequest;
    }

    public Request getRequest(){
        return this.request;
    }

    public HttpSession getSession() {
        return request.getSession();
    }

    public Cookie[] getCookies() {
        return request.getCookies();
    }

    public Cookie getCookie(String iPlanetDirectoryPro) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (c.getName() != null && c.getName().equals(iPlanetDirectoryPro)) {
                return c;
            }
        }
        return null;
    }

    public void end() {
        this.request.setHandled(true);
    }
}
