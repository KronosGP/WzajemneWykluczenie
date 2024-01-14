package server;

import Data.Info;
import Data.Message;
import org.glassfish.tyrus.server.Server;
import server.MLP.Siec;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/Connect")
public class WebSocketServer {
    private boolean isWorking=true;
    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    //private static List<HashMap<Integer,Session>> IdSession=new ArrayList<>();
    private static Set<Session> sessionsR = Collections.synchronizedSet(new HashSet<>());
    private static Thread th;
    private static Teach t;
    private static Lock lamportLock = new ReentrantLock();
    private static int lamportClock = 0;

    private int port;
    private Info info;
    private Message msg;

    public void Init(int port, Info info, Message msg) {
        this.info=info;
        this.msg=msg;
        this.port=port;
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // Dodaj nową sesję do zbioru sesji
        int connCount=sessionsR.size();
        sessionsR.add(session);
        session.getBasicRemote().sendText("NEW_CONN "+connCount);
        System.out.println("Nowy klient połączony. ID sesji: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException{
        System.out.println("Otrzymano wiadomość od klienta " + session.getId() + ": " + message);
        if(message.equals("MY_TURN")) {
            int idTicket=checkNumber(session,true);
            if (idTicket == 0) {
                session.getBasicRemote().sendText("ACCEPTED");
                critic(session);

            }
            else{
                if(sessions.contains(session))
                    session.getBasicRemote().sendText("SET_TICKET "+idTicket);
                else{
                    session.getBasicRemote().sendText("SET_TICKET "+sessions.size());
                    sessions.add(session);
                }
            }
        }
        else if(message.equals("RECIVED_SIEC"))
        {
            broadcast("YOUR_TICKET",session,true);
            sessions.remove(session);
        }
        else if(message.equals("GET_TICKET")){
            int ticket=sessions.size();
            session.getBasicRemote().sendText("SET_TICKET "+ticket);
            sessions.add(session);
        }
    }

    @OnClose
    public void onClose(Session session) {
        // Usuń zamkniętą sesję z zestawu sesji
        if(checkNumber(session,true)==0 && th!=null)
            if(th.isAlive())
                th.stop();
        broadcast("YOUR_TICKET",session,true);
        broadcast("NEW_NUMBER",session,false);
        sessionsR.remove(session);
        sessions.remove(session);

        System.out.println("Klient odłączony. ID sesji: " + session.getId());
    }

    @OnError
    public void onError(Session s,Throwable t) {
        System.out.println(s);
        t.printStackTrace();
    }

    private void broadcast(String message, Session s,Boolean isTicket) {
        // Prześlij wiadomość do wszystkich podłączonych klientów
        int idTicket=checkNumber(s,isTicket);
        for (Session session : sessionsR) {
            try {
                if(session!=s)
                    session.getBasicRemote().sendText(message + " " + idTicket);
            } catch (IOException e) {
                System.err.println("Błąd podczas wysyłania wiadomości do klienta.");
                e.printStackTrace();
            }
        }
    }
    private int checkNumber(Session session,Boolean isTicket) {
        int i=0;
        for(Session s:(isTicket)?sessions:sessionsR){
            if(s.getId()==session.getId())
                return i;
            i++;
        }
        return -1;
    }

    public void start() {
        new Thread(()-> {
            Server server = null;
            try {
                server = new Server(InetAddress.getLocalHost().getHostAddress(), port, "/", null, this.getClass());
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

            try {
                server.start();
                //System.out.println("Serwer WebSocket uruchomiony. Naciśnij Enter, aby zakończyć...");
                while (isWorking){
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                server.stop();
            }
        }).start();
    }
    private void critic(Session session) {
        //sekcja krytyczna
        lamportLock.lock();
        try {
            lamportClock++;
            th = new Thread(() -> {
                try {
                    Session save= session;

                    Gson gsn = new Gson();

                    int[] tab = new int[3];
                    tab[0] = 20;
                    tab[1] = 15;
                    tab[2] = 3;
                    Siec s = new Siec(64, 3, tab);
                    t = new Teach(s);

                    System.out.println("Nauka rozpoczęta");
                    t.runQueries();
                    String respone = gsn.toJson(s);

                    System.out.println(save);
                    save.getBasicRemote().sendText("SIEC_SET " + respone);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            th.start();
        }
        finally {
            lamportLock.unlock();
        }
    }

    public void closeServer(){
        broadcast("SERVER_DISCONECT",null,false);
        isWorking=false;
    }
}
