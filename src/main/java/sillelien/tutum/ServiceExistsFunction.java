package sillelien.tutum;

/**
 * (c) 2015 Cazcade Limited
 *
 * @author neil@cazcade.com
 */
public interface ServiceExistsFunction<T, R> {
    R apply(T t) throws Exception;

}
