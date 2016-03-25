package database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.json.simple.JSONObject;
import util.Config;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by francisco on 25/03/16.
 */
public class ConnectionPool {

    List<ComboPooledDataSource> dataSources = new LinkedList<>();

    public ConnectionPool(Config config){
        for (Map.Entry e: config.getItens().entrySet()){
            JSONObject obj = (JSONObject)e.getValue();
            ComboPooledDataSource cpds = new ComboPooledDataSource((String)obj.get("name"));
            try {
                cpds.setDriverClass((String)obj.getOrDefault(obj.get("driver"), "org.postgresql.Driver"));
            } catch (PropertyVetoException e1) {
                e1.printStackTrace();
            }
            cpds.setJdbcUrl((String)obj.get("url"));
            cpds.setUser((String)obj.get("user"));
            cpds.setPassword((String)obj.get("password"));

            dataSources.add(cpds);
        }
    }

    public Connection getConnection(String dataSourceName){
        for(ComboPooledDataSource cpds: dataSources){
            if (cpds.getDataSourceName().equals(dataSourceName)){
                System.out.println(cpds);
                try {
                    return cpds.getConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public ComboPooledDataSource get(String dataSourceName){
        for(ComboPooledDataSource cpds: dataSources){
            if (cpds.getDataSourceName().equals(dataSourceName)){
                System.out.println(cpds);
                return cpds;
            }
        }
        return null;
    }



}
