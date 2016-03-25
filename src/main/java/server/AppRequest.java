package server;

import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.MultiMap;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import util.TypedHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by francisco on 21/03/16.
 */
public class AppRequest {

    private Request request;
    private HttpServletRequest httpRequest;
    public TypedHashMap params = new TypedHashMap();

    public AppRequest(Request request){
        this.request = request;

        if (request.getContentType() == null){
            MultiMap<String> parameters = request.getQueryParameters();
            if (parameters != null)
                params.putAll(parameters);
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

}
