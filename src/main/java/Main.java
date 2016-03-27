import database.DBConnection;
import database.ConnectionPool;
import database.RowList;
import org.apache.commons.fileupload.FileItem;
import server.App;
import server.middleware.Autenticador;
import server.middleware.AutenticadorOpenAM;
import server.middleware.Logger;
import util.Config;

import javax.servlet.http.Cookie;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by f3445038 on 21/03/16.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        App app = new App((config) -> {
            config.setServeStatic(true);
            config.useConnectionPool(true);
        });

        //app.use(new AutenticadorOpenAM());
        //app.use(new Logger());

        app.get("/api/teste/:id", (req, res) -> {
            DBConnection db = app.getDb("production");
            //res.json(db.list("SELECT  * FROM dependencia WHERE prefixo = ?", req.params.getInt("id")));
            /*DBConnection db = new DBConnection("production");
            try {
                db.connect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            */
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(req.getRequest().getRequestURI().replaceAll("/api/teste/1/", ""));

            res.json(db.first("SELECT DISTINCT prefixo, nome FROM dependencia WHERE prefixo = ?", req.params.getInt("id")));
            db.disconnect();

            //res.json(Thread.currentThread().getId() + " -> " + req);
        });

        app.get("/api/teste/:name/:id", (req, res) -> {
            DBConnection db = app.getDb("production");
            /*
            List<FileItem> files = (List<FileItem>)req.params.remove("files");
            for (FileItem f : files) {
                System.out.println(f);
            }*/
            System.err.println(Thread.currentThread().getId() + " -> " + req.params.getInt("id"));
            RowList row = db.tx((portal) -> {

                return portal.list("SELECT DISTINCT prefixo, nome FROM dependencia WHERE prefixo = ?", req.params.getInt("id"));
            });
            res.status(200).json(row);
        });
        app.listen(3000);
    }
}