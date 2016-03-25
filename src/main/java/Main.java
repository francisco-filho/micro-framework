import database.DatabaseManager;
import database.Row;
import database.RowList;
import org.apache.commons.fileupload.FileItem;
import server.App;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by f3445038 on 21/03/16.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        DatabaseManager db = new DatabaseManager("dired");

        App app = new App();
        app.serveStatic(true);

        app.get("/api/teste/:id", (req, res) -> {
            res.json(db.tx((dired)->{
                return dired.list("SELECT  * FROM dependencia ORDER BY 1 LIMIT 10");
            }));
        });

        app.post("/api/teste/:name/:id", (req, res)-> {
            List<FileItem> files = (List<FileItem>)req.params.remove("files");
            for (FileItem f : files) {
                System.out.println(f);
            }
            System.out.println(req.params);
            RowList row = db.tx((dired)->{
                String p = (String)req.params.get("id");
                String x= null;
                //return dired.list("SELECT DISTINCT * FROM dependencia WHERE prefixo = ?", Integer.parseInt(p));
                return dired.list("SELECT DISTINCT * FROM dependencia WHERE prefixo = ?", null);
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            res.status(200).json(files);
        });

        app.listen(3000);
    }
}
