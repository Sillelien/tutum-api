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

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class TutumContainerImpl extends VarBackedTutumObject implements TutumContainer  {

    public TutumContainerImpl(var json) {

        this.json = json;
    }

    @Override
    public List<TutumPort> ports() {
        return json.$("container_ports").$list().stream().map(TutumPortImpl::new).collect(Collectors.toList());
    }

    @Override
    public String publicDns() {
        return json.$("public_dns").toString();
    }

    @Override
    public String url() {
        return null;
    }

}
