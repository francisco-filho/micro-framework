package server;

import database.*;
import database.Field;
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
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import server.annotations.Post;
import server.middleware.AppMiddleware;
import util.Config;
import util.Util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class App extends AbstractHandler{

    private Server server = null;
    private Config config;
    private ConnectionPool connectionPool;
    private AppRouter appRouter = new AppRouter();

    private Map<String, Map<String,Field>> schemas = new HashMap<>();

    private Map<String, Object> controllers = new HashMap<>();

    private List<Handler> appHandlers = new ArrayList<>();

    private final ThreadLocal<DB> dbConnections = new ThreadLocal<>();

    public DBTable table(DB db, String relation) {
        Map<String, Field> schema = schemas.get(db.name + "." + relation);
        synchronized (schemas){
            if (schema == null) {
                synchronized (schemas){
                    schemas.put(db.name + "." + relation, DBSchema.getFields(db, relation));
                }
                schema = schemas.get(db.name + "." + relation);
            } else return new DBTable(db, relation, schema);
        }
        return new DBTable(db, relation, schema);
    }

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

    private ServletHolder getServletHolder(String dir) {
        ServletHolder holderHome = new ServletHolder(dir, DefaultServlet.class);
        holderHome.setInitParameter("dirAllowed","true");
        holderHome.setInitParameter("resourceBase", "/home/francisco/apps/dev/jetty-framework/src/main/resources/"+dir+"/");
        holderHome.setInitParameter("welcomeServlets","true");
        holderHome.setInitParameter("redirectWelcome", "false");
        return holderHome;
    }

    private ServletContextHandler getServletContextHandler() {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        context.setResourceBase("/home/francisco/apps/dev/jetty-framework/src/main/resources/");
        context.setWelcomeFiles(new String[]{ "index.html" });
        context.setInitParameter("useFileMappedBuffer", "false");
        context.setInitParameter("maxCachedFiles", "0");
        context.setInitParameter("dirAllowed","true");
        return context;
    }

    @Override
    public void handle(String s, Request jettyRequest, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        AppResponse response = new AppResponse(res);
        AppRequest request = new AppRequest(jettyRequest);

        //handling files
        if (req.getContentType() != null && (req.getContentType().startsWith("multipart/form-data")
                || req.getContentType().equals("false"))){
            request.files.addAll(processFile(req));
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

    private List<FileItem> processFile(HttpServletRequest req){
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
        return files;
    }

    public void use(AbstractHandler middleware){
        appHandlers.add(0, middleware);
        if (middleware instanceof AppMiddleware)
            ((AppMiddleware)middleware).init(App.this);
    }

    public void use(Class<?> cls) {
        Arrays.asList(cls.getDeclaredMethods()).stream()
        .filter((method) -> {
            Class<?>[] types  = method.getParameterTypes();

            return (types.length == 2 &&
                    types[0].equals(AppRequest.class) &&
                    types[1].equals(AppResponse.class) &&
                    Modifier.isPublic(method.getModifiers()));
        })
        .forEach((method) -> {
            String route = String.format("/%s/%s", cls.getSimpleName(), method.getName()).toLowerCase();
            if (method.getAnnotation(Post.class) != null){
                this.post(route, (req, res) -> {
                    executeMethod(cls, method, req, res);
                });
                return;
            }
            this.get(route, (req, res) -> {
                executeMethod(cls, method, req, res);
            });
        });
    }

    private void executeMethod(Class<?> cls, Method method, AppRequest req, AppResponse res) {
        Object o = null;
        try {
            if (!controllers.containsKey(cls.getCanonicalName())) {
                o = cls.newInstance();
                synchronized (controllers.getClass()){
                    if (!controllers.containsKey(cls.getCanonicalName())){
                        controllers.put(cls.getCanonicalName(), o);

                        java.lang.reflect.Field appField = cls.getDeclaredField("app");
                        if (appField != null) {
                            appField.setAccessible(true);
                            appField.set(o, App.this);
                        }
                    }
                }
            }
            else { o = controllers.get(cls.getCanonicalName()); }

            method.invoke(o, req, res);
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }

    public void get(String uri, BiConsumer<AppRequest, AppResponse> fn){
        appRouter.add("GET", new Route(uri, fn));
    }

    public void post(String uri, BiConsumer<AppRequest, AppResponse> fn){
        appRouter.add("POST", new Route(uri, fn));
    }

    public Config getConfig() {
        return config;
    }
}