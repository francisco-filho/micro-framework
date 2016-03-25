package database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * Created by francisco on 24/03/16.
 */
public class DatabaseManager {

    private final Database db;

    public DatabaseManager(String dbname){
        this.db = new Database(dbname);
        try {
            db.connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void tx(DatabaseConsumer<Database> db) throws SQLException {
        try {
            this.db.getConnection().setAutoCommit(false);
            db.accept(this.db);
            this.db.getConnection().commit();
        } catch(SQLException ex){
            this.db.getConnection().rollback();
            ex.printStackTrace();
        }
    }

    public RowList tx(Function<Database, Object> db)  {
        RowList result = null;
        try {
            this.db.getConnection().setAutoCommit(false);
            result = (RowList)db.apply(this.db);
            this.db.getConnection().commit();
        } catch(SQLException ex){

            ex.printStackTrace();
        }
        try {
            this.db.getConnection().rollback();
        } catch(SQLException ex){
            ex.printStackTrace();
        }

        return result;
    }

    public void disconnect(){
        db.disconnect();
    }
}
