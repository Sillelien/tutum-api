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

import java.util.Map;

/**
 * (c) 2015 Cazcade Limited
 *
 * @author neil@cazcade.com
 */
public class VarBackedTutumObject {

    com.sillelien.dollar.api.var json;

    public Map<String, Object> asMap() {
        return json.toMap();
    }

    public String uri() {
        return json.$("resource_uri").toString();
    }

    @Override
    public String toString() {
        return json.toString();
    }

    public String uuid() {
        return json.$("uuid").toString();
    }

    public var asVar() {
        return json;
    }

    public String asJsonString() {
        return json.toJsonObject().toString();
    }
}
