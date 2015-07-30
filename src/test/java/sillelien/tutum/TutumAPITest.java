package sillelien.tutum;

import me.neilellis.dollar.api.var;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * (c) 2015 Cazcade Limited
 *
 * @author neil@cazcade.com
 */
public class TutumAPITest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testExec() throws Exception {
        TutumAPI api = new TutumAPI();
        TutumService service = api.getServiceByName("tutum-api-exec-test");
        String containerUrl = service.containers().get(0);
        TutumContainer container = api.getContainer(containerUrl);
        List<var> result = api.exec(container, "ls -la /");
        assertTrue(result.size() > 0);
        for (var s : result) {
            System.out.println(s);
        }
    }
}