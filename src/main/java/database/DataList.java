package database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by francisco on 16/04/16.
 */
public class DataList extends HashMap {

    private List<List<Object>> dataList = new ArrayList<>();

    public DataList(){
        this.put("columns", new ArrayList<>());
        this.put("data", dataList);
    }

    public void setColumns(List<String> cols) {
        this.put("columns", cols);
    }

    public void addRow(List<Object> objects) {
        dataList.add(objects);
    }
}