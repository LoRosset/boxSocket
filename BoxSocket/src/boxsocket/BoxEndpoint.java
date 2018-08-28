
package boxsocket;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.websocket.EncodeException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import org.json.JSONObject;

/**
 *
 * @author Loïc
 */
public class BoxEndpoint extends Endpoint {
    
    private Session session;
    private ArrayList<String> store = new ArrayList<String>();
    private ArrayList<String> cameras = new ArrayList<String>();
    private ArrayList<Process> connexionsCam = new ArrayList<Process>();
    private final String FILENAME = "./listeCam.txt";
    private boolean managing = false;

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {

            @Override
            public void onMessage(String message) {
                store.add(message);
            }
        });
        
    }
    
    public void manageRequest() throws FileNotFoundException, IOException{
        managing = true;
        String message = store.get(0);
        JSONObject msg = new JSONObject(message);
        if(msg.getString("msg").compareTo("camera") == 0){
            System.out.println("Try to send cameras");
            try(BufferedReader br = new BufferedReader(new FileReader(FILENAME))){
                String currentLine;
                JSONObject obj = new JSONObject();
                int i = 0;
                while((currentLine = br.readLine()) != null){
                    obj.put("ip", currentLine);
                    obj.put("name", "camera"+i);
                    cameras.add(i, currentLine);
                    i++;
                }
                if(i!=0){
                    sendMessage(obj.toString());
                    store.remove(0);
                    managing = false;
                }
            }
        } else if(msg.getString("msg").compareTo("connexion") == 0){
            String camera = msg.getString("camera");
            camera.replaceAll("[^0-9]", "");
            int i = Integer.parseInt(camera);
            String ip = cameras.get(i);
            String command = "ssh -f -N -T -l loic -R8554:"+ip+":80 51.15.227.253";
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(command);
            connexionsCam.add(pr);
        } else if(msg.getString("msg")=="kill"){
            connexionsCam.get(0).destroy();
        } else {
            System.out.println(msg);
        }
        
    }
    
    public boolean hasRequest(){
        if(store.isEmpty()){
            return false;
        }
        else{
            return true;
        }
    }
    
    public boolean getManaging(){
        return this.managing;
    }
    
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
    
    public void sendObject(Object obj) throws IOException, EncodeException {
        this.session.getBasicRemote().sendObject(obj);
    }
    
    
    
    
}
