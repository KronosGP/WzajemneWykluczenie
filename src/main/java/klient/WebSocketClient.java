package klient;

import com.google.gson.Gson;

import klient.MLP.Siec;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocketClient {
    static Session s;
    static Siec siec;
    static int ticket;
    static boolean accepted=false;
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Nawiązano połączenie z serwerem. ID sesji: " + session.getId());
        this.s=session;
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("Otrzymano wiadomość od serwera: " + message);
        String[] temp=message.split(" ");
        if(temp[0].equals("SIEC_SET")) {
            Gson gsn = new Gson();
            siec = gsn.fromJson(temp[1], Siec.class);
            siec.wyswietl();
            if(siec!=null){
                session.getBasicRemote().sendText("RECIVED_SIEC");
            }
        }
        else if(temp[0].equals("SET_TICKET")) {
            ticket=InteagerParse(temp[1]);
        }
        else if(temp[0].equals("YOUR_TICKET")) {
            int tempInt=InteagerParse(temp[1]);
            if(tempInt>=0 &&ticket>tempInt)
                ticket--;
        }
        else if(temp[0].equals("ACCEPTED")){
            accepted=true;
        }
    }

    private int InteagerParse(String s) {
        int temp;
        try{
            temp=Integer.parseInt(s);
            return temp;
        }
        catch (NumberFormatException nfe){
            System.out.println(nfe);
            return -1;
        }
    }

    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    public static void main(String[] args) {
        try {
            // Tworzenie klienta WebSocket
            WebSocketClient client = new WebSocketClient();

            // Adres URL serwera WebSocket
            URI serverUri = new URI("ws://localhost:8080/MLP/Connect");

            // Nawiązywanie połączenia
            ContainerProvider.getWebSocketContainer().connectToServer(client, serverUri);
            s.getBasicRemote().sendText("CONNECT");

            while (true) {
                if(ticket==0 && !accepted) {
                    Thread.sleep(1000);
                    s.getBasicRemote().sendText("MY_TURN");
                }
                // Wysyłanie wiadomości do serwera
                // Tutaj możesz dostosować wiadomość do swoich potrzeb
                //client.sendMessage("Hello, org.server!");

                // Oczekiwanie na zakończenie działania programu
                // W rzeczywistych aplikacjach możesz użyć jakiegoś mechanizmu do utrzymania działania klienta (np. wątku)
                Thread.sleep(5000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
