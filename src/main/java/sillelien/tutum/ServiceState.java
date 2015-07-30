package sillelien.tutum;

/**
* @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
*/
public enum ServiceState {
    INIT("The service has been created and has no deployed containers yet. Possible actions in this state: start," +
         " terminate."),
    STARTING(
            "All containers for the service are either starting or already running. No actions allowed in this " +
            "state."),
    RUNNING("All containers for the service are deployed and running. Possible actions in this state: stop, " +
            "redeploy, terminate."),
    PARTLY_RUNNING(
            "One or more containers of the service are deployed and running. Possible actions in this state: " +
            "stop, redeploy, terminate."),
    SCALING("The service is either deploying new containers or destroying existing ones responding to a scaling " +
            "request. No actions allowed in this state."),
    REDEPLOYING(
            "The service is redeploying all its containers with the updated configuration. No actions allowed in " +
            "this state."),
    STOPPING(
            "All containers for the service are either stopping or already stopped. No actions allowed in this " +
            "state."),
    STOPPED("All containers for the service are stopped. Possible actions in this state: start, redeploy, " +
            "terminate."),
    TERMINATING(
            "All containers for the service are either being terminated or already terminated. No actions allowed" +
            " in this state."),
    TERMINATED("The service and all its containers have been terminated. No actions allowed in this state."),
    NOT_RUNNING(
            "There are no containers to be deployed for this service. Possible actions in this state: terminate.");

    ServiceState(String s, String ... actions) {

    }

    public static ServiceState fromStateString(String s) {
        return valueOf(s.replace(' ', '_').toUpperCase());

    }

}
