import database.DB;
import database.RowList;
import server.App;
import server.AppRequest;
import server.AppResponse;
import server.annotations.Post;

import static util.Util.mapOf;

public class Funcionarios {

    App app;

    public void one(AppRequest req, AppResponse res){
        DB db = app.db("production");
        RowList rows = db.list("SELECT DISTINCT d.* FROM dependencia d WHERE prefixo = ? LIMIT 10", req.params.getInt("prefixo"));
        res.json(rows);
    }

    @Post
    public void two(AppRequest req, AppResponse res){
        res.json(mapOf("success", true));
    }
}
