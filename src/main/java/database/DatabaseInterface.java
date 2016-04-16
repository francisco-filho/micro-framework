package database;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DatabaseInterface {

    Connection connect() throws SQLException;
    void disconnect();

    Connection getConnection();

    ResultSet query(String q, Object... params) throws SQLException;

    int execute(String query, Object... params);

    int copyFrom(DatabaseInterface source, String query, String targetTable) throws SQLException;
    public List<File> copyTo(String query, File file) throws SQLException;

}
