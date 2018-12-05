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
        Session session = container.connectToServer(boxEndpoint, new URI("wss://camera-stream.tk:8001"));
        
        String macAddress = tools.GetNetworkAddress.GetAddress("mac");
        //String macAddress = "14-DA-E9-F4-6F-D6";
        if(macAddress != null){
            System.out.println("Mac: "+ macAddress);
        }
        JSONObject obj = new JSONObject();
        obj.put("msg", "verify");
        obj.put("box", macAddress);
        boxEndpoint.sendMessage(obj.toString());
        
        while(session.isOpen()){
            if(boxEndpoint.hasRequest() && boxEndpoint.getManaging()==false){
                boxEndpoint.manageRequest();
            }
            Thread.sleep(1000);
        }
    }
    
    
}
