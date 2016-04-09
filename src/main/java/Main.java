import database.DB;
import database.Row;
import database.RowList;
import server.App;

import java.io.*;
import java.net.URLConnection;

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
            DB db = app.db("production");
            Row row = db.first("SELECT prefixo, nome, uf FROM dependencia WHERE prefixo = ?", req.params.getInt("id"));

            res.json(row);
        });

        app.get("/api/texto", (req, res) -> {
            res.json("{\"texto\": \"hello world\"}");
        });

        app.get("/api/download/:filename", (req, res) -> {
            String filename  = "/home/francisco/Imagens/" + req.params.getString("filename");
            File f = new File(filename);

            if (!f.exists()) throw new RuntimeException("File doesn't exists");
            res.getHttpServletResponse().setContentType(URLConnection.guessContentTypeFromName(f.getAbsolutePath()));

            try (BufferedInputStream bs = new BufferedInputStream(new FileInputStream(f))){
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                BufferedOutputStream bos = new BufferedOutputStream(res.getHttpServletResponse().getOutputStream());

                while ((bytesRead = bs.read(buffer)) != -1){
                    bos.write(buffer);
                }
                bos.flush();

            }catch (IOException iox){
                iox.printStackTrace();
            }
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