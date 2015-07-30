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
