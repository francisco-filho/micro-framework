package database;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by francisco on 03/04/16.
 */
public class DBRelation {

    private DB db;
    private final String name;
    private Field primaryKey;
    private final Map<String, Field> fields = new LinkedHashMap<>();

    private DBRelation(){
        this.name = null;
    }

    private final String relationQuery = "\n" +
            "WITH primary_key as (\n" +
            "SELECT a.attname , indrelid\n" +
            "FROM   pg_index i\n" +
            "JOIN   pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey)\n" +
            "WHERE   i.indisprimary\n" +
            ")\n" +
            "SELECT table_catalog db, table_schema , table_name, RTRIM(column_name) column_name, ordinal_position col_position, is_nullable, \n" +
            "data_type, character_maximum_length max_length,\n" +
            "CASE WHEN attname IS NOT NULL THEN true END is_primary_key\n" +
            "FROM information_schema.columns c\n" +
            "LEFT JOIN primary_key i ON i.indrelid = (table_schema||'.'||table_name)::regclass AND column_name=attname\n" +
            "WHERE table_schema= ? AND table_name = ? \n" +
            "ORDER BY ordinal_position";

    public DBRelation(DB db, String relation) {
        this.name = relation;
        this.db = db;

        String[] relationParts = relation.split(Pattern.quote("."));

        db.list(relationQuery, relationParts[0], relationParts[1]).forEach((row) -> {
            Field f = Field.build(row);
            if (f.isPrimaryKey != null && f.isPrimaryKey) this.primaryKey = f;
            fields.put((String)row.get("column_name"), f);
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

    public Row save(Object o){
        Row row = this.get(o);
        return (row != null) ? this.update(o) : this.create(o);
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    public String fieldsToProjection(DBRelation relation){
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
                        java.sql.Timestamp newTimestamp =  new java.sql.Timestamp(((Date)v).getTime());
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
