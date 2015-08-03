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
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * (c) 2015 Cazcade Limited
 *
 * @author neilellis@sillelien.com
 */
public interface Tutum {

    /* Containers */

    /**
     * <strong>Create a new stack</strong>
     * <p>
     * <p> <a href="https://docs.tutum.co/v2/api/#create-a-new-stack">API Ref</a></p>
     * <p>
     * <p>Creates a new stack without starting it. Note that the JSON syntax is abstracted by both, the Tutum CLI and our UI, in order to use Stack YAML files</p>
     * <p>
     * <strong>HTTP REQUEST</strong>
     * <p>POST /api/v1/stack/</p>
     *
     * @param stackName     A human-readable name for the stack, i.e. my-hello-world-stack
     * @param stackServices List of services belonging to the stack.
     * @return a newly created stack
     * @throws TutumException if something goes wrong
     */
    TutumStack createStack(String stackName, List<TutumService> stackServices)  throws TutumException;

    /**
     * <strong>Get an existing stack</strong>
     * <p> <a href="https://docs.tutum.co/v2/api/#get-an-existing-stack">API Ref</a></p>
     * <p>Get all the details of an specific stack</p>
     * <strong>HTTP REQUEST</strong>
     * <p>GET /api/v1/stack/(uuid)/</p>
     *
     * @param uuid the id of the stack you wish to retrieve
     * @return an existing stack
     * @throws TutumException if something goes wrong
     */
    TutumStack getStack(String uuid) throws TutumException;

    /**
     * Returns an existing stack with the user specified name.
     *
     * @param name the name of the stack to retrieve
     * @return the stack, or null if there is no stack by that name
     * @throws TutumException if something goes wrong
     */
    TutumStack getStackByName(String name);


    /**
     * <strong>Start a stack</strong>
     * <p> <a href="https://docs.tutum.co/v2/api/#start-a-stack">API Ref</a></p>
     * <p>Starts the services in the stack</p>
     * <strong>HTTP REQUEST</strong>
     * <p>POST /api/v1/stack/(uuid)/start/</p>
     *
     * @param uuid the id of the stack you wish to start
     * @return the response from Tutum if any
     * @throws TutumException if something goes wrong
     */
    TutumResponse startStack(String uuid) throws TutumException;

    /**
     * Checks to see if a stack exists
     *
     * @param name the name of the stack
     * @param exists function to call if the stack exists
     * @param doesNotExist function to call if the stack does not exist
     * @return the return value of the called function
     * @throws Exception if something goes wrong
     */
    <T> T checkForStack(String name, ServiceExistsFunction<TutumStack, T> exists,
                         Callable<T> doesNotExist) throws Exception;





    /* Services */

    /**
     * Creates a service definition from the supplied key/values, see <a href="https://docs.tutum.co/v2/api/#service">Tutum API</a> for details of valid attributes.
     * @param kv a map of key/values suitable for creating a service from.
     *
     * @return a {@link TutumServiceDefinition} from which a live service can be built.
     */
    TutumServiceDefinition buildService(Map<String,Object> kv);

    /**
     * <strong>Create a new service</strong>
     * <p> <a href="https://docs.tutum.co/v2/api/#create-a-new-service">API Ref</a></p>
     * <p>Creates a new service without starting it</p>
     * <strong>HTTP REQUEST</strong>
     * <p>POST /api/v1/service/</p>
     *
     * @param service the definition of the service you wish to create see {@link Tutum#buildService}
     * @return the response from Tutum if any
     * @throws TutumException if something goes wrong
     */
    TutumService createService(TutumServiceDefinition service) throws TutumException;

    /**
     * <strong>Get an existing service</strong>
     * <p> <a href="https://docs.tutum.co/v2/api/#get-an-existing-service">API Ref</a></p>
     * <p>Get all the details of an specific service</p>
     * <strong>HTTP REQUEST</strong>
     * <p>GET /api/v1/service/(uuid)/</p>
     *
     * @param uuid the id of the service you wish to retrieve
     * @return an existing service
     * @throws TutumException if something goes wrong
     */
    TutumServiceImpl getService(String uuid) throws TutumException;

    /**
     * Retrives an existing service by it's URI.
     *
     * @see #getService(String)
     *
     * @param uri the URI of the service to retrieve
     * @return an existing service
     *
     * @throws TutumException if something goes wrong
     */
    TutumService getServiceByURI(String uri) throws TutumException;

    /**
     * Retrives an existing service by it's name.
     *
     * @see #getService(String)
     *
     * @param name the name of the service to retrieve
     * @return an existing service
     *
     * @throws TutumException if something goes wrong
     */
    TutumServiceImpl getServiceByName(String name);

    /**
     * <strong>Update an existing service</strong>
     * <p> <a href="https://docs.tutum.co/v2/api/#update-an-existing-service">API Ref</a></p>
     * <p>Updates the service details</p>
     * <strong>HTTP REQUEST</strong>
     * <p>PATCH /api/v1/service/(uuid)/</p>
     *
     * @param service the locally updated service definition
     *
     * @return an existing service
     * @throws TutumException if something goes wrong
     */
    TutumService updateService(TutumService service);



    /**
     * <strong>Starts a service</strong>
     * <p> <a href="https://docs.tutum.co/v2/api/#start-a-service">API Ref</a></p>
     * <p>Starts all containers in a stopped or partly running service.</p>
     * <strong>HTTP REQUEST</strong>
     * <p>POST /api/v1/service/(uuid)/start/</p>
     *
     * @param uuid the id of the service you wish to start
     * @return the response from Tutum if any
     * @throws TutumException if something goes wrong
     */
    TutumResponse startService(String uuid) throws TutumException;


    /**
     * Checks to see if a service exists
     *
     * @param name the name of the service
     * @param exists function to call if the service exists
     * @param doesNotExist function to call if the service does not exist
     * @return the return value of the called function
     * @throws Exception if something goes wrong
     */
    <T> T checkForService(String name, ServiceExistsFunction<TutumService, T> exists,
                           Callable<T> doesNotExist) throws Exception;


    /* Containers */

    /**
     * <strong>Get an existing container</strong>
     * <p> <a href="https://docs.tutum.co/v2/api/#get-an-existing-container">API Ref</a></p>
     * <p>Get all the details of an specific container</p>
     * <strong>HTTP REQUEST</strong>
     * <p>GET /api/v1/container/(uuid)/</p>
     *
     * @param path the ur/path of the container you wish to retrieve
     * @return an existing container
     * @throws TutumException if something goes wrong
     */
    TutumContainer getContainer(String path) throws TutumException;

    /**
     * <strong>Execute command inside a container</strong>
     * <p> <a href="https://docs.tutum.co/v2/api/#execute-command-inside-a-container">API Ref</a></p>
     * <p>Executes a command inside the specified running container, creating a bi-directional stream for the process' standard input and output</p>
     * <strong>HTTP REQUEST</strong>
     * <p>GET /v1/container/(uuid)/exec/</p>
     *
     * NB: Synchronous call, asynchronous version is TODO
     *
     * @param uuid the id the container you wish to execute the command on.
     * @param command the command to execute
     * @return the response from executing the command, upon termination
     * @throws TutumException if something goes wrong
     */
    TutumExecResponse exec(String uuid, String command) throws TutumException;


    /**
     * <strong>Execute command inside a container</strong>
     * <p> <a href="https://docs.tutum.co/v2/api/#execute-command-inside-a-container">API Ref</a></p>
     * <p>Executes a command inside the specified running container, creating a bi-directional stream for the process' standard input and output</p>
     * <strong>HTTP REQUEST</strong>
     * <p>GET /v1/container/(uuid)/exec/</p>
     *
     * NB: Synchronous call, asynchronous version is TODO
     *
     * @param container the container you wish to execute the command on.
     * @param command the command to execute
     * @return the response from executing the command, upon termination
     * @throws TutumException if something goes wrong
     */
    TutumExecResponse exec(TutumContainer container, String command) throws TutumException;


    /**
     * Creates a link between two named services
     * @param alias the name of the link
     * @param serviceFrom the name of the service to link from
     * @param serviceTo the name of the service to link to
     */
    void linkServices(String alias, String serviceFrom, String serviceTo);








}
