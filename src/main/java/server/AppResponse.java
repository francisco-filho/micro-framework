package server;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;

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
            if (obj instanceof String)
                obj = JSONValue.parse((String)obj);

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

    public void setContentType(String contentType) {
        this.getHttpServletResponse().setContentType(contentType);
    }

    public OutputStream getOutputStream() throws IOException {
        return getHttpServletResponse().getOutputStream();
    }

    public void file(File f){
        if (!f.exists())
            throw new RuntimeException("File doesn't exists");

        String filename = f.getAbsolutePath();

        this.setContentType(URLConnection.guessContentTypeFromName(filename));

        try (BufferedInputStream bs = new BufferedInputStream(new FileInputStream(filename))){
            byte[] buffer = new byte[1024];
            BufferedOutputStream bos = new BufferedOutputStream(this.getOutputStream());

            while (bs.read(buffer) != -1){
                bos.write(buffer);
            }
            bos.flush();

        }catch (IOException iox){
            iox.printStackTrace();
        }
    }

    public void file(String filename) {
        file(new File(filename));
    }
}
