package database;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by francisco on 03/04/16.
 */
public class DBTable {

    private DB db;
    private final String name;
    private String dbname;
    private Field primaryKey;
    private final Map<String, Field> fields = new LinkedHashMap<>();

    private DBTable(){
        this.name = null;
    }

    public DBTable(DB db, String relation) {
        this.name = relation;
        this.dbname = db.name;
        this.db = db;

        String[] relationParts = relation.split(Pattern.quote("."));
        Map<String, Field> fs = DBSchema.getFields(db, relation);
        fs.forEach((k, v) -> {
            fields.put(k, v);
            if (v.getPrimaryKey() != null && v.getPrimaryKey().equals(true)) this.primaryKey = v;
        });
    }

    public DBTable(DB db, String relation, Map<String, Field> t) {
        this.name = relation;
        this.dbname = db.name;
        this.db = db;

        String[] relationParts = relation.split(Pattern.quote("."));
        Map<String, Field> fs = t;
        fs.forEach((k, v) -> {
            fields.put(k, v);
            if (v.getPrimaryKey() != null && v.getPrimaryKey().equals(true)) this.primaryKey = v;
        });
    }

    public Row get(Object key) {
        if (this.primaryKey == null){
            throw new RuntimeException("Relation does not have a primary key");
        }
        return db.first(String.format("SELECT %s FROM %s WHERE %s = ?",
                fieldsToProjection(this), this.name, this.primaryKey.getName()), key);
    }

    public Row create(Object o) {
        Map<String,Object> map = (Map<String,Object>)o;
        Map<String,Object> filteredMap = (Map<String,Object>)o;
        Object key;

        map.forEach((k, v) -> {
            if (fields.containsKey((String)k)){
                filteredMap.put(k,v);
            }
        });

        String query = String.format("INSERT INTO %s (%s) VALUES (%s) RETURNING *",
                this.name, mapToProjection(filteredMap), mapToPlaceholders(filteredMap));

        return db.executeAndReturn(query, mapToValues(filteredMap));
    }

    public Row update(Object o) {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        LinkedHashMap<String,Object> filteredMap = new LinkedHashMap<>();
        map.putAll((Map<String,Object>)o);

        List<Object> keys = new LinkedList<>();

        map.forEach((k, v) -> {
            if (fields.containsKey((String)k)){
                if (primaryKey == null || !primaryKey.getName().equals(k)) {
                    filteredMap.put(k, v);
                } else {
                    keys.add(v);
                }
            }
        });

        String primaryKeyName = this.primaryKey.getName();
        String query = String.format("UPDATE %s SET %s WHERE %s = ? RETURNING *",
                this.name, mapToUpdateProjection(filteredMap), primaryKey.getName());

        List<Object> params = mapToUpdateValues(filteredMap);
        params.add(map.get(primaryKeyName));
        return db.executeAndReturn(query, params.toArray());
    }

    public RowList list(Map<String,Object> where){
        String query = String.format("SELECT %s FROM %s WHERE %s",
                fieldsToProjection(this), this.name, mapToWhere(where));

        return db.list(query, mapToValues(where));
    }

    public Row save(Object o){
        Row row = this.get(o);
        return (row != null) ? this.update(o) : this.create(o);
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    public String fieldsToProjection(DBTable relation){
        List<String> projection = new ArrayList<>();
        getFields().forEach((k,v) -> {
            projection.add(v.getName());
        });
        return String.join(",", projection);
    }

    public String mapToProjection(Map<String,Object> map){
        List<String> projection = new ArrayList<>();
        map.forEach((k,v) -> {
            projection.add(k);
        });
        return String.join(",", projection);
    }

    public String mapToPlaceholders(Map<String,Object> map){
        List<String> projection = new ArrayList<>();
        map.forEach((k,v) -> {
            projection.add("?");
        });
        return String.join(",", projection);
    }

    public Object[] mapToValues(Map<String,Object> map){
        List<Object> values = new ArrayList<>();
        map.forEach((k,v) -> {
            values.add(v);
        });
        return values.toArray();
    }

    public String mapToUpdateProjection(Map<String,Object> map){
        List<String> projection = new ArrayList<>();
        map.forEach((k,v) -> {
            if (primaryKey == null || !primaryKey.getName().equals(k)){
                projection.add((String)k + " = ? ");
            }
        });
        return String.join(",", projection);
    }

    public String mapToWhere(Map<String, Object> map) {
        List<String> f = new ArrayList<>();
        map.forEach((k, v) -> {
            f.add(k + " = ?");
        });
        return String.join(" AND ", f);
    }



    public List<Object> mapToUpdateValues(Map<String,Object> map){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatTimestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        List<Object> values = new ArrayList<>();
        map.forEach((k,v) -> {
            if (primaryKey == null || !primaryKey.getName().equals(k)){
                Class<?> cls = ((Field)fields.get(k)).getType();
                if (cls.equals(Date.class)){
                    if (v instanceof String) {
                        Date d = null;
                        try {
                            d = format.parse((String)v);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        values.add(new java.sql.Date(d.getTime()));
                    } else {
                        java.sql.Date newDate =  new java.sql.Date(((Date)v).getTime());
                        values.add(newDate);
                    }

                } else if (cls.equals(Timestamp.class)){
                    if (v instanceof String) {
                        Date d = null;
                        try {
                            d = formatTimestamp.parse((String)v);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        values.add(new java.sql.Date(d.getTime()));
                    } else {
                        Timestamp newTimestamp =  new Timestamp(((Date)v).getTime());
                        values.add(newTimestamp);
                    }

                }
                else {
                    values.add(v);
                }
            }
        });
        return values;
    }
}