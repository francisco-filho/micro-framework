package database;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.SQLException;
import java.util.function.Function;

/**
 * Created by francisco on 24/03/16.
 */
public class DatabaseConnection {

    private final DBConnection db;

    public DatabaseConnection(String dbname){
        this.db = new DBConnection(dbname);
        try {
            db.connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public DatabaseConnection(ComboPooledDataSource cpds, String dbname){
        this.db = new DBConnection(dbname);
        try {
            db.connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public RowList list(String query, Object ... params){
        try {
            this.db.getConnection().setAutoCommit(true);
            return db.list(query, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Row first(String query, Object... params){
        try {
            this.db.getConnection().setAutoCommit(true);
            return db.first(query, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int execute(String query, Object... params){

        return 0;
    }



    public void disconnect(){
        db.disconnect();
    }
}
