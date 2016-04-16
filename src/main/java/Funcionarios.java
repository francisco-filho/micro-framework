import database.DB;
import database.RowList;
import server.App;
import server.AppRequest;
import server.AppResponse;
import server.annotations.Post;

import static util.Util.*;
import static util.FileUtil.*;

public class Funcionarios {

    App app;

    public void one(AppRequest req, AppResponse res){
        DB db = app.db("production");
        RowList rows = db.list(sql("municipios"), req.params.getInt("id"));
        res.json(rows);
    }

    @Post(uri="/funcionarios/todo/:id")
    public void two(AppRequest req, AppResponse res){
        res.json(mapOf("success", true));
    }
}
