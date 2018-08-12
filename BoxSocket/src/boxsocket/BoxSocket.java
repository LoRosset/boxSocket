package boxsocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.json.JSONObject;
/**
 *
 * @author Loïc
 */
public class BoxSocket {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws DeploymentException, IOException, URISyntaxException, InterruptedException {
        BoxEndpoint boxEndpoint = new BoxEndpoint();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session session = container.connectToServer(boxEndpoint, new URI("ws://www.camera-stream.tk:8001"));
        
        String macAddress = tools.GetNetworkAddress.GetAddress("mac");
        if(macAddress != null){
            System.out.println("Mac: "+ macAddress);
        }
        JSONObject obj = new JSONObject();
        obj.put("mac", macAddress);
        boxEndpoint.sendMessage("Hello from client, my mac address is : "+macAddress);
         
        Thread.sleep(2000);
    }
    
    
}
