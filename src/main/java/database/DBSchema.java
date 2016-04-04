package database;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by francisco on 03/04/16.
 */
public class DBSchema {

    private final static String relationQuery = "\n" +
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

    public static final Map<String, Field> getFields(DB db, String relation){
        Map<String, Field> fields = new LinkedHashMap<>();

        String[] relationParts = relation.split(Pattern.quote("."));
        db.list(relationQuery, relationParts[0], relationParts[1]).forEach((row) -> {
            Field f = Field.build(row);
            fields.put((String)row.get("column_name"), f);
        });
        return fields;
    }

}
