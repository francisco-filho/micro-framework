/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author f3445038
 */
public class Util {
    
    private static DecimalFormat d = new DecimalFormat();
	
    private static DecimalFormat d2;
    private static DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    private static DecimalFormat d0;
    private static SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    private static SimpleDateFormat full = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    static{
            symbols.setDecimalSeparator(',');
            symbols.setGroupingSeparator('.');
            d2 = new DecimalFormat();
            d2.setMaximumFractionDigits(2);
            d2.setMinimumFractionDigits(2);
            d2.setCurrency( Currency.getInstance("BRL"));
            d2.setDecimalFormatSymbols(symbols);            
            
            
            d0 = new DecimalFormat();
            d0.setMaximumFractionDigits(0);
            d0.setMinimumFractionDigits(0);
            d0.setCurrency( Currency.getInstance("BRL"));
    }


    public static String getSql(String sql, String file) throws FileNotFoundException {
        if (sql == null) {

            if (!Files.exists(Paths.get(file))) {
                throw new FileNotFoundException();
            }

            try {
                sql = new String(Files.readAllBytes(Paths.get(file)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sql;
    }
    
    public static String join(List<Integer> list){
        
        if (list.size() == 0) return null;
        String s = "" + list.get(0);
        
        for(int i = 1; i < list.size(); i++){
            s += ", " + list.get(i);
        }
        
        return s;
    }

    public static String joinFields(List<String> list){

        if (list.size() == 0) return null;
        String s = "" + list.get(0);

        for(int i = 1; i < list.size(); i++){
            s += ", " + list.get(i);
        }

        return s;
    }

    public static String joinCsvLine(List<String> list, String sep ){

        if (list.size() == 0) return null;
        String s = "" + list.get(0);

        for(int i = 1; i < list.size(); i++){
            s += sep + list.get(i);
        }

        return s;
    }
    
    public static String joinString(List<String> list){
        
        if (list.size() == 0) return null;
        String s = "'" + list.get(0) + "'";
        
        for(int i = 1; i < list.size(); i++){
            s += ", '" + list.get(i) + "'";
        }
        
        return s;
    }
    
    public static String UpperCaseWords(String line)
    {
            if (line== null) return null;
        line = line.trim().toLowerCase();
        String data[] = line.split("\\s");
        line = "";
        for(int i =0;i< data.length;i++)
        {
            if(data[i].length()>1)
                line = line + data[i].substring(0,1).toUpperCase()+data[i].substring(1)+" ";
            else
                line = line + data[i].toUpperCase();
        }
        return line.trim();
    }
    
    public static String h( Object o ){
        if( o == null )
                return "";
        if( o instanceof Float){
                return d2.format( (Float)o );
        }
        if( o instanceof Double){
                return d2.format( (Double)o );
        }
        if( o instanceof Integer ||  o instanceof Long){
                return o.toString();
        }
        if( o instanceof AtomicInteger || o instanceof AtomicLong || o instanceof BigInteger ||
                        o instanceof Byte){
                return d2.format( ((Number)o).longValue() );
        }
        if( o instanceof BigDecimal){
                return d2.format( ((BigDecimal)o).doubleValue() );
        }
        if( o instanceof Calendar){
                return format.format( ((Calendar)o).getTime() );
        }
        if( o instanceof Date){
                return format.format( (Date)o );
        }
        
        
        String s = o.toString();
        // return StringEscapeUtils.escapeHtml( s );
        return s.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;")
                        .replaceAll("\"", "&quot;").replaceAll("'", "&#039;");
        
       
    }
    
    /**
     * Varre um Map pegando somente um 'key' e inseri em uma lista do tipo especificado
     * @param map   fonte de dados
     * @param key   key que formará a lista
     * @param type  tipo de dados do map a qual o 'key' se refere
     * @return 
     */
    public static <T extends Object> List<T> listMapToList(List<Map<String,Object>> map, String key, Class<T> type){

        List<T> list = new ArrayList<>();
        
        for(Map<String,Object> m: map){
            if (m.containsKey(key) && m.get(key) != null){
                list.add(type.cast(m.get(key)));
            }            
        }
        
        return list;
    }


    public static String formatAsCsv(ResultSet rs, ResultSetMetaData rsmd, int columnLength,String sep) throws SQLException {
        String line = "";
        List<String> list = new ArrayList<>();

        for(int i = 0; i < columnLength; i++){
            int index = i + 1;
            //save file
            String stringValue = "";
            Object o = rs.getObject(index);
            int colType = rsmd.getColumnType(index);

            if (colType == 1 || colType == 12){
                stringValue = ((String)o);
                stringValue = stringValue != null ? "\"" + stringValue.trim() + "\"" : "\"\"";
            } else {
                stringValue = o != null ? (o).toString() : "";
            }

            stringValue = o != null ? (o).toString() : "";
            list.add(stringValue);
        }

        return Util.joinCsvLine(list,sep);
    }

    public static File getPublicDirectory(){
        ClassLoader classLoader = Util.class.getClassLoader();
        return new File(classLoader.getResource("public").getFile());
    }

    public static JSONObject readResourceConfigFile(String resourceFileName){
        JSONParser parser = new JSONParser();
        try {
            ClassLoader classLoader = parser.getClass().getClassLoader();
            File file = new File(classLoader.getResource(resourceFileName).getFile());
            if (!file.exists())
                file = new File("config.json");

            FileReader fileReader = new FileReader(file);

            return (JSONObject)parser.parse(fileReader);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String toCamelCase(String text){
        Matcher m = Pattern.compile("_([a-zA-Z])").matcher(text);
        StringBuilder sb = new StringBuilder();
        int last = 0;
        while (m.find()) {
            sb.append(text.substring(last, m.start()));
            sb.append(m.group(1).toUpperCase());
            last = m.end();
        }
        sb.append(text.substring(last));
        return sb.toString();
    }

    public static Map<String,Object> map(Object... o) {
        if ((o.length % 2) > 0) throw new RuntimeException("The number of params must be even");
        Map<String, Object> map = new LinkedHashMap<>();
        for(int i = 0; i < o.length; i+=2){
            map.put((String)o[i], o[i + 1]);
        }
        return map;
    }
}