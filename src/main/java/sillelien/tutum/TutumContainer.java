package sillelien.tutum;

import com.sillelien.dollar.api.var;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class TutumContainer {
    private final var json;

    public TutumContainer(var json) {

        this.json = json;
    }

    public Map<String, Object> asMap() {
        return json.toMap();
    }

    public List<TutumPort> ports() {
        return json.$("container_ports").$list().stream().map(TutumPort::new).collect(Collectors.toList());
    }

    public String publicDns() {
        return json.$("public_dns").toString();
    }

    public String url() {
        return null;
    }

    @Override public String toString() {
        return json.toString();
    }

    public String uuid() {
        return json.$("uuid").toString();
    }
}
