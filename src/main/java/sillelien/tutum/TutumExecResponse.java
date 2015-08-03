package sillelien.tutum;

import com.sillelien.dollar.api.var;

import java.util.List;

import static com.sillelien.dollar.api.DollarStatic.$;

/**
 * (c) 2015 Cazcade Limited
 *
 * @author neil@cazcade.com
 */
public class TutumExecResponse {
    private List<var> response;

    public TutumExecResponse(List<var> response) {

        this.response = response;
    }

    public int size() {
        return response.size();
    }

    public var toVar() {
        return $(response);
    }

    @Override
    public String toString() {
        return toVar().toString();
    }
}
