package server.middleware;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import server.App;
import server.AppRequest;
import server.AppResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by francisco on 25/03/16.
 */
public class Autenticador implements AppMiddleware {

    private String clientSecret = "c3b4d298cbf3434db4d5fe322bb5c590";
    private final String clientId = "1066607956736864";
    private final String redirectURI = "http://localhost:3000/api/code";

    public boolean authenticated = false;

    public boolean obterUsuarioFacebook(String code)
            throws MalformedURLException, IOException {

        String retorno = readURL(new URL(this.getAuthURL(code)));

        String accessToken = null;
        @SuppressWarnings("unused")
        Integer expires = null;
        String[] pairs = retorno.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length != 2) {
                throw new RuntimeException("Resposta auth inesperada.");
            } else {
                if (kv[0].equals("access_token")) {
                    accessToken = kv[1];
                }
                if (kv[0].equals("expires")) {
                    expires = Integer.valueOf(kv[1]);
                }
            }
        }

        String obj = (readURL(new URL("https://graph.facebook.com/me?access_token=" + accessToken)));
        System.out.println(JSONValue.parse(obj));
        return true;
    }

    public String getLoginRedirectURL() {
        return "https://graph.facebook.com/oauth/authorize?client_id="
                + clientId + "&display=page&redirect_uri=" + redirectURI
                + "&scope=email,publish_actions";
    }

    private String readURL(URL url) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = url.openStream();
        int r;
        while ((r = is.read()) != -1) {
            baos.write(r);
        }
        return new String(baos.toByteArray());
    }

    public String getAuthURL(String authCode) {
        return "https://graph.facebook.com/oauth/access_token?client_id="
                + clientId + "&redirect_uri=" + redirectURI
                + "&client_secret=" + clientSecret + "&code=" + authCode;
    }

    @Override
    public void init(App app) {
        app.get("/api/code", (req, res) -> {
            Object params = req.getRequest().getQueryParameters();
            HttpServletRequest r = req.getRequest();
            HttpSession session = r.getSession();
            if (session.getAttribute("authenticated") != null && (Boolean)session.getAttribute("authenticated")) {
                try {
                    res.getHttpServletResponse().sendRedirect("/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            String[] map = req.getRequest().getParameterMap().get("code");
            try {
                session.setAttribute("user", this.obterUsuarioFacebook(map[0]));
                session.setAttribute("authenticated", true);
                res.getHttpServletResponse().sendRedirect("/");

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean execute(AppRequest request, AppResponse response) {
        HttpSession session = request.getRequest().getSession();
        if (!(Boolean)session.getAttribute("authenticated") && !request.getRequest().getRequestURI().startsWith("/api/code")){
            try {
                response.getHttpServletResponse().sendRedirect(getLoginRedirectURL());
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
