package util;

import java.util.Calendar;

/**
 * Created by F3445038 on 19/12/2014.
 */
public class Cronometro {

    private Calendar time;

    public Cronometro(){
        time = Calendar.getInstance();
    }

    public void progress( String a ) {
        Calendar now = Calendar.getInstance();
        System.err.println( a + " " + (now.getTimeInMillis() - time.getTimeInMillis()) + " ms" );
        time = now;
    }
}
