package server;

import database.ConnectionPool;
import database.DB;
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
import org.eclipse.jetty.server.session.SessionHandler;
import server.middleware.AppMiddleware;
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
    private Config config;
    private ConnectionPool connectionPool;
    private AppRouter appRouter = new AppRouter();

    private List<Handler> appHandlers = new ArrayList<>();

    private final ThreadLocal<DB> dbConnections = new ThreadLocal<>();

    public App(){
        this.config = new Config();
    }

    public App(Consumer<Config> config){
        this();
        config.accept(this.config);
        if (this.config.useConnectionPool){
            this.connectionPool  = new ConnectionPool(this.config);
        }
    }

    public DB db(String db){
        if (!this.config.useConnectionPool) throw new RuntimeException();
        dbConnections.set(new DB(connectionPool.get(db)));
        return dbConnections.get();
    }

    public void listen(int port) throws Exception {
        server = new Server(port);

        HandlerList handlers = new HandlerList();

        if (config.getServeStatic()){
            ResourceHandler rh = new ResourceHandler();
            rh.setResourceBase(Util.getPublicDirectory().getAbsolutePath());
            rh.setDirectoriesListed(true);
            rh.setWelcomeFiles(new String[]{ "index.html" });
            rh.setMinMemoryMappedContentLength(-1);

            appHandlers.add(0, rh);
            appHandlers.add(0, new SessionHandler());
            appHandlers.add(this);
        } else {
            appHandlers.add(this);
        }
        handlers.setHandlers(appHandlers.toArray(new Handler[appHandlers.size()]));

        server.setHandler(handlers);
        server.start();
        server.join();
    }

    @Override
    public void handle(String s, Request jettyRequest, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        AppResponse response = new AppResponse(res);
        AppRequest request = new AppRequest(jettyRequest);

        //handling files
        if (req.getContentType() != null && (req.getContentType().startsWith("multipart/form-data")
                || req.getContentType().equals("false"))){
            request.params.putAll(processFile(req));
        }

        Route route = appRouter.getRoute(request);
        if (route == null) {
            res.setStatus(404);
            jettyRequest.setHandled(true);
            return;
        }
        route.execute(request, response);

        if (dbConnections.get() != null){
            dbConnections.get().disconnect();
        }
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

    public void use(AbstractHandler middleware){
        appHandlers.add(middleware);
        if (middleware instanceof AppMiddleware)
            ((AppMiddleware)middleware).init(App.this);
    }

//    public Object get(String uri, BiFunction<AppRequest, AppResponse, Object> fn){
//        return appRouter.add("GET", new Route(uri, fn));
//    }

//    public Object get(String uri, TriFunction<DB, AppRequest, AppResponse, Object> fn){
//        return appRouter.add("GET", new Route(uri, fn));
//    }

    public void get(String uri, BiConsumer<AppRequest, AppResponse> fn){
        appRouter.add("GET", new Route(uri, fn));
    }

//    public void get(String uri, TriConsumer<AppRequest, AppResponse, DB> fn){
//        appRouter.add("GET", new Route(uri, fn));
//    }

    public void post(String uri, BiConsumer<AppRequest, AppResponse> fn){
        appRouter.add("POST", new Route(uri, fn));
    }

    public Config getConfig() {
        return config;
    }
}
