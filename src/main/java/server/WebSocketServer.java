package server;

import server.MLP.Siec;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/Connect")
public class WebSocketServer {

    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static Set<Session> sessionsR = Collections.synchronizedSet(new HashSet<>());
    private static Thread th;
    private static Teach t;
    private static Lock lamportLock = new ReentrantLock();
    private static int lamportClock = 0;
    @OnOpen
    public void onOpen(Session session) throws IOException {
        // Dodaj nową sesję do zbioru sesji
        int ticket= sessions.size();
        sessions.add(session);
        session.getBasicRemote().sendText("YOUR_TICKET "+ticket);
        System.out.println("Nowy org.klient połączony. ID sesji: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {
        System.out.println("Otrzymano wiadomość od klienta " + session.getId() + ": " + message);
        if(message.equals("MY_TURN")) {
            int idTicket=checkTicket(session);
            if (idTicket == 0) {
                session.getBasicRemote().sendText("ACCEPTED");
                //sekcja krytyczna
                lamportLock.lock();
                try {
                    lamportClock++;
                    th = new Thread(() -> {
                        try {

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

                            if (checkTicket(session) == 0)
                                session.getBasicRemote().sendText("SIEC_SET " + respone);
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
            else{
                session.getBasicRemote().sendText("SET_TICKET "+idTicket);
            }
        }
        else if(message.equals("RECIVED_SIEC"))
        {
            sessionsR.add(session);
            sessions.remove(session);
        }
        else if(message.equals("NEW_SIEC")){
            int ticket= sessions.size();
            sessions.add(session);
            sessionsR.remove(session);
            session.getBasicRemote().sendText("YOUR_TICKET "+ticket);
        }


    }

    @OnClose
    public void onClose(Session session) {
        // Usuń zamkniętą sesję z zestawu sesji
        if(checkTicket(session)==0 && th!=null)
            if(th.isAlive())
                th.interrupt();
        broadcast("SET_TICKET",session);
        sessions.remove(session);

        System.out.println("Klient odłączony. ID sesji: " + session.getId());
    }

    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    private void broadcast(String message, Session s) {
        // Prześlij wiadomość do wszystkich podłączonych klientów
        int id=checkTicket(s);
        for (Session session : sessions) {
            try {
                if(checkTicket(session)!=id)
                    session.getBasicRemote().sendText(message+" "+id);
            } catch (IOException e) {
                System.err.println("Błąd podczas wysyłania wiadomości do klienta.");
                e.printStackTrace();
            }
        }
    }
    private int checkTicket(Session session) {
        int i=0;
        for(Session s:sessions){
            if(s.getId()==session.getId())
                return i;
            i++;
        }
        return -1;
    }
}
