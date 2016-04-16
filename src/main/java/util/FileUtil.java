package util;

import org.apache.commons.fileupload.FileItem;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

    public static void saveFile(FileItem item, String filename) {

        try (BufferedInputStream bis = new BufferedInputStream(item.getInputStream())){

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename));
            byte[] buffer = new byte[1024];

            while (bis.read(buffer) != -1) {
                bos.write(buffer);
            }

            close(bos);
            close(bis);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void copy(String from, String to) {
        File newFile = new File(to);
        if (newFile.exists()) return;

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(from)));
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile))){
            byte[] buffer = new byte[1024];
            int read;
            while ((read = (bis.read(buffer))) != -1) {
                bos.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveFiles(List<FileItem> itens, String toDirectory) {
        if (itens == null) return;

        itens.forEach((file) -> {
            saveFile(file, toDirectory + file.getName());
        });
    }

    public static String readTextFile(String fileName) {
        try {
            return String.join("\n", Files.readAllLines(Paths.get(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sql(String sqlFile){
        String baseDir = System.getProperty("user.dir");
        sqlFile = sqlFile.endsWith(".sql") ? sqlFile : sqlFile + ".sql";
        return readTextFile(String.format("%s/src/main/resources/sql/%s", baseDir, sqlFile));
    }
}