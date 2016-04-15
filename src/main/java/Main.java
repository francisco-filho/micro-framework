import database.DB;
import database.Row;
import database.RowList;
import server.App;

import static util.Util.mapOf;

public class Main {

    //TODO: FileUtil.saveAs(FileItem, Filename)
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

        app.addController(Funcionarios.class);

        app.get("/api/texto", (req, res) -> {
            System.out.println(req.params.get("id"));
            res.json(mapOf("texto","hello world"));
        });

        app.get("/api/teste/:id", (req, res) -> {
            DB db = app.db("production");
            Row row = db.first("SELECT prefixo, nome, uf FROM dependencia WHERE prefixo = ?", req.params.getInt("id"));
            res.json(row);
        });

        app.get("/api/download/:filename", (req, res) -> {
            String fileName  = "/home/francisco/Imagens/" + req.params.getString("filename");
            res.file(fileName);
        });

        app.get("/api/teste/:name/:id", (req, res) -> {
            DB db = app.db("production");

            RowList row = db.tx((portal) -> {
                return portal.list("SELECT DISTINCT municipio, uf FROM municipios WHERE id = ?", req.params.getInt("id"));
            });
            res.status(200).json(row);
        });

        app.listen(3000);
    }
}