/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author f3445038
 */
public class Config {
    private Map<String,JSONObject> itemConfig = new HashMap<>();
    private boolean serveStatic = false;
    private String defaultDBConnection = null;
    public boolean useConnectionPool = true;

    public Config(){
        this.loadConfigFromFile();
    }

    private void loadConfigFromFile(){
        JSONObject obj = Util.readJson("config.json");
        defaultDBConnection = (String)obj.get("default-connection");

        JSONArray jsonArray = (JSONArray)obj.get("databases");

        jsonArray.forEach((o) -> {
            JSONObject item = (JSONObject)o;
            itemConfig.put((String)item.get("name"), item);
        });
    }

    public boolean getServeStatic(){
        return this.serveStatic;
    }

    public void setServeStatic(boolean serve){
        this.serveStatic = serve;
    }
    
    public Map<String,Object> get(String config){
        return this.itemConfig.get(config);
    }

    public Map<String, JSONObject> getItens(){
        return this.itemConfig;
    }

    public String getDefaultDBConnection() {
        return defaultDBConnection;
    }

    public void setDefaultDBConnection(String defaultDBConnection) {
        this.defaultDBConnection = defaultDBConnection;
    }

    public void useConnectionPool(boolean b) {
        this.useConnectionPool = b;
    }


}
