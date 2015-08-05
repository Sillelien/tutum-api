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
 *
 *
 * @author neilellis@sillelien.com
 *
 */
public class TutumPortImpl extends VarBackedTutumObject implements TutumPort {

    public TutumPortImpl(var json) {this.json = json;}

    @Override
    public String endpointUri() {
        return json.$("endpoint_uri").toString();
    }

    @Override
    public boolean hasEndpointUri() {
        return !json.$("endpoint_uri").isNull();
    }

    @Override
    public boolean hasOuterPort() {
        final var outerPort = json.$("outer_port");
        return !outerPort.isVoid();
    }

    @Override
    public int outerPort() {
        return json.$("outer_port").toInteger();
    }

}
