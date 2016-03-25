package server;

import database.DBConnection;
import database.TriConsumer;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import util.Config;
import util.TriFunction;
import util.Util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Created by f3445038 on 21/03/16.
 */
public class App extends AbstractHandler{

    private Server server = null;
    private final Config config;
    private AppRouter appRouter = new AppRouter();

    public App(){
        this.config = new Config();
    }

    public App(Consumer<Config> config){
        this();
        config.accept(this.config);
    }

    public void listen(int port) throws Exception {
        server = new Server(port);

        if (config.getServeStatic()){
            ResourceHandler rh = new ResourceHandler();
            rh.setResourceBase(Util.getPublicDirectory().getAbsolutePath());
            rh.setDirectoriesListed(true);
            rh.setWelcomeFiles(new String[]{ "index.html" });
            rh.setMinMemoryMappedContentLength(-1);

            HandlerList handlers = new HandlerList();
            handlers.setHandlers( new Handler[] {rh, this});
            server.setHandler(handlers);
        } else {
            server.setHandler(this);
        }

        server.start();
        server.join();
    }

    @Override
    public void handle(String s, Request jettyRequest, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        System.out.println(new Date() + " " + req.getMethod() + " " + "" + req.getContentType() + " " +req.getRequestURI());

        AppResponse response = new AppResponse(res);
        AppRequest request = new AppRequest(jettyRequest);

        //handling files
        if (req.getContentType() != null && (req.getContentType().startsWith("multipart/form-data")
                || req.getContentType().equals("false"))){
            request.params.putAll(processFile(req));
        }

        Route route = appRouter.getRoute(request.getRequest());

        route.execute(request, response);

        jettyRequest.setHandled(true);
    }

    private Map<String,Object> processFile(HttpServletRequest req){
        final int MAX_REQUEST_SIZE = 1_000_000;
        Map<String, Object> params = new LinkedHashMap<>();
        List<FileItem> files = new LinkedList<>();
        params.put("files", files);

        String dir = "/tmp/";

        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(MAX_REQUEST_SIZE);
        factory.setRepository(new File("/tmp/"));

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(MAX_REQUEST_SIZE);

        // Processa request
        try {
            List<FileItem> items = upload.parseRequest(req);

            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = iter.next();

                if (item.isFormField()) {
                    params.put(item.getFieldName(), item.getString());
                } else {
                    files.add(item);
                    /*
                    --exemplo de processamento de arquivos
                    FileOutputStream fos = new FileOutputStream(new File(dir + item.getName()));
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    BufferedInputStream bis = new BufferedInputStream(item.getInputStream());
                    IOUtils.copy(bis, bos);
                    bos.flush();
                    */
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }

        return params;
    }


    public Object get(String uri, BiFunction<AppRequest, AppResponse, Object> fn){
        return appRouter.add("GET", new Route(uri, fn));
    }

    public Object get(String uri, TriFunction<DBConnection, AppRequest, AppResponse, Object> fn){
        return appRouter.add("GET", new Route(uri, fn));
    }

    public void get(String uri, BiConsumer<AppRequest, AppResponse> fn){
        appRouter.add("GET", new Route(uri, fn));
    }

    public void get(String uri, TriConsumer<AppRequest, AppResponse,DBConnection> fn){
        appRouter.add("GET", new Route(uri, fn));
    }

    public void post(String uri, BiConsumer<AppRequest, AppResponse> fn){
        appRouter.add("POST", new Route(uri, fn));
    }

    public Config getConfig() {
        return config;
    }
}
