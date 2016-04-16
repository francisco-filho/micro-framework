import database.DB;
import database.Row;
import database.RowList;
import server.App;
import util.FileUtil;

import static util.Util.mapOf;
import static util.FileUtil.*;

public class Main {

    //TODO: Util.toCSV(RowList | DataList) -> res.csv
    //TODO: Util.toXLS(RowList | DataList) -> res.xls
    //TODO: DB.dataList(String, ...) returns { fields:[], data[]}
    //TODO: WebSocket
    //TODO: HTTP2
    //TODO: CDI

    public static void main(String[] args) throws Exception {

        App app = new App((config) -> {
            config.setServeStatic(true);
            config.useConnectionPool(true);
        });

        app.use(Funcionarios.class);

        app.get("/api/texto", (req, res) -> {
            DB db = app.db("production");

            db.list(sql("municipios"), 1).forEach(System.out::println);

            res.json(mapOf("texto","hello world"));
        });

        app.get("/api/teste/:id", (req, res) -> {
            DB db = app.db("production");
            Row row = db.first(sql("municipios"), req.params.getInt("id"));
            res.json(row);
        });

        app.get("/api/download/:filename", (req, res) -> {
            String fileName  = "/home/francisco/Imagens/" + req.params.getString("filename");
            res.file(fileName);
        });

        app.get("/api/upload/", (req, res) -> {
            FileUtil.saveFiles(req.files, "/home/francisco/Imagens/");
        });

        app.get("/api/teste/:name/:id", (req, res) -> {
            DB db = app.db("production");

            RowList row = db.tx((portal) -> {
                return portal.list(sql("municipios"), req.params.getInt("id"));
            });
            res.status(200).json(row);
        });

        app.listen(3000);
    }
}