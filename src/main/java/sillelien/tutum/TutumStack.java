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

import java.util.List;

/**
 * @author neilellis@sillelien.com
 */
public interface TutumStack {

    /**
     * Returns true if the stack is currently in a Running state.
     *
     * @return true if running
     */
    boolean isRunning();

    /**
     * Returns true if the stack is currently in a Starting state.
     *
     * @return true if running
     */
    boolean isStarting();

    /**
     * A user provided name for the stack.
     *
     * @return a user provided name for the stack.
     */
    String name();

    /**
     * A unique identifier for the stack generated automatically on creation
     * @return a unique identifier for the stack generated automatically on creation
     */
    String uuid();

    /**
     * A unique API endpoint that represents the stack.
     *
     * @return a unique API endpoint that represents the stack.
     */
    String uri();

    /**
     * A list of services that make up the stack
     *
     * @return a list of services.
     */
    List<TutumService> services();
}
