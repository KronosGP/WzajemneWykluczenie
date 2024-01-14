package klient;

import Data.Info;
import Data.Message;

import Data.MLP.Siec;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocketClient {
    static Session s;
    private boolean isWorking=true;

    private Info info;
    private Message msg;


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

            if(temp[1]!=null){
                session.getBasicRemote().sendText("RECIVED_SIEC");
                msg.setService("GUI");
                msg.setSubject("NETWORK");
                msg.setMsg(temp[1]);
                info.setTicketNumber(-1);
            }
        }
        else if(temp[0].equals("SET_TICKET")) {
            info.setTicketNumber(InteagerParse(temp[1]));
            msg.setService("GUI");
            msg.setSubject("YOUR_TOUR");
            if(info.getTicketNumber()==0)
                s.getBasicRemote().sendText("MY_TURN");
        }
        else if(temp[0].equals("YOUR_TICKET")) {
            int tempInt=InteagerParse(temp[1]);
            if(tempInt>=0 && info.getTicketNumber()>=tempInt)
                info.setTicketNumber(info.getTicketNumber()-1);
            if(info.getTicketNumber()==0)
                s.getBasicRemote().sendText("MY_TURN");
        }
        else if(temp[0].equals("ACCEPTED")){
            msg.setService("GUI");
            msg.setSubject("DISABLE");
            info.accept=true;
        }
        else if(temp[0].equals("NEW_CONN")){
            info.setConnNumber(InteagerParse(temp[1]));
        }
        else if(temp[0].equals("NEW_NUMBER")){
            int tempInt=InteagerParse(temp[1]);
            if(tempInt>=0 &&info.getConnNumber()>=tempInt)
                info.setConnNumber(info.getConnNumber()-1);
        }
    }
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println(session);
        System.out.println("Sesja WebSocket została zamknięta. Powód: " + reason.toString());
        msg.setService("GUI");
        msg.setSubject("SERVER_STOP");
    }

    @OnError
    public void onError(Session session, Throwable t) {
        System.err.println("Błąd w sesji WebSocket. ID sesji: " + session.getId());
        t.printStackTrace();
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

    public void start(int port, String ip,Info info, Message msg) throws IOException {
        this.info=info;
        this.msg=msg;
        new Thread(()-> {
            try {
                // Tworzenie klienta WebSocket
                WebSocketClient client = new WebSocketClient();

                // Adres URL serwera WebSocket
                URI serverUri = new URI("ws://" + ip + ":" + port + "/Connect");

                // Nawiązywanie połączenia
                Session se=ContainerProvider.getWebSocketContainer().connectToServer(this, serverUri);

                s=se;
                while (isWorking) ;
                se.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void learn() throws IOException, EncodeException {
        if(s.isOpen())
            s.getBasicRemote().sendText("MY_TURN");
        else
            System.out.println("Sesja zamknięta");
    }

    public void getTicket() throws IOException {
        if(s.isOpen())
            s.getBasicRemote().sendText("GET_TICKET");
        else
            System.out.println("Sesja zamknięta");

    }

    public void closeClient() throws IOException {
        s.getBasicRemote().sendText("CLIENT_DISCONECTED");
        isWorking=false;
    }

    public void stop() {
        isWorking=false;
    }
    public static void main(String args[]) throws IOException {
        WebSocketClient ws=new WebSocketClient();
        ws.start(8080,"localhost",null,null);
    }


}
