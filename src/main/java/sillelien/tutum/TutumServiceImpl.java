/*
 * Copyright (c) 2015 Sillelien
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package sillelien.tutum;

import com.sillelien.dollar.api.collections.ImmutableMap;
import com.sillelien.dollar.api.var;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.sillelien.dollar.api.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class TutumServiceImpl extends VarBackedTutumObject implements TutumService {


    private static final List<String> UPDATE_KEYS = Arrays.asList("autorestart", "autodestroy", "container_envvars",
            "container_ports", "cpu_shares",
            "entrypoint", "image",
            "linked_to_service",
            "memory", "privileged", "roles", "run_command", "sequential_deployment",
            "tags", "target_num_containers");



    public TutumServiceImpl(var json) {

        this.json = json;
    }

    public String asJsonString() {
        return json.toJsonObject().toString();
    }




    @Override
    public List<String> containers() {
        return json.$("containers").$list().stream().map(var::toString).collect(Collectors.toList());
    }

    @Override
    public boolean isRunning() {
        return json.$("state").toString().equalsIgnoreCase("RUNNING");
    }

    @Override
    public boolean isStarting() {
        return json.$("state").toString().equalsIgnoreCase("STARTING");
    }

    @Override
    public String state() {
        return json.$("state").toString();
    }

    @Override
    public String name() {

        return json.$("name").toString();
    }

    public void link(String linkName, String uri) {
        json = json.$("linked_to_service", json.$("linked_to_service").$append($("to_service", uri).$("name", linkName)));
    }

    @Override
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

    @Override
    public List<TutumPort> containerPorts() {
        return json.$("container_ports").$list().stream().map(TutumPort::new).collect(Collectors.toList());
    }


}
