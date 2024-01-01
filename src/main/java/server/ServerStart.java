package server;

import Data.Info;
import Data.Message;
import org.glassfish.tyrus.server.Server;

public class ServerStart {
    public static void main(String[] args) {
        WebSocketServer wss=new WebSocketServer();
        wss.Init(8080,new Info(),new Message());
        Server server = new Server("localhost", 8080, "/",null, wss.getClass());

        try {
            server.start();
            System.out.println("Serwer WebSocket uruchomiony. Naciśnij Enter, aby zakończyć...");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }
}