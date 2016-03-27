package server.middleware;

import server.App;
import server.AppRequest;
import server.AppResponse;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by francisco on 26/03/16.
 */
public class AutenticadorOpenAM implements AppMiddleware {

    private final String USER_SESSION_ATTR = "usuario";
    private final String PROTOCOL = "http";
    private final String HOST = "openam.example.com";
    private final String PORT = "8080";
    private final String URI = "OpenAM-13.0.0/XUI/#login/&goto=";
    private final String IDENTITY_URI = "OpenAM-13.0.0/identity/attributes?refresh=true&subjectid=";
    private final String LOGIN_URL = String.format("%s://%s:%s/%s", PROTOCOL, HOST, PORT, URI);
    private final String LOGOUT_URL = String.format("%s://%s:%s/%s", PROTOCOL, HOST, PORT, "OpenAM-13.0.0/logout");
    private final String IDENTITY_URL = String.format("%s://%s:%s/%s", PROTOCOL, HOST, PORT, IDENTITY_URI);
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
    public boolean execute(AppRequest request, AppResponse response) throws IOException {
        Cookie c = request.getCookie(SSO_COOKIE);
        Map<String, String> identityMap = new HashMap<>();
        String TO_LOGIN = LOGIN_URL + URLEncoder.encode("http://www.example.com:3000/");

        if (usuarioEstaNaSecao(request)){
            return true;
        }
        //não há usuário na secão, tenta cookie ou redireciona para login
        if (!usuarioEstaNaSecao(request)) {
            String tokenIdentity, r = null;

            if (c != null && c.getValue() != null){
                try {
                    tokenIdentity = getToken(c.getValue());
                } catch(Exception e){
                    response.redirectTo(TO_LOGIN);
                    return false;
                }
                if (tokenIdentity != null) {
                    addUsuarioNaSessao(request, tokenIdentity);
                }
            } else {
                response.redirectTo(TO_LOGIN);
                return false;
            }
            return true;
        }
        //tem usuário e cookie não é nulo
        if (c != null){
            String tokenIdentity;
            try {
                tokenIdentity = getToken(c.getValue());
            } catch(Exception e){
                response.redirectTo(TO_LOGIN);
                return false;
            }
            if (tokenIdentity != null) {
                if (tokenIdentity != null) {
                    addUsuarioNaSessao(request, tokenIdentity);
                } else
                    response.redirectTo(TO_LOGIN);

            } else {
                response.redirectTo(TO_LOGIN);
            }
            return true;
        }
        //não tem usuário nem cookie
        if (!usuarioEstaNaSecao(request) && c == null){
            response.redirectTo(TO_LOGIN);
            return false;
        }

        response.redirectTo(TO_LOGIN);
        return false;
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

    private String getIdentityFromOpenAM(String url ) throws IOException {
        URLConnection connection;
        connection = new URL( url ).openConnection();
        connection.setRequestProperty("Request-Method", "GET");
        connection.setConnectTimeout( CONNECTION_TIMEOUT );
        connection.setReadTimeout( CONNECTION_TIMEOUT );
        connection.setDoInput(true);
        connection.setDoOutput(false);

        connection.connect();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
        StringBuffer newData = new StringBuffer(10000);
        String s = "";
        while (null != ((s = br.readLine()))) {
            newData.append(s);
            newData.append("\n");
        }
        br.close();
        return newData.toString();
    }
}