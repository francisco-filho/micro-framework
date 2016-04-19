package pgadmin;

import server.App;
import server.middleware.Logger;

import static util.Util.*;
import static util.FileUtil.*;

/**
 * Created by francisco on 18/04/16.
 */
public class PgAdmin {

    public static void main(String[] args) throws Exception {
        App app = new App();

        //app.use(new Logger());

        app.get("/pgadmin/serverinfo", (request, response) -> {
            response
                //.addHeader("Access-Control-Allow-Origin", "http://localhost:3001")
                .json(mapOf(
                    "hostname", "localhost",
                    "port", 5432,
                    "database", "portal",
                    "version",  9.5,
                    "size",     "5GB"
                ));
        });

        app.get("/pgadmin/schemas", (req, res) -> {
            res.json(app.db("production").list(sql("schemas")));
        });

        app.get("/pgadmin/schemas-and-tables", (req, res) -> {
            res.json(app.db("production").list(sql("schemas-and-tables")));
        });

        app.get("/pgadmin/query/:id/", (req, res) -> {
            res.json(mapOf("message", "Not implemented yet"));
        });

        app.get("/pgadmin/savetable/:table/", (req, res) -> {
            res.json(mapOf("message", "Not implemented yet"));
        });

        app.listen(3000);
    }
}
