package sillelien.tutum;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * (c) 2015 Cazcade Limited
 *
 * @author neil@cazcade.com
 */
public interface Tutum {

    /**
     * <strong>Create a new stack</strong>
     *
     * <p> <a href="https://docs.tutum.co/v2/api/#create-a-new-stack">API Ref</a></p>
     *
     * <p>Creates a new stack without starting it. Note that the JSON syntax is abstracted by both, the Tutum CLI and our UI, in order to use Stack YAML files</p>
     *
     * <strong>HTTP REQUEST</strong>
     * <p>POST /api/v1/stack/</p>
     * @param stackName  A human-readable name for the stack, i.e. my-hello-world-stack
     * @param stackServices List of services belonging to the stack.
     * @return
     */
    TutumStack createStack(String stackName, List<TutumService> stackServices);

    void linkServices(String linkName, String serviceFrom, String serviceTo);

    TutumService updateService(TutumService service);

    TutumService getServiceByName(String name);

    TutumStack getStackByName(String name);

    String checkForService(String name, ServiceExistsFunction<TutumService, String> exists,
                           Callable<String> doesNotExist) throws
            Exception;

    String checkForStack(String name, ServiceExistsFunction<TutumStack, String> exists,
                         Callable<String> doesNotExist) throws
            Exception;

    TutumResponse startService(String uuid) throws TutumException;

    TutumResponse startStack(String uuid) throws TutumException;

    TutumService getServiceByURI(String uri) throws TutumException;

    TutumService getService(String uuid) throws TutumException;

    TutumStack getStack(String uuid) throws TutumException;

    TutumService createService(TutumService service) throws TutumException;

    TutumContainer getContainer(String containerUrl) throws TutumException;

    TutumExecResponse exec(TutumContainer container, String command) throws TutumException;
}
