package server.middleware;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import server.App;
import server.AppRequest;
import server.AppResponse;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by francisco on 26/03/16.
 */
public class AutenticadorOpenAM  extends AbstractHandler implements AppMiddleware {

    private final String USER_SESSION_ATTR = "usuario";
    private final String PROTOCOL = "http";
    private final String HOST = "openam.example.com";
    private final String PORT = "8080";
    private final String URI = "OpenAM-13.0.0/XUI/#login/&goto=";
    private final String IDENTITY_URI = "OpenAM-13.0.0/identity/attributes?refresh=true&subjectid=";
    private final String LOGIN_URL = String.format("%s://%s:%s/%s", PROTOCOL, HOST, PORT, URI);
    private final String LOGOUT_URL = String.format("%s://%s:%s/%s", PROTOCOL, HOST, PORT, "OpenAM-13.0.0/logout");
    private final String IDENTITY_URL = String.format("%s://%s:%s/%s", PROTOCOL, HOST, PORT, IDENTITY_URI);
    private final String TOKEN_VALIDATION_URI = String.format("%s://%s:%s/%s", PROTOCOL, HOST, PORT, "OpenAM-13.0.0/json/sessions/");
    private final String TOKEN_VALIDATION_OLD_URI = String.format("%s://%s:%s/%s", PROTOCOL, HOST, PORT, "identity/isTokenValid?tokenid");
    private final String SSO_COOKIE = "iPlanetDirectoryPro";
    private final int    CONNECTION_TIMEOUT = 1_000;
    private final int    MAX_SESSION_INTERVAL = 60*15;

    @Override
    public void init(App app) {
        app.get("/auth/logout", (req, res) -> {
            //remove usuário da sessão
            req.getSession().setAttribute(USER_SESSION_ATTR, null);
            //limpa cookies
            Cookie cookie = req.getCookie(SSO_COOKIE);
            cookie.setMaxAge(0);
            cookie.setValue("");
            cookie.setPath("/");
            cookie.setDomain("");
            res.redirectTo(LOGOUT_URL);
        });

        app.get("/auth/userdetails", (req, res) -> {
            Object obj = req.getSession().getAttribute(USER_SESSION_ATTR);
            System.err.println(app.db("production").list("SELECT * FROM pessoas WHERE id = 100"));
            res.json(obj);
        });
    }

    private boolean usuarioEstaNaSecao(AppRequest request){
        return request.getSession().getAttribute(USER_SESSION_ATTR) != null;
    }

    private String getToken(String token) throws IOException {
        if (token == null || token.isEmpty()) return null;
        return getIdentityFromOpenAM(IDENTITY_URL + token);
    }

    @Override
    public void handle(String s, Request jettyRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        AppResponse response = new AppResponse(httpServletResponse);
        AppRequest request = new AppRequest(jettyRequest);

        Cookie c = request.getCookie(SSO_COOKIE);
        Map<String, String> identityMap = new HashMap<>();
        String TO_LOGIN = LOGIN_URL + URLEncoder.encode(String.format("%s://%s:%s", PROTOCOL, HOST, 3000));

        if (usuarioEstaNaSecao(request)){
            return;
        }
        //não há usuário na secão, tenta cookie ou redireciona para login
        if (!usuarioEstaNaSecao(request) && isTokenValid(c.getValue(), true)) {
            String tokenIdentity, r = null;

            if (c != null && c.getValue() != null){
                try {
                    tokenIdentity = getToken(c.getValue());
                } catch(Exception e){
                    response.redirectTo(TO_LOGIN);
                    return;
                }
                if (tokenIdentity != null) {
                    addUsuarioNaSessao(request, tokenIdentity);
                }
            } else {
                response.redirectTo(TO_LOGIN);
                return;
            }
            return;
        }
        //tem usuário e cookie não é nulo
        if (c != null){
            String tokenIdentity;
            try {
                tokenIdentity = getToken(c.getValue());
            } catch(Exception e){
                response.redirectTo(TO_LOGIN);
                return;
            }
            if (tokenIdentity != null) {
                if (tokenIdentity != null) {
                    addUsuarioNaSessao(request, tokenIdentity);
                } else
                    response.redirectTo(TO_LOGIN);

            } else {
                response.redirectTo(TO_LOGIN);
            }
            return;
        }
        //não tem usuário nem cookie
        if (!usuarioEstaNaSecao(request) && c == null){
            response.redirectTo(TO_LOGIN);
            return;
        }
        response.redirectTo(TO_LOGIN);
        return;
    }

    private void addUsuarioNaSessao(AppRequest req, String userIdentity){
        req.getSession().setMaxInactiveInterval(MAX_SESSION_INTERVAL);
        req.getSession().setAttribute(USER_SESSION_ATTR, parseIdentity(userIdentity));
    }

    private Map<String, String> parseIdentity(String identityString){
        Map<String, String> identityMap = new HashMap<>();

        String name = null;
        for (String s : identityString.split("\n")) {
            if (s.startsWith("userdetails.attribute.name=")){
                name = s.split("=")[1];
            } else
            if (s.startsWith("userdetails.attribute.value=")){
                identityMap.put(name, s.split("=")[1]);
                name = null;
            } else
            if (s.startsWith("userdetails.token.id=")){
                identityMap.put("id", s.split("=")[1]);
            }
        }
        identityMap.remove("userPassword");
        return identityMap;
    }

    private String http(String method, String url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
        urlConnection.setRequestMethod(method);
        urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
        urlConnection.setReadTimeout(CONNECTION_TIMEOUT);
        urlConnection.connect();

        StringBuilder builder;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
            builder = new StringBuilder();
            String line = "";
            while (null != (line = br.readLine())) {
                builder.append(line + "\n");
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
            throw ioex;
        }
        return builder.toString();
    }

    private String getIdentityFromOpenAM(String url ) throws IOException {
        return http("GET", url);
    }

    private boolean isTokenValid(String token, Boolean newAPI){
        try {
            String result;
            if (newAPI == null || !newAPI)
                result = http("GET", TOKEN_VALIDATION_OLD_URI + token);
            else
                result = http("POST", TOKEN_VALIDATION_URI + token + "?_action=validate");

            JSONObject response = (JSONObject)JSONValue.parse(result);

            return (Boolean)response.get("valid");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}