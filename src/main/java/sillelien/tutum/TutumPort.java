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

import com.sillelien.dollar.api.var;

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
