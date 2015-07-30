package sillelien.tutum;

import me.neilellis.dollar.api.collections.ImmutableMap;
import me.neilellis.dollar.api.var;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static me.neilellis.dollar.api.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class TutumService {


    private static final List<String> UPDATE_KEYS = Arrays.asList("autorestart", "autodestroy", "container_envvars",
            "container_ports", "cpu_shares",
            "entrypoint", "image",
            "linked_to_service",
            "memory", "privileged", "roles", "run_command", "sequential_deployment",
            "tags", "target_num_containers");

    private var json;

    public TutumService(var json) {

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

    public List<String> containers() {
        return json.$("containers").$list().stream().map(var::toString).collect(Collectors.toList());
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

    public String uri() {
        return json.$("resource_uri").toString();
    }


    public String uuid() {
        return json.$("uuid").toString();
    }

    @Override
    public String toString() {
        return json.toJsonObject().toString();
    }

    public String name() {

        return json.$("name").toString();
    }

    public void link(String linkName, String uri) {
        json = json.$("linked_to_service", json.$("linked_to_service").$append($("to_service", uri).$("name", linkName)));
    }

    public String asUpdate() {
        ImmutableMap<var, var> entries = json.$map();
        Map<var, var> result = new TreeMap<>();
        for (Map.Entry<var, var> entry : entries) {
            if (UPDATE_KEYS.contains(entry.getKey().toString())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return $(result).toJsonObject().toString();
    }

    public List<TutumPort> containerPorts() {
        return json.$("container_ports").$list().stream().map(TutumPort::new).collect(Collectors.toList());
    }

}
