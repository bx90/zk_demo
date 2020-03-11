package org.bx.io;

import com.sun.org.glassfish.gmbal.ParameterNames;
import org.apache.zookeeper.KeeperException;
import org.bx.http.UrlConnection;
import org.bx.zookeeper.cluster.ZookeeperManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * @author bx
 */
@Path("machine")
public class rest {
//    private static String[] instance = {"1", "8081"};
//    private static String[] instance = {"2", "8082"};
    private static String[] instance = {"3", "8083"};

    private static ZookeeperManager zookeeperManager;

    static {
        try {
            zookeeperManager = new ZookeeperManager();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/Id/{id}")
    public String getId(@PathParam("id") int id) {

        try {
            String url = zookeeperManager.getUrl( id );
            System.out.println( url );
            return url;
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return instance[0];
    }

    @GET
    @Path("/Register")
    public String register() throws InterruptedException, IOException, KeeperException {

        String port = instance[1];

        String[] input = { port };
        zookeeperManager.register(input);

        return "ok";
    }

}
