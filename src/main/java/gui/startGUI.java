package gui;

import Data.Info;
import Data.MLP.Siec;
import Data.Message;
import Udp.UdpStart;
import com.google.gson.Gson;
import klient.WebSocketClient;
import server.WebSocketServer;

import javax.swing.*;
import javax.websocket.EncodeException;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.UnknownHostException;


public class startGUI extends JFrame {
    Thread th;
    private UdpStart udp;
    private WebSocketClient WSC;
    private WebSocketServer WSS;
    private Info info;
    private Message msg;
    private Siec siec = null;
    private final JButton Learn;

    private final MyComponent komponent;
    private final JLabel wyjscie,poprawnosc,ticket,conn;
    private final JFrame frame;
    private static class MyComponent extends JPanel implements MouseListener, MouseMotionListener {
        private boolean p;
        private boolean painting;
        private int px, py;
        private boolean[][] data;
        private int[] bit8x8;
        private boolean czyRysowane=false;

        protected void paint() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            bit8x8=new int[64];
            cleanMatrix();
            p = false;
            painting = false;
            px = 0;
            py = 0;
            data = new boolean[getWidth()][getHeight()];
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        public void mousePressed(MouseEvent e) {
            p = true;
            painting = true;
        }

        public void mouseDragged(MouseEvent e) {
            int x = e.getX(), y = e.getY();
            Graphics graphics = getGraphics();
            graphics.setColor(Color.BLACK);
            czyRysowane=true;
            if (painting && p) {
                graphics.drawLine(x, y, x, y);
                p = false;
            } else if (painting) {
                graphics.drawLine(px, py, x, y);
            }
            px = x;
            py = y;
            if (painting) data[x][y] = true;
        }

        public void mouseExited(MouseEvent e) {
            painting = false;
        }

        public void mouseEntered(MouseEvent e) {
            painting = true;
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void Clean() {
            data = new boolean[getWidth()][getHeight()];
            Graphics graphics = getGraphics();
            graphics.clearRect(0, 0, getWidth(), getHeight());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            czyRysowane=false;
            cleanMatrix();
        }

        private void cleanMatrix() {
            for(int i=0;i<64;i++)
                bit8x8[i]=0;
        }

        public void minimalData() {
            if(czyRysowane) {
                boolean[][] imgData;
                int px = getWidth(), py = getHeight(), kx = 0, ky = 0;
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < data[i].length; j++) {
                        if (data[i][j]) {
                            if (i < px)
                                px = i;
                            if (j < py)
                                py = j;
                            if (j > ky)
                                ky = j;
                            if (i > kx)
                                kx = i;
                        }
                    }
                }
                imgData = new boolean[kx - px+8][ky - py+8];
                for (int i = 0; i < imgData.length; i++) {
                    for (int j = 0; j < imgData[i].length; j++) {
                        if(data[i].length<j+py+4 || data.length<i+px+4 ||j+py-4<0 ||i+px-4<0 )
                            imgData[i][j]=false;

                        else
                            imgData[i][j] = data[i + px-4][j + py-4];
                    }
                }
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        bit8x8[i + j * 8] = getStatus(imgData.length * i / 8, imgData[0].length * j / 8, imgData.length * (i + 1) / 8, imgData[0].length * (j + 1) / 8, imgData);
                    }
                }
            }
        }

        private int getStatus(int x1, int y1, int x2, int y2, boolean[][] imgData) {
            for(int i=y1;i<y2;i++)
                for(int j=x1;j<x2;j++)
                {
                    if(imgData[j][i])
                        return 1;
                }
            return 0;
        }
        public int[] getBit8x8()
        {
            minimalData();
            return bit8x8;
        }
    }

    public startGUI(String string,int i) throws IOException {
        super(string);
        frame=new JFrame();
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension d = kit.getScreenSize();
        setLayout(null);
        setLocationRelativeTo(null);
        setResizable(true);
        this.setBounds(d.getSize().width/3, d.getSize().height/3, 512, 512);
        this.setSize(512, 512);
        komponent=new MyComponent();
        komponent.setBounds(10,10,getWidth()-30,getHeight()*2/3);
        komponent.paint();
        add(komponent);

        Learn =new JButton("Bilet");
        Learn.addActionListener(new learn());
        Learn.setBounds(10,getHeight()*2/3+15,getWidth()/3-20,25);
        add(Learn);

        JButton Test=new JButton("Sprawdź");
        Test.addActionListener(new test());
        Test.setBounds(5+getWidth()/3,getHeight()*2/3+15,getWidth()/3-20,25);
        add(Test);

        JButton Clean=new JButton("Wyczyść");
        Clean.addActionListener(new clean());
        Clean.setBounds(getWidth()*2/3,getHeight()*2/3+15,getWidth()/3-20,25);
        add(Clean);

        wyjscie=new JLabel("Jaka Litera?");
        poprawnosc=new JLabel("Poprawność");
        ticket=new JLabel("Numer biletu: ");
        conn=new JLabel("Numer połączenia: ");
        wyjscie.setBounds(10,getHeight()*2/3+50,getWidth()/2-20,25);
        conn.setBounds(getWidth()/2+10,getHeight()*2/3+50,getWidth()/2-20,25);
        poprawnosc.setBounds(10,getHeight()*2/3+80,getWidth()/2-20,25);
        ticket.setBounds(getWidth()/2+10,getHeight()*2/3+80,getWidth()/2-20,25);
        add(wyjscie);
        add(poprawnosc);
        add(conn);
        add(ticket);


        WindowListener exitListener = new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                udp.stopServer();
                if(WSS!=null)
                    WSS.closeServer();
                if(WSC!=null) {
                    try {
                        WSC.closeClient();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                if(th.isAlive())
                    th.stop();
                System.exit(0);
            }
        };
        frame.addWindowListener(exitListener);
        setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        setVisible(true);
        Init(i);
    }

    private void Init(int i) {
        info=new Info();
        msg=new Message();

        udp=new UdpStart(4444, info,msg,i);
        udp.startServer();
        th =new Thread(()->{
            try {
                Thread.sleep(2000);
                udp.askServer();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while(true){
               if(msg.getService().equals("GUI")){
                   if(msg.getSubject()!=null) {
                       System.out.println(msg.getSubject());
                   }
                   if(msg.getSubject().equals("SERVER_WORK")){
                       if(WSC!=null)
                           WSC.stop();
                       WSC=new WebSocketClient();
                       try {
                           WSC.start(8080, (String) msg.getMsg(),info,msg);
                       } catch (IOException e) {
                           throw new RuntimeException(e);
                       }
                       msg.ClearValue();
                       new Thread(()->JOptionPane.showMessageDialog(this,"Podłączyłeś się do servera")).start();
                   } else if (msg.getSubject().equals("DISABLE")) {
                       new Thread(()->JOptionPane.showMessageDialog(this,"Nauka rozpoczeta")).start();
                       Learn.setEnabled(false);
                       Learn.setText("Bilet");
                       msg.ClearValue();
                   } else if(msg.getSubject().equals("NETWORK")){
                       Gson gsn = new Gson();
                       siec = gsn.fromJson((String) msg.getMsg(), Siec.class);
                       msg.ClearValue();
                       Learn.setEnabled(true);
                       new Thread(()->JOptionPane.showMessageDialog(this,"Sieć nauczona")).start();
                   }
                   else if(msg.getSubject().equals("SERVER_NEEDED")){
                       WSS=new WebSocketServer();
                       WSS.Init(8080,info,msg);
                       WSS.start();
                       info.setImServer(true);
                       msg.ClearValue();
                       try {
                           Thread.sleep(100);
                           udp.sendIP();
                       } catch (IOException | InterruptedException e) {
                           throw new RuntimeException(e);
                       }
                       new Thread(()->JOptionPane.showMessageDialog(this,"Ty jesteś serwerem")).start();
                   }
                   else if(msg.getSubject().equals("SERVER_STOP")){
                       try {
                           info.setServerWSIp(null);
                           WSC.stop();
                           WSC=null;
                           udp.tiran(info.getConnNumber());
                           msg.ClearValue();
                       } catch (UnknownHostException e) {
                           throw new RuntimeException(e);
                       } catch (IOException e) {
                           throw new RuntimeException(e);
                       }
                   }
                   else if(msg.getSubject().equals("YOUR_TOUR")){
                       new Thread(()->JOptionPane.showMessageDialog(null,"Twoje miejsce w kolejce: "+info.getTicketNumber())).start();
                       msg.ClearValue();
                   }
               }
               ticket.setText("Numer biletu: " + info.getTicketNumber());
               conn.setText("Numer połączenia: " + info.getConnNumber());
           }
        });
        th.start();
    }

    private class clean implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            komponent.Clean();
        }
    }

    private class test implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int[] temp = komponent.getBit8x8();
                double[] tempD=new double[64];
                for (int i=0;i<64;i++)
                    tempD[i]=temp[i];
                double[] wynik = siec.oblicz_wyjscie(tempD);
                wyjscie.setText("Jest to litera " + ((wynik[0] > 0.65) ? "V" : ((wynik[1] > 0.65) ? "P" : ((wynik[2] > 0.65) ? "R" : "nie wiem jaka"))));
            }
            catch (Exception ex)
            {}

        }
    }
    private class learn implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
                if(info.getTicketNumber()<0)
                    try {
                        WSC.getTicket();
                        Learn.setText("Ucz");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                else {
                    try {
                        WSC.learn();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } catch (EncodeException ex) {
                        throw new RuntimeException(ex);
                    }
                }
        }
    }
}
