import database.DBConnection;
import database.Row;
import database.RowList;
import server.App;
import server.middleware.AutenticadorOpenAM;
import server.middleware.Logger;

import java.sql.SQLException;

/**
 * Created by f3445038 on 21/03/16.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        App app = new App((config) -> {
            config.setServeStatic(false);
            config.useConnectionPool(true);
        });
        //app.use(new AutenticadorOpenAM());
        app.use(new Logger());

        app.get("/api/teste/:id", (req, res) -> {
            DBConnection db = app.getDb("production");
            Row row = db.first("SELECT DISTINCT id, municipio, uf FROM municipios WHERE id = ?", req.params.getInt("id"));
            res.json(row);
        });

        app.get("/api/teste/:name/:id", (req, res) -> {
            DBConnection db = app.getDb("production");
            /*
            List<FileItem> files = (List<FileItem>)req.params.remove("files");
            for (FileItem f : files) {
                System.out.println(f);
            }*/
            RowList row = db.tx((portal) -> {

                return portal.list("SELECT DISTINCT municipio, uf FROM municipios WHERE id = ?", req.params.getInt("id"));
            });
            res.status(200).json(row);
        });
        app.listen(3000);
    }
}