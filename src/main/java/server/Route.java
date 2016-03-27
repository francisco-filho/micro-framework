package server;

import database.DBConnection;
import database.TriConsumer;

import javax.security.auth.login.LoginContext;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by francisco on 22/03/16.
 */
public class Route {

    private final String uri;
    private final Pattern uriPattern;
    private final Object fn;
    private final Pattern fieldPattern  = Pattern.compile(":([a-zA-Z0-9]+)");
    public final Map<String, String> requestData = new HashMap<>();

    public Route(String uri, Object fn){
        this.uri = uri;
        this.fn = fn;

        if (uri.contains(":")) {
            Matcher matcher = fieldPattern.matcher(this.uri);
            this.uriPattern = Pattern.compile(matcher.replaceAll("([a-zA-Z0-9]+)"));
        } else {
            this.uriPattern = null;
        }
    }

    public String getUri(){
        return this.uri;
    }

    public boolean test(String uriEntrada){

        if (this.uri.equals(uriEntrada)){
            return true;
        } else
        if (uriPattern != null){
            //valida URI contra regex uriPattern

            if (uriPattern.matcher(uriEntrada).matches()){
                Matcher match = uriPattern.matcher(uriEntrada);
                Matcher fields = fieldPattern.matcher(this.uri);
                int i = 1;
                if (match.find()) {
                    List<String> paramNames = new LinkedList<>();
                    while(fields.find()){
                        paramNames.add(fields.group(1));
                    }
                    for (int x = 1; x <= match.groupCount(); x++) {
                        requestData.put(paramNames.get(x - 1), match.group(x));
                    }
                }
                if (uriPattern.matcher(uriEntrada).matches())
                    return true;
            }
        }
        return false;
    }

    public void execute(AppRequest request, AppResponse response, DBConnection dbConnection) {
        execute(request, response);

        if (fn instanceof TriConsumer){
            TriConsumer<AppRequest, AppResponse, DBConnection> toExecute =
                    (TriConsumer<AppRequest, AppResponse, DBConnection>)fn;
            response.getHttpServletResponse().setStatus(200);
            toExecute.accept(request, response, dbConnection);
            request.getRequest().setHandled(true);
        }
    }

    public void execute(AppRequest request, AppResponse response){

        if (this.requestData.size() > 0){

            request.params.putAll(this.requestData);
        }
        if (fn == null){
            response.getHttpServletResponse().setStatus(404);
            request.getRequest().setHandled(true);
            return;
        }

        if (fn instanceof BiFunction){
            BiFunction<AppRequest, AppResponse, Object> toExecute =
                    (BiFunction<AppRequest, AppResponse, Object>)fn;
            response.getHttpServletResponse().setStatus(200);
            Object obj =  toExecute.apply(request, response);
            request.getRequest().setHandled(true);
        } else
        if (fn instanceof Function){
            Function<AppRequest, Object> toExecute = (Function<AppRequest, Object>)fn;
            response.getHttpServletResponse().setStatus(200);
            Object obj =  toExecute.apply(request);
            request.getRequest().setHandled(true);
        } else
        if (fn instanceof BiConsumer){
            BiConsumer<AppRequest, AppResponse> toExecute =
                    (BiConsumer<AppRequest, AppResponse>)fn;
            response.getHttpServletResponse().setStatus(200);
            toExecute.accept(request, response);
            request.getRequest().setHandled(true);
        }
    }

    @Override
    public String toString() {
        return "Route{" +
                "uri='" + uri + '\'' +
                ", uriPattern=" + uriPattern +
                ", fieldPattern=" + fieldPattern +
                ", requestData=" + requestData +
                '}';
    }
}
