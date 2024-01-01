package Udp;

import Data.Info;
import Data.Message;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UdpStart {
    private int port;
    private Info info;
    private Message msg;
    private DatagramSocket udpSocket;
    private DatagramPacket sendPacket;
    private DatagramPacket receivePacket;
    private Thread th,th2;
    private boolean Win=true;
    private int ip;
    public UdpStart(int port, Info info, Message msg, int i) {
        this.info=info;
        this.msg=msg;
        this.port=port;
        ip=i;
    }

    public void startServer() {
        th=new Thread(() -> {
            try {

                udpSocket = new DatagramSocket(port,InetAddress.getByName("127.0.0."+ip));
                udpSocket.setBroadcast(true);
                byte[] receiveData = new byte[1024];

                while (true) {
                    receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    udpSocket.receive(receivePacket);

                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    String[] temp=message.split(";");
                    if(temp[0].equals("UDP")){
                        if(temp[1].equals("GUI")){
                            msg.setService(temp[1]);
                            msg.setSubject(temp[2]);
                            msg.setMsg(receivePacket.getAddress().getHostAddress());
                            System.out.println(receivePacket.getAddress().getHostAddress());
                            info.setServerWSIp(receivePacket.getAddress().getHostAddress());
                        }
                        else {
                            if(temp[1].equals("IS_SERVER")){
                                if(info.isImServer()) {
                                    System.out.println(temp[2]);
                                    InetAddress clientAddres = InetAddress.getByName(receivePacket.getAddress().getHostAddress());
                                    int clientPort = receivePacket.getPort();
                                    String respone = "UDP;GUI;SERVER_WORK;";
                                    byte[] sendData = respone.getBytes(StandardCharsets.UTF_8);
                                    sendPacket = new DatagramPacket(sendData, sendData.length, clientAddres, clientPort);
                                    udpSocket.send(sendPacket);
                                }
                                else if(th2!=null){
                                    if(th2.isAlive())
                                        th2.stop();
                                }
                            }
                            if(temp[1].equals("TIRAN")){
                                int tempInt=Integer.parseInt(temp[2]);
                                System.out.println(info.getConnNumber()+" "+tempInt);
                                if(info.getConnNumber()<tempInt) {
                                    Win=false;
                                }
                                else if(info.getConnNumber()-1==tempInt)
                                    tiran(info.getConnNumber());
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        th.start();
    }
    public void askServer() throws IOException {
        InetAddress brdcast= InetAddress.getByName("255.255.255.255");
        int clientPort=4444;
        String newMsg="UDP;IS_SERVER;CHECK";
        byte[] sendData= newMsg.getBytes(StandardCharsets.UTF_8);
        sendPacket= new DatagramPacket(sendData,sendData.length,brdcast,clientPort);
        udpSocket.send(sendPacket);
        new Thread(()->{
            try {
                Thread.sleep(5000+(10-info.getConnNumber())*100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(info.getServerWSIp()==null) {
                msg.setSubject("SERVER_NEEDED");
                msg.setService("GUI");
            }
        }).start();

    }
    public void stopServer() {
        if(th.isAlive())
            th.interrupt();
    }

    public void tiran(int connNumber) throws IOException {
        Win=true;
        InetAddress brdcast= InetAddress.getByName("255.255.255.255");
        int clientPort=4444;
        String newMsg="UDP;TIRAN;"+connNumber;
        byte[] sendData= newMsg.getBytes(StandardCharsets.UTF_8);
        sendPacket= new DatagramPacket(sendData,sendData.length,brdcast,clientPort);
        udpSocket.send(sendPacket);
        th2=new Thread(()->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(Win==true) {
                msg.setSubject("SERVER_NEEDED");
                msg.setService("GUI");
            }
        });
        th2.start();
    }

    public void sendIP() throws IOException{
        InetAddress brdcast= InetAddress.getByName("255.255.255.255");
        int clientPort=4444;
        String respone = "UDP;GUI;SERVER_WORK;" + InetAddress.getLocalHost().getHostAddress();
        byte[] sendData = respone.getBytes(StandardCharsets.UTF_8);
        sendPacket = new DatagramPacket(sendData, sendData.length, brdcast, clientPort);
        udpSocket.send(sendPacket);
    }
}
