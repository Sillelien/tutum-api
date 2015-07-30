package sillelien.tutum;

import com.github.oxo42.stateless4j.StateMachineConfig;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import me.neilellis.dollar.api.var;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static me.neilellis.dollar.api.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class TutumAPI {

    private static final Map<String, String> AUTH_HEADERS;
    public static final int MAX_RETRY = 3;
    public static final int RETRY_DELAY = 2000;
    public static final int GET_OPERATION_CACHE_MILLI = 1000;
    public static final int CHECK_FOR_SERVICE_CACHE_MILLI = 10000;
    public static StateMachineConfig<ServiceState, ServiceAction> state;

    private static SuperSimpleCache<String, var> cache = new SuperSimpleCache<>(100000);

    static {
        AUTH_HEADERS = $("Authorization", "ApiKey neilellis:5e212d0b326312b42ba776ce90278254fe1cc626").$(
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

    public TutumStack createStack(String stackName, List<TutumService> stackServices) {
        final List<var> services = stackServices.stream().map(TutumService::asVar).collect(Collectors.toList());
        return createStackFromVar(stackName, services);
    }


    public void linkServices(String linkName, String serviceFrom, String serviceTo) {
        TutumService fromService = getServiceByName(serviceFrom);
        TutumService toService = getServiceByName(serviceTo);
        toService.link(linkName, fromService.uri());
        updateService(toService);
    }

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
            System.out.println("Uncached response");
            return $(jsonResponse.getBody());

        });
        System.out.println("Response was " + response.toHumanString());
        final var objects = response.$("objects");
        if (objects.$isEmpty().isTrue()) {
            return null;
        }
        TutumService tutumService = new TutumService(objects.$list().get(0));
        return getService(tutumService.uuid());
    }

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
            System.out.println("Uncached response");
            return $(jsonResponse.getBody());

        });
        System.out.println("Response was " + response.toHumanString());
        final var objects = response.$("objects");
        if (objects.$isEmpty().isTrue()) {
            return null;
        }
        TutumStack tutumService = new TutumStack(objects.$list().get(0));
        return getStack(tutumService.uuid());
    }


    public interface ServiceExistsFunction<T, R> {
        R apply(T t) throws Exception;

    }

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
            System.out.println("Uncached response");
            return $(jsonResponse.getBody());

        });
        System.out.println("Response was " + response.toHumanString());
        final var objects = response.$("objects");
        if (objects.$isEmpty().isTrue()) {
            System.out.println("Does not exist");
            return doesNotExist.call();
        }
        TutumService tutumService = new TutumService(objects.$list().get(0));
        System.out.println("Service exists and is " + tutumService.toString());
        return exists.apply(tutumService);
    }


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
            System.out.println("Uncached response");
            return $(jsonResponse.getBody());

        });
        System.out.println("Response was " + response.toHumanString());
        final var objects = response.$("objects");
        if (objects.$isEmpty().isTrue()) {
            System.out.println("Does not exist");
            return doesNotExist.call();
        }
        TutumStack tutumService = new TutumStack(objects.$list().get(0));
        System.out.println("Service exists and is " + tutumService.toString());
        return exists.apply(tutumService);
    }


    public var startService(String uuid) throws TutumException {
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
        return $(jsonResponse.getBody());
    }


    public var startStack(String uuid) throws TutumException {
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
        return $(jsonResponse.getBody());
    }


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


    public TutumService createService(TutumService service) throws TutumException {
        HttpResponse<String> jsonResponse = null;
        System.out.println(service.toString());
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
