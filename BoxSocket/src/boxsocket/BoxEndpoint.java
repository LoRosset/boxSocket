
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
        String message = store.get(0);
        JSONObject msg = new JSONObject(message);
        if(msg.getString("msg") == "camera"){
            try(BufferedReader br = new BufferedReader(new FileReader(FILENAME))){
                String currentLine;
                JSONObject obj = new JSONObject();
                int i = 0;
                while((currentLine = br.readLine()) != null){
                    i++;
                    obj.put("camera"+i, currentLine);
                    cameras.add(i, currentLine);
                }
                if(i!=0){
                    sendMessage(obj.toString());
                }
            }
        } else if(msg.getString("msg") == "connexion"){
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
    
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
    
    public void sendObject(Object obj) throws IOException, EncodeException {
        this.session.getBasicRemote().sendObject(obj);
    }
    
    
    
    
}
