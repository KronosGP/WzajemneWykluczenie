package server;

import org.glassfish.tyrus.server.Server;

public class ServerStart {
    public static void main(String[] args) {
        Server server = new Server("localhost", 8080, "/MLP",null, WebSocketServer.class);

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