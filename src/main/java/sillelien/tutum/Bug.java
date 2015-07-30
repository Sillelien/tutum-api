package sillelien.tutum;

import me.neilellis.dollar.api.var;

/**
 * (c) 2015 Cazcade Limited
 *
 * @author neil@cazcade.com
 */
public class Bug {

    static {

    }


    public static void report(Throwable t, var... data) {
        t.printStackTrace(System.err);
        for (var var : data) {
            System.err.println(data);
        }

    }

    public static void report(String s, var... data) {
        System.err.println(s);

    }
}
