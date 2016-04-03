package database;

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
            "SELECT table_catalog db, table_schema , table_name, column_name, ordinal_position col_position, is_nullable, \n" +
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
                mapToProjection(this), this.name, this.primaryKey.getName()), key);
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    public String mapToProjection(DBRelation relation){
        List<String> projection = new ArrayList<>();
        getFields().forEach((k,v) -> {
            projection.add(v.getName());
        });
        return String.join(",", projection);
    }
}
