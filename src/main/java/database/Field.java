package database;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by francisco on 03/04/16.
 */
public class Field {
    String name;
    Integer order;
    Boolean isNullable = true;
    Class<?> type = String.class;
    Boolean isPrimaryKey = false;
    Integer maxLength;

    private final static Map<String, Class<?>> classes = new HashMap<>();

    static {
        classes.put("character", String.class);
        classes.put("text", String.class);
        classes.put("smallint", Short.class);
        classes.put("integer", Integer.class);
        classes.put("bigint", BigInteger.class);
        classes.put("date", java.sql.Date.class);
        classes.put("timestamp", Timestamp.class);
    }

    private Field(){}

    public static Field build(Map<String,Object> map){
        Field f = new Field();
        f.setName((String)map.get("column_name"));
        f.setOrder((Integer) map.get("col_position"));
        f.setPrimaryKey((Boolean)map.get("is_primary_key"));
        f.setNullable((String)map.get("is_nullable") == "YES" ? true: false);
        f.setMaxLength(map.get("max_length") != null ? (Integer)map.get("max_length") : null);
        f.stringToClass((String) map.get("data_type"));
        return f;
    }

    private Class<?> stringToClass(String sqlType){
        if (classes.containsKey(sqlType))
            return classes.get(sqlType);

        return String.class;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Boolean getNullable() {
        return isNullable;
    }

    public void setNullable(Boolean nullable) {
        isNullable = nullable;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Boolean getPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }
}
