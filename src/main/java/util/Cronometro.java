package util;

import java.util.Calendar;

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
