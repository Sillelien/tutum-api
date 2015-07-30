package sillelien.tutum;

import me.neilellis.dollar.api.var;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class TutumStack {



    private final var json;

    public TutumStack(var json) {

        this.json = json;
    }

    public String asJsonString() {
        return json.toJsonObject().toString();
    }

    public Map asMap() {
        return json.toMap();
    }

    public var asVar() {
        return json;
    }

    public boolean isRunning() {
        return json.$("state").toString().equalsIgnoreCase("RUNNING");
    }

    public boolean isStarting() {
        return json.$("state").toString().equalsIgnoreCase("STARTING");
    }

    public String state() {
        return json.$("state").toString();
    }


    public String name() {
        return json.$("name").toString();
    }
    public String uuid() {
        return json.$("uuid").toString();
    }

    public String uri() {
        return json.$("resource_uri").toString();
    }

    public List<TutumService> services() {
        return json.$("services").$list().stream().map(TutumService::new).collect(Collectors.toList());
    }

    @Override public String toString() {
        return json.toJsonObject().toString();
    }
}
