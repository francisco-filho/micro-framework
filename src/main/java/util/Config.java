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

    public Map<String,JSONObject> itemConfig = new HashMap<>();
    
    public Config(){
        this.loadConfigFromFile();
    }

    public void loadConfigFromFile(){
        JSONObject obj = Util.readJson("config.json");

        JSONArray jsonArray = (JSONArray)obj.get("databases");

        jsonArray.forEach((o) -> {
            JSONObject item = (JSONObject)o;
            itemConfig.put((String)item.get("name"), item);
        });
    }
    
    public Map<String,Object> get(String config){
        return this.itemConfig.get(config);
    }
}
