
package boxsocket;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.websocket.EncodeException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Loïc
 */
public class BoxEndpoint extends Endpoint {
    
    private Session session;
    private ArrayList<String> store = new ArrayList<String>();
    private ArrayList<String> cameras = new ArrayList<String>();
    private HashMap<String, Process> connexionsCam = new HashMap<String, Process>();
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
        //Get cameras
        if(msg.getString("msg").compareTo("camera") == 0){
            System.out.println("Try to send cameras");
            try(BufferedReader br = new BufferedReader(new FileReader(FILENAME))){
                JSONArray arr = new JSONArray();
                JSONObject camerasJSON = new JSONObject();
                int i = 0;
                String currentLine;
                while((currentLine = br.readLine()) != null){
                    System.out.println(currentLine);
                    JSONObject camera = new JSONObject();
                    camera.put("ip", currentLine);
                    camera.put("name", "camera"+i);
                    arr.put(camera);
                    cameras.add(i, currentLine);
                    i++;
                }
                camerasJSON.put("cameras", arr);
                if(i!=0){
                    sendMessage(camerasJSON.toString());
                    store.remove(0);
                    managing = false;
                }
            }
        //Create connexion to camera
        } else if(msg.getString("msg").compareTo("connexion") == 0){
            String port = msg.getString("port");
            String camera = msg.getString("camera");
            camera = camera.replaceAll("[^0-9]", "");
            int cameraId = Integer.parseInt(camera);
            String ip = cameras.get(cameraId);
            String connexionID = "conCam"+camera;
            String command = "ssh -M -S "+connexionID+" -f -N -T -l loic -R"+port+":"+ip+":80 camera-stream.tk";
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(command);
            System.out.println("Connexion with camera is opened !");
            connexionsCam.put(camera, pr);
            store.remove(0);
            managing = false;
        //Kill the connexion to a specific camera
        } else if(msg.getString("msg").compareTo("kill") == 0){
            System.out.println("Kill the connexion");
            String camera = msg.getString("camera");
            camera = camera.replaceAll("[^0-9]", "");
            String connexionID = "conCam"+camera;
            String command = "ssh -S "+connexionID+" -O exit loic@camera-stream.tk";
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(command);
            connexionsCam.remove(camera);
            store.remove(0);
            managing = false;
        //Everything else
        } else {
            System.out.println(msg);
            store.remove(0);
            managing = false;
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
