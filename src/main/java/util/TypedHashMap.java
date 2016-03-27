package util;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by francisco on 25/03/16.
 */
public class TypedHashMap extends ConcurrentHashMap<String, Object> {

    public Integer getInt(String k){
        Object tmp = this.get(k);
        if (tmp instanceof Integer){
            return (Integer)tmp;
        } else
        if (tmp instanceof String){
            return Integer.parseInt((String)tmp);
        }
        return null;
    }

    public String getString(String k){
        Object tmp = this.get(k);
        if (tmp instanceof String){
            return (String)tmp;
        }
        return tmp.toString();
    }
}
