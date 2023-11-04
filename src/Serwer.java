
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serwer {
    
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8080);
        Runnable
        try{
            System.out.println("Server hes started");
            while(true) {
                Socket client = server.accept();

                if()
            }
        }
        catch (Exception ex) {
            System.out.println("Error: "+ex);
        }


    }

}
