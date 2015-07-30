package sillelien.tutum;

import me.neilellis.dollar.api.var;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class TutumPort  {
    private final var json;

    public TutumPort(var json) {this.json = json;}

    public String endpointUri() {
        return json.$("endpoint_uri").toString();
    }

    public boolean hasEndpointUri() {
        return !json.$("endpoint_uri").isNull();
    }

    public boolean hasOuterPort() {
        final var outerPort = json.$("outer_port");
        return !outerPort.isVoid();
    }


    public int outerPort() {
        return json.$("outer_port").toInteger();
    }

    public String toString() {
        return json.toString();
    }
}
