import database.DBConnection;
import database.ConnectionPool;
import database.RowList;
import org.apache.commons.fileupload.FileItem;
import server.App;
import server.middleware.Logger;
import util.Config;

import java.util.List;

/**
 * Created by f3445038 on 21/03/16.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        ConnectionPool pool = new ConnectionPool(new Config());

        App app = new App((config) -> {
            config.setServeStatic(true);
        });

        app.use(new Logger());

        app.get("/api/teste/:id", (req, res) -> {
            DBConnection db = new DBConnection(pool.get("production"));
            res.json(db.list("SELECT  * FROM dependencia WHERE prefixo = ?", req.params.getInt("id")));
        });

        app.get("/api/teste/:name/:id", (req, res) -> {
            DBConnection db = new DBConnection(pool.get("production"));

            List<FileItem> files = (List<FileItem>)req.params.remove("files");
            for (FileItem f : files) {
                System.out.println(f);
            }
            RowList row = db.tx((portal) -> {
                return portal.list("SELECT DISTINCT * FROM dependencia WHERE prefixo = ?", req.params.getInt("id"));
            });
            res.status(200).json(files);
        });
        app.listen(3000);
    }
}
