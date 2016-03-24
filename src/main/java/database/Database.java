package database;

import util.Config;
import util.Util;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by F3445038 on 12/11/2014.
 */
public class Database implements DatabaseInterface {

    protected Connection conn = null;
    public final int BATCH_SIZE = 10_000;

    Map<String,Object> config = new HashMap<>();

    public Database(String db) {
       this.config = new Config().get(db);
    }

    public Database(Map<String, Object> c){
        this.config = c;
    }

    @Override
    public Connection connect() throws SQLException {
        if (conn != null){
            return conn;
        }

        String user = (String)config.get("user");
        String password = (String)config.get("password");
        String url = (String)config.get("url");

        try {
            //carrega driver DB2
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }

        conn = DriverManager.getConnection(url, user, password);

        return conn;
    }

    @Override
    public void disconnect() {
        if (conn != null ){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Connection getConnection() {
        return this.conn;
    }

    @Override
    public ResultSet query(String q, Object... params) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();

        PreparedStatement stmt = this.conn.prepareStatement(q);

        for(int i = 0; i < params.length; i++){
            if (params[i] != null)
                stmt.setObject(i+1, params[i]);
            else
                stmt.setNull(i+1, Types.INTEGER);
        }

        stmt.setFetchSize(BATCH_SIZE);
        ResultSet rs = stmt.executeQuery();

        return rs;
    }

    @Override
    public List<Map<String, Object>> list(String query, Object... params) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
        ResultSet rs = this.query(query, params);

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnLength = rsmd.getColumnCount();

        for(int i = 1; i <= columnLength; i++){
            String col = rsmd.getColumnName(i);
        }

        //loop through rs
        while(rs.next()){
            LinkedHashMap<String,Object> map = new LinkedHashMap<>();

            for(int i = 1; i <= columnLength; i++){
                String colName = rsmd.getColumnName(i);
                map.put(colName, rs.getObject(colName));
            }
            list.add(map);
        }

        return list;
    }

    @Override
    public Map<String, Object> first(String query, Object... params) throws SQLException {
        ResultSet rs = this.query(query, params);

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnLength = rsmd.getColumnCount();

        for(int i = 1; i <= columnLength; i++){
            String col = rsmd.getColumnName(i);
        }

        Map<String,Object> map = new HashMap<>();
        //loop through rs
        if (rs.next()){
            for(int i = 1; i <= columnLength; i++){
                String colName = rsmd.getColumnName(i);
                map.put(colName, rs.getObject(colName));
            }
        }

        return map;
    }

    @Override
    public int copyFrom(DatabaseInterface source, String query, String targetTable) throws SQLException {
        this.conn.setAutoCommit(false);
        //executa consulta no banco "source"
        //System.err.println("Start Query....");

        //PreparedStatement stmt = source.getConnection().prepareStatement(query);
        //ResultSet rs = stmt.executeQuery();
        ResultSet rs = source.query(query);

        //verifica quantidade de colunas retornada na consulta
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnLength = rsmd.getColumnCount();

        //monta insert com qtd de parametros = qtd colunas
        String tpl = "INSERT INTO " + targetTable + " VALUES(";
        String[] questionMark = new String[columnLength];
        for(int i = 0; i < columnLength; i++){
            questionMark[i] = "?";
        }
        tpl += Util.joinFields(Arrays.asList(questionMark));
        tpl +=");";

        //Inseri dados na tabela "targetTable" do banco atual
        PreparedStatement stmtInsert = this.getConnection().prepareStatement(tpl);

        int executeResult = 0;
        final int batchSize = BATCH_SIZE;
        int count = 0;
        //Thread.sleep(500);
        while (rs.next()){
            for(int i = 0; i < columnLength; i++){
                int index = i + 1;

                if (rsmd.getColumnType(index) == 1){
                    String stringValue = ((String)rs.getObject(index));
                    stringValue = stringValue != null ? stringValue.trim() : null;
                    stmtInsert.setObject(index, stringValue);
                } else {
                    stmtInsert.setObject(index, rs.getObject(index));
                }

            }
            stmtInsert.addBatch();

            if ((++count % batchSize) == 0) {
                try {
                    stmtInsert.executeBatch();
                }
                catch(BatchUpdateException e){
                    System.err.println(e.getNextException());
                }

                System.out.println("#"+count+" -> inserted batch("+ batchSize +") records");
            }
        }
        stmtInsert.executeBatch();

        this.getConnection().commit();
        return executeResult;
    }

    public void pipeToStdout(DatabaseInterface source, String query, boolean multicommand) throws SQLException, IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/y k:m:s-S ");
        char delimiter = '|';
        //System.err.println("start query -> " + sdf.format(Calendar.getInstance().getTime()));

        ResultSet rs = null;
        if (!multicommand){
            PreparedStatement stmt = this.conn.prepareStatement(query);
            stmt.setFetchSize(BATCH_SIZE);
            rs = stmt.executeQuery();
        } else {
            String[] q = query.trim().split(";");

            PreparedStatement stmt2 = this.conn.prepareStatement(q[1]);
            stmt2.setFetchSize(BATCH_SIZE);

            source.execute(q[0]);
            rs = stmt2.executeQuery();
        }

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnLength = rsmd.getColumnCount();

        OutputStream output = new BufferedOutputStream(System.out);
        int i = 0;
        Object o = null;
        while (rs.next()){
            for(i = 0 ; i < columnLength; i++){
                if ( i != 0) {
                    output.write( delimiter );
                }
                o = rs.getObject(i+1);
                if (o != null){
                    output.write( (o).toString().replace(delimiter,' ').replace('\n',' ').getBytes() );
                } else {
                    continue;
                }
            }
            output.write('\n');
        }

        output.flush();

    }

    @Override
    public void insert(String q, Object... params) throws SQLException {
        PreparedStatement stmt = this.conn.prepareStatement(q);

        for(int i = 0; i < params.length; i++){
            stmt.setObject(i+1,params[i]);
        }
        stmt.executeUpdate();
    }

    @Override
    public int delete(String table, String where) throws SQLException {
        String delete = "DELETE FROM " + table + " WHERE " + where + ";";
        PreparedStatement stmt = this.getConnection().prepareStatement(delete);
        return stmt.executeUpdate();
    }

    @Override
    public void execute(String query) {
        PreparedStatement stmt = null;
        try {
            stmt = this.getConnection().prepareStatement(query);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<File> copyTo(String query, File file) throws SQLException {
        //executa consulta no banco "source"
        System.err.println("Start Query....");
        List<File> files = new ArrayList<File>();

        //PreparedStatement stmt = source.getConnection().prepareStatement(query);
        //ResultSet rs = stmt.executeQuery();
        ResultSet rs = this.query(query);

        //verifica quantidade de colunas retornada na consulta
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnLength = rsmd.getColumnCount();

        //monta insert com qtd de parametros = qtd colunas

        //Inseri dados na tabela "targetFile" do banco atual
        String targetFile = file.getName();
        //file = new File(targetFile + ".csv");
        files.add(file);

        int executeResult = 0;
        final int batchSize = BATCH_SIZE;
        int count = 0, fileCount = 1;
        //Thread.sleep(500);
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            while (rs.next()) {
                writer.write(Util.formatAsCsv(rs, rsmd, columnLength, ";"));
                count += 1;

                //caso atinja o 'batchSize' criar novo arquivo
                if (count >= batchSize) {
                    //salva arquivo atual
                    writer.flush();
                    writer.close();
                    //reseta counters
                    fileCount += 1;
                    count = 0;
                    System.out.println("Batch");
                    //novo arquivo
                    file = new File(targetFile + "-" + fileCount +".csv");
                    files.add(file);
                    writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
                }
            }
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        this.getConnection().commit();

        return files;
    }
}
