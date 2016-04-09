import database.DB;
import database.Row;
import database.RowList;
import server.App;

import java.io.File;
import java.nio.file.Files;

import static util.Util.mapOf;

public class Main {

    public static void main(String[] args) throws Exception {
        App app = new App((config) -> {
            config.setServeStatic(true);
            config.useConnectionPool(true);
        });
        //app.use(new AutenticadorOpenAM());
        //app.use(new Logger());

        app.get("/api/teste/:id", (req, res) -> {
            DB db = app.db("production");
            Row row = db.first("SELECT prefixo, nome, uf FROM dependencia WHERE prefixo = ?", req.params.getInt("id"));

            res.json(row);
        });

        app.get("/api/texto", (req, res) -> {
            res.json(mapOf("texto","hello world"));
        });

        app.get("/api/download/:filename", (req, res) -> {
            File f = new File("/home/francisco/Imagens/" + req.params.getString("filename"));
            if (!f.exists())
                throw new RuntimeException("File doesn't exists");
            else
                res.file(f.getAbsolutePath());
        });

        app.get("/api/teste/:name/:id", (req, res) -> {
            DB db = app.db("production");
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