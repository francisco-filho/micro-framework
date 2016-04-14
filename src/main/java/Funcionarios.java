import database.DB;
import server.AbstractModule;
import server.App;

import static util.Util.mapOf;

public class Funcionarios extends AbstractModule {

    public void setup(App app){

        app.get("/one/", (req, res) -> {
            res.json(mapOf("route", req.getRequest().getRequestURI()));
        });

    }
}
