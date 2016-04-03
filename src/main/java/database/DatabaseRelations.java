package database;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by francisco on 03/04/16.
 */
public class DatabaseRelations {
    private final ConcurrentHashMap<String, DBRelation> relations = new ConcurrentHashMap<String, DBRelation>();
}
