package database;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by F3445038 on 12/11/2014.
 */
public interface DatabaseConsumer {

    Connection connect() throws SQLException;
    void disconnect();

    Connection getConnection();

    ResultSet query(String q, Object... params) throws SQLException;

    List<Map<String,Object>> list(String query, Object... params) throws SQLException;

    Map<String,Object> first(String query, Object... params) throws SQLException;

    void insert(String q, Object... params) throws SQLException;

    int delete(String table, String where) throws SQLException;

    public void execute(String query);

    int copyFrom(DatabaseConsumer source, String query, String targetTable) throws SQLException;
    public List<File> copyTo(String query, File file) throws SQLException;

}
