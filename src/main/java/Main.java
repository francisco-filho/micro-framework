import database.Database;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.StringUtil;
import server.App;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.PooledConnection;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.xml.internal.ws.api.message.Packet.Status.Request;

/**
 * Created by f3445038 on 21/03/16.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Database db = new Database("dired");
        db.connect();
        db.list("SELECT  * FROM pg_stat_activity").forEach((row) -> {
            System.out.println(row.toString());
        });
        db.disconnect();

        /*
        App app = new App();

        app.get("/api/teste/:name/:id", (req, res)-> {
            req.params.get("id");
            Map<String, Object> map = req.params;
            res.status(200).json(map);
        });

        app.get("/api/teste", (req, res) -> {
            List<Integer> list = new LinkedList<>();
            list.add(1);
            list.add(2);
            list.add(3);
            res.json(list);
        });

        app.listen(3000);
        */
    }
}
