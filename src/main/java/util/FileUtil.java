package util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by francisco on 13/04/16.
 */
public class FileUtil {

    public static void close(Closeable closable){
        try {
            if (closable != null)
                closable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
