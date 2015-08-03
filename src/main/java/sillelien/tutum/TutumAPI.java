package sillelien.tutum;

import com.github.oxo42.stateless4j.StateMachineConfig;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sillelien.dollar.api.var;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.sillelien.dollar.api.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class TutumAPI implements Tutum {

    private static final Map<String, String> AUTH_HEADERS;
    private static final Logger log= LoggerFactory.getLogger(TutumAPI.class);

    public static final int MAX_RETRY = 3;
    public static final int RETRY_DELAY = 2000;
    public static final int GET_OPERATION_CACHE_MILLI = 1000;
    public static final int CHECK_FOR_SERVICE_CACHE_MILLI = 10000;
    public static StateMachineConfig<ServiceState, ServiceAction> state;

    private static SuperSimpleCache<String, var> cache = new SuperSimpleCache<>(100000);

    private static final String user;
    private static final String key;

    static {
        String auth = System.getenv("TUTUM_AUTH");
        if(auth == null) {
            throw new RuntimeException("You must supply an environment variable TUTUM_AUTH unless you are running in a Tutum container in which case it is not needed.");
        }
        String[] authSplit = auth.split(" ");
        if(authSplit.length != 2) {
            throw new RuntimeException("Could not parse the key from "+auth);
        }
        String userKey = authSplit[1];
        String[] userKeySplit = userKey.split(":");
        if(userKeySplit.length != 2) {
            throw new RuntimeException("Could not parse the user/key from "+auth);
        }
        user=userKeySplit[0];
        key=userKeySplit[1];

        AUTH_HEADERS = $("Authorization", auth).$(
                "Accept", "application/json").toMap();

        state = new StateMachineConfig<>();
        state.configure(ServiceState.INIT)
                .permit(ServiceAction.START, ServiceState.STARTING)
                .permit(ServiceAction.TERMINATE, ServiceState.TERMINATING);
        state.configure(ServiceState.STARTING);
        state.configure(ServiceState.RUNNING)
                .permit(ServiceAction.STOP, ServiceState.STOPPING)
                .permit(ServiceAction.TERMINATE, ServiceState.TERMINATING)
                .permit(ServiceAction.REDEPLOY, ServiceState.REDEPLOYING);
        state.configure(ServiceState.PARTLY_RUNNING)
                .permit(ServiceAction.STOP, ServiceState.STOPPING)
                .permit(ServiceAction.TERMINATE, ServiceState.TERMINATING)
                .permit(ServiceAction.REDEPLOY, ServiceState.REDEPLOYING);
        state.configure(ServiceState.STOPPED)
                .permit(ServiceAction.START, ServiceState.STARTING)
                .permit(ServiceAction.TERMINATE, ServiceState.TERMINATING)
                .permit(ServiceAction.REDEPLOY, ServiceState.REDEPLOYING);
        state.configure(ServiceState.NOT_RUNNING)
                .permit(ServiceAction.TERMINATE, ServiceState.TERMINATING);
    }

    @Override
    public TutumStack createStack(String stackName, List<TutumService> stackServices) {
        final List<var> services = stackServices.stream().map(TutumService::asVar).collect(Collectors.toList());
        return createStackFromVar(stackName, services);
    }


    @Override
    public void linkServices(String linkName, String serviceFrom, String serviceTo) {
        TutumService fromService = getServiceByName(serviceFrom);
        TutumService toService = getServiceByName(serviceTo);
        toService.link(linkName, fromService.uri());
        updateService(toService);
    }

    @Override
    public TutumService updateService(TutumService service) {

        HttpResponse<String> jsonResponse = null;
        try {
            jsonResponse = Unirest.patch("https://dashboard.tutum.co/api/v1/service/" + service.uuid() + "/")
                    .headers(AUTH_HEADERS).header("Content-Type", "application/json")
                    .body(service.asUpdate())
                    .asString();
            if (jsonResponse.getStatus() >= 400) {
                Bug.report("Failed to update service " +
                        service.name() +
                        " response was " +
                        jsonResponse.getStatus() +
                        ": " +
                        jsonResponse.getStatusText(), service.asVar(), $(jsonResponse.getBody()));
                throw new TutumException("Could not update service");
            }
        } catch (UnirestException e) {
            Bug.report(e, service.asVar());
            throw new TutumException(e);
        }
        return new TutumService($(jsonResponse.getBody()));
    }

    public TutumStack createStackFromVar(String stackName, List<var> services) {
        HttpResponse<String> jsonResponse = null;
        final var stack = $("name", stackName).$("services", $(services));
        try {
            jsonResponse = Unirest.post("https://dashboard.tutum.co/api/v1/stack/")
                    .headers(AUTH_HEADERS)
                    .header("Content-Type", "application/json")
                    .body(stack.toString())
                    .asString();
            if (jsonResponse.getStatus() >= 400) {
                Bug.report("Failed to create stack " +
                        stackName +
                        " response was " +
                        jsonResponse.getStatus() +
                        ": " +
                        jsonResponse.getStatusText(), stack, $(jsonResponse.getBody()));
                throw new TutumException("Could not create service", jsonResponse.getStatus());
            }
        } catch (UnirestException e) {
            Bug.report(e, stack);
            throw new TutumException(e);

        }
        return new TutumStack($(jsonResponse.getBody()));
    }

    @Override
    public TutumService getServiceByName(String name) {
        final var response = cache.getOrCreate("get_service_by_name:" + name, CHECK_FOR_SERVICE_CACHE_MILLI, () -> {
            HttpResponse<String> jsonResponse = null;
            try {
                jsonResponse = Unirest.get("https://dashboard.tutum.co/api/v1/service")
                        .headers(AUTH_HEADERS).queryString("name", name)
                        .asString();
                if (jsonResponse.getStatus() >= 400) {
                    Bug.report("Failed to check for service " +
                            name +
                            " response was " +
                            jsonResponse.getStatus() +
                            ": " +
                            jsonResponse.getStatusText(), $(name), $(jsonResponse.getBody()));
                    throw new TutumException("Could not start service");
                }
            } catch (UnirestException e) {
                Bug.report(e, $(name));
                throw new TutumException(e);

            }
            log.debug("Uncached response");
            return $(jsonResponse.getBody());

        });
        log.debug("Response was " + response.toHumanString());
        final var objects = response.$("objects");
        if (objects.$isEmpty().isTrue()) {
            return null;
        }
        TutumService tutumService = new TutumService(objects.$list().get(0));
        return getService(tutumService.uuid());
    }

    @Override
    public TutumStack getStackByName(String name) {
        final var response = cache.getOrCreate("get_stack_by_name:" + name, CHECK_FOR_SERVICE_CACHE_MILLI, () -> {
            HttpResponse<String> jsonResponse = null;
            try {
                jsonResponse = Unirest.get("https://dashboard.tutum.co/api/v1/stack")
                        .headers(AUTH_HEADERS).queryString("name", name)
                        .asString();
                if (jsonResponse.getStatus() >= 400) {
                    Bug.report("Failed to get stack " +
                            name +
                            " response was " +
                            jsonResponse.getStatus() +
                            ": " +
                            jsonResponse.getStatusText(), $(name), $(jsonResponse.getBody()));
                    throw new TutumException("Could not get stack");
                }
            } catch (UnirestException e) {
                Bug.report(e, $(name));
                throw new TutumException(e);

            }
            log.debug("Uncached response");
            return $(jsonResponse.getBody());

        });
        log.debug("Response was " + response.toHumanString());
        final var objects = response.$("objects");
        if (objects.$isEmpty().isTrue()) {
            return null;
        }
        TutumStack tutumService = new TutumStack(objects.$list().get(0));
        return getStack(tutumService.uuid());
    }


    @Override
    public String checkForService(String name, ServiceExistsFunction<TutumService, String> exists,
                                  Callable<String> doesNotExist) throws
            Exception {
        final var response = cache.getOrCreate("check_for_service:" + name, CHECK_FOR_SERVICE_CACHE_MILLI, () -> {
            HttpResponse<String> jsonResponse = null;
            try {
                jsonResponse = Unirest.get("https://dashboard.tutum.co/api/v1/service")
                        .headers(AUTH_HEADERS).queryString("name", name)
                        .asString();
                if (jsonResponse.getStatus() >= 400) {
                    Bug.report("Failed to check for service " +
                            name +
                            " response was " +
                            jsonResponse.getStatus() +
                            ": " +
                            jsonResponse.getStatusText(), $(name), $(jsonResponse.getBody()));
                    throw new TutumException("Could not start service");
                }
            } catch (UnirestException e) {
                Bug.report(e, $(name));
                throw new TutumException(e);

            }
            log.debug("Uncached response");
            return $(jsonResponse.getBody());

        });
        log.debug("Response was " + response.toHumanString());
        final var objects = response.$("objects");
        if (objects.$isEmpty().isTrue()) {
            log.debug("Does not exist");
            return doesNotExist.call();
        }
        TutumService tutumService = new TutumService(objects.$list().get(0));
        log.debug("Service exists and is " + tutumService.toString());
        return exists.apply(tutumService);
    }


    @Override
    public String checkForStack(String name, ServiceExistsFunction<TutumStack, String> exists,
                                Callable<String> doesNotExist) throws
            Exception {
        final var response = cache.getOrCreate("check_for_stack:" + name, CHECK_FOR_SERVICE_CACHE_MILLI, () -> {
            HttpResponse<String> jsonResponse = null;
            try {
                jsonResponse = Unirest.get("https://dashboard.tutum.co/api/v1/stack")
                        .headers(AUTH_HEADERS).queryString("name", name)
                        .asString();
                if (jsonResponse.getStatus() >= 400) {
                    Bug.report("Failed to check for stack " +
                            name +
                            " response was " +
                            jsonResponse.getStatus() +
                            ": " +
                            jsonResponse.getStatusText(), $(name), $(jsonResponse.getBody()));
                    throw new TutumException("Could not start service");
                }
            } catch (UnirestException e) {
                Bug.report(e, $(name));
                throw new TutumException(e);

            }
            log.debug("Uncached response");
            return $(jsonResponse.getBody());

        });
        log.debug("Response was " + response.toHumanString());
        final var objects = response.$("objects");
        if (objects.$isEmpty().isTrue()) {
            log.debug("Does not exist");
            return doesNotExist.call();
        }
        TutumStack tutumService = new TutumStack(objects.$list().get(0));
        log.debug("Service exists and is " + tutumService.toString());
        return exists.apply(tutumService);
    }


    @Override
    public TutumResponse startService(String uuid) throws TutumException {
        HttpResponse<String> jsonResponse = null;
        try {
            jsonResponse = Unirest.post("https://dashboard.tutum.co/api/v1/service/" + uuid + "/start/")
                    .headers(AUTH_HEADERS)
                    .asString();
            if (jsonResponse.getStatus() >= 400) {
                Bug.report("Failed to start service " +
                        uuid +
                        " response was " +
                        jsonResponse.getStatus() +
                        ": " +
                        jsonResponse.getStatusText(), $(uuid), $(jsonResponse.getBody()));
                throw new TutumException("Could not start service");
            }
        } catch (UnirestException e) {
            Bug.report(e, $(uuid));
            throw new TutumException(e);
        }
        return new TutumResponse($(jsonResponse.getBody()));
    }


    @Override
    public TutumResponse startStack(String uuid) throws TutumException {
        HttpResponse<String> jsonResponse = null;
        try {
            jsonResponse = Unirest.post("https://dashboard.tutum.co/api/v1/stack/" + uuid + "/start/")
                    .headers(AUTH_HEADERS)
                    .asString();
            if (jsonResponse.getStatus() >= 400) {
                Bug.report("Failed to start stack " +
                        uuid +
                        " response was " +
                        jsonResponse.getStatus() +
                        ": " +
                        jsonResponse.getStatusText(), $(uuid), $(jsonResponse.getBody()));
                throw new TutumException(jsonResponse.getStatus() + ": Could not start stack");
            }
        } catch (UnirestException e) {
            Bug.report(e, $(uuid));
            throw new TutumException(e);
        }
        return new TutumResponse($(jsonResponse.getBody()));
    }


    @Override
    public TutumService getServiceByURI(String uri) throws TutumException {
        if (!uri.startsWith("/")) {
            throw new TutumException("Uri '" + uri + "' is invalid.");
        }
        final var response = cache.getOrCreate("get_service_by_uri:" + uri, GET_OPERATION_CACHE_MILLI, () -> {
            HttpResponse<String> jsonResponse = null;
            try {
                jsonResponse = getWithRetry("https://dashboard.tutum.co" + uri);
            } catch (UnirestException e) {
                Bug.report(e, $(uri));
                throw new TutumException(e);
            }
            if (jsonResponse.getStatus() >= 400) {
                Bug.report("Failed to get service " +
                        uri +
                        " response was " +
                        jsonResponse.getStatus() +
                        ": " +
                        jsonResponse.getStatusText(), $(uri), $(jsonResponse.getBody()));
                throw new TutumException("Could not retrieve service");
            }
            return $(jsonResponse.getBody());
        });
        return new TutumService(response);

    }

    @Override
    public TutumService getService(String uuid) throws TutumException {
        final var response = cache.getOrCreate("get_service:" + uuid, GET_OPERATION_CACHE_MILLI, () -> {
            HttpResponse<String> jsonResponse = null;
            try {
                jsonResponse = getWithRetry("https://dashboard.tutum.co/api/v1/service/" + uuid);
            } catch (UnirestException e) {
                Bug.report(e, $(uuid));
                throw new TutumException(e);
            }
            if (jsonResponse.getStatus() >= 400) {
                Bug.report("Failed to get service " +
                        uuid +
                        " response was " +
                        jsonResponse.getStatus() +
                        ": " +
                        jsonResponse.getStatusText(), $(uuid), $(jsonResponse.getBody()));
                throw new TutumException("Could not retrieve service");
            }
            return $(jsonResponse.getBody());
        });
        return new TutumService(response);

    }

    @Override
    public TutumStack getStack(String uuid) throws TutumException {
        final var response = cache.getOrCreate("get_stack:" + uuid, GET_OPERATION_CACHE_MILLI, () -> {
            HttpResponse<String> jsonResponse = null;
            try {
                jsonResponse = getWithRetry("https://dashboard.tutum.co/api/v1/stack/" + uuid);
            } catch (UnirestException e) {
                Bug.report(e, $(uuid));
                throw new TutumException(e);
            }
            if (jsonResponse.getStatus() >= 400) {
                Bug.report("Failed to get stack " +
                        uuid +
                        " response was " +
                        jsonResponse.getStatus() +
                        ": " +
                        jsonResponse.getStatusText(), $(uuid), $(jsonResponse.getBody()));
                throw new TutumException("Could not retrieve stack");
            }
            return $(jsonResponse.getBody());
        });
        return new TutumStack(response);

    }


    @Override
    public TutumService createService(TutumService service) throws TutumException {
        HttpResponse<String> jsonResponse = null;
        log.debug(service.toString());
        try {
            jsonResponse = Unirest.post("https://dashboard.tutum.co/api/v1/service/")
                    .headers(AUTH_HEADERS)
                    .header("Content-Type", "application/json")
                    .body(service.toString())
                    .asString();
            if (jsonResponse.getStatus() >= 400) {
                Bug.report("Failed to create service " +
                        service.uuid() +
                        " response was " +
                        jsonResponse.getStatus() +
                        ": " +
                        jsonResponse.getStatusText(), service.asVar(), $(jsonResponse.getBody()));
                throw new TutumException("Could not create service", jsonResponse.getStatus());
            }
        } catch (UnirestException e) {
            Bug.report(e, service.asVar());
            throw new TutumException(e);

        }
        return new TutumService($(jsonResponse.getBody()));
    }


    @Override
    public TutumContainer getContainer(String containerUrl) throws TutumException {
        final var response = cache.getOrCreate("get_container:" + containerUrl, GET_OPERATION_CACHE_MILLI, () -> {
            HttpResponse<String> jsonResponse = null;
            try {
                final String url = "https://dashboard.tutum.co/" + containerUrl;
                jsonResponse = getWithRetry(url);
                if (jsonResponse.getStatus() >= 400) {
                    Bug.report("Failed to get container " +
                            containerUrl +
                            " response was " +
                            jsonResponse.getStatus() +
                            ": " +
                            jsonResponse.getStatusText(), $(containerUrl), $(jsonResponse.getBody()));
                    throw new TutumException("Could not retrieve container");
                }
            } catch (UnirestException e) {
                Bug.report(e, $(containerUrl));
                throw new TutumException(e);
            }
            return $(jsonResponse.getBody());
        });
        return new TutumContainer(response);

    }


    @Override
    public TutumExecResponse exec(TutumContainer container, String command) throws TutumException {
        try {
            CompletableFuture<TutumExecResponse> future = new CompletableFuture<>();
            List<var> output = new ArrayList<>();
            String url = "wss://stream.tutum.co/v1/container/" + container.uuid() + "/exec/?command="+ URLEncoder.encode(command)+"&user=" + user + "&token=" + key;
            log.debug("Url is "+url);
            WebSocketClient socketClient = new WebSocketClient(new URI(url)) {
                @Override
                public void onMessage(String message) {
                    log.debug("Received "+message);
                    output.add($(message));
                }

                @Override
                public void onOpen(ServerHandshake handshake) {
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.debug("Done "+reason+" "+code);
                    future.complete(new TutumExecResponse(output));
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }

            };
            SSLContext sslContext = null;
            sslContext = SSLContext.getInstance( "TLS" );
            sslContext.init( null, null, null ); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates

            socketClient.setWebSocketFactory( new DefaultSSLWebSocketClientFactory( sslContext ) );
            socketClient.connectBlocking();
            return future.get();
        } catch (NoSuchAlgorithmException | KeyManagementException | ExecutionException | InterruptedException | URISyntaxException e) {
            throw new TutumException(e);
        }

    }

    private HttpResponse<String> getWithRetry(String url) throws UnirestException {
        HttpResponse<String> jsonResponse = null;
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                jsonResponse = Unirest.get(url)
                        .headers(AUTH_HEADERS)
                        .asString();
                return jsonResponse;
            } catch (UnirestException e) {
                if (i == MAX_RETRY - 1) {
                    throw e;
                } else {
                    try {
                        Thread.sleep(RETRY_DELAY);
                    } catch (InterruptedException e1) {
                        throw new Error(e1);
                    }
                }
            }
        }
        return null;
    }

}
