package server;

import server.MLP.Siec;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Teach {
    private int epoka=0;
    private Siec siec;
    public Teach(Siec s) {
        this.siec=s;
    }

    public void runQueries() throws InterruptedException {
        //------------czytanie z pliku------------//
        File file;
        String path="src/main/java/server/MLP/";
        int[] wynik=new int[4];
        do {
            //Testowanie--------------------------------------------------------------------
            for (int k = 0; k < 4; k++) {//ustawienie 4 testuje z nieznanymi elementami; ustawienie 3 testuje bez nieznanych elementów
                if (k == 0)
                    file = new File(path+"ciagTestowy/ciagTestowy_V.txt");
                else if (k == 1)
                    file = new File(path+"ciagTestowy/ciagTestowy_P.txt");
                else if(k==2)
                    file = new File(path+"ciagTestowy/ciagTestowy_R.txt");
                else
                    file = new File(path+"ciagTestowy/ciagTestowy_F.txt");
                wynik[k] = 0;
                try {
                    wynik[k] = spr(new Scanner(file), k);
                } catch (FileNotFoundException ex) {
                    System.out.println("Nie ma plików");
                    //wyjscie.setText("Nie ma plików testowych");
                    break;
                }

            }
            //poprawnosc.setText("<html>Zadane pytania: "+zadaneP+"<br> Poprawne odpowiedzi: "+poprawneODP+"<br> Skutecznosc ok: "+ Math.round((double)poprawneODP/(double) zadaneP*10000)/100.0+"%<br> Epoki: "+epoka+"</html>");


            //Uczenie-----------------------------------------------------------------------
            for (int k = 0; k < 4; k++) {//ustawienie 4 uczy z nieznanymi elementami; ustawienie 3 uczy bez nieznanych element?w
                if (k == 0)
                    file = new File(path+"ciagUczacy/ciagUczacy_V.txt");
                else if (k == 1)
                    file = new File(path+"ciagUczacy/ciagUczacy_P.txt");
                else if(k==2)
                    file = new File(path+"ciagUczacy/ciagUczacy_R.txt");
                else
                    file=new File(path+"ciagUczacy/ciagUczacy_F.txt");

                try {
                    wynik[k] = spr(new Scanner(file), k);//wyniki do wykres?w
                    ucz(new Scanner(file),k);
                } catch (FileNotFoundException ex) {
                    System.out.println("Nie ma plików");
                    //wyjscie.setText("Nie ma plików uczących");
                }


            }
            siec.popraw();
            epoka++;
            //System.out.println(wynik[0]+"   "+wynik[1]+"   "+wynik[2]+"     "+wynik[3]);
            if(Thread.currentThread().isInterrupted())
                return;
            Thread.sleep(10);
        }while((wynik[0]>5 || wynik[1]>5 || wynik[2]>5 || wynik[3]>5) && epoka<10000 && !Thread.currentThread().isInterrupted());
        //JOptionPane.showMessageDialog(null,"Nauka zakończona");
        System.out.println("Nauka zakończona");
        //------------------------------------//
    }

    private int spr(Scanner in, int k) {
        double[] temp=new double[64];
        int wynik=0;
        int ile=0;
        while(in.hasNext() && !Thread.currentThread().isInterrupted()) {
            String[] zdanie = in.nextLine().split(";");
            for (int i = 0; i < 64; i++)
                temp[i] = Double.parseDouble(zdanie[i]);

            double [] oWyjscie=siec.oblicz_wyjscie(temp);
            ile++;


            //Obliczanie b??du og?lnego

            //Sqrt((b??dW0^2+b??dW1^2+b??dW2^2)/3)*100
            if(k==0) {
                wynik+=(int)((Math.sqrt(((1-oWyjscie[0])*(1-oWyjscie[0])+(0-oWyjscie[1])*(0-oWyjscie[1])+(0-oWyjscie[2])*(0-oWyjscie[2]))/3))*100);
                //if (oWyjscie[0] > 0.9 && oWyjscie[1] < 0.1 && oWyjscie[2] < 0.1)
                    //poprawneODP++;
            }

            if(k==1)
            {
                wynik+=(int)((Math.sqrt(((0-oWyjscie[0])*(0-oWyjscie[0])+(1-oWyjscie[1])*(1-oWyjscie[1])+(0-oWyjscie[2])*(0-oWyjscie[2]))/3))*100);
                //if(oWyjscie[0]<0.1 && oWyjscie[1]>0.9 && oWyjscie[2]<0.1)
                    //poprawneODP++;
            }
            if(k==2)
            {
                wynik+=(int)((Math.sqrt(((0-oWyjscie[0])*(0-oWyjscie[0])+(0-oWyjscie[1])*(0-oWyjscie[1])+(1-oWyjscie[2])*(1-oWyjscie[2]))/3))*100);
                //if(oWyjscie[0]<0.1 && oWyjscie[1]<0.1 && oWyjscie[2]>0.9)
                    //poprawneODP++;
            }
            if(k==3) {
                wynik+=(int)((Math.sqrt(((0-oWyjscie[0])*(0-oWyjscie[0])+(0-oWyjscie[1])*(0-oWyjscie[1])+(0-oWyjscie[2])*(0-oWyjscie[2]))/3))*100);
                //if(oWyjscie[0]<0.9 && oWyjscie[1]<0.9 && oWyjscie[2]<0.9)
                    //poprawneODP++;
            }
            //System.out.println(oWyjscie[0]+"    "+oWyjscie[1]+"     "+oWyjscie[2]);
            //zadaneP++;
        }

        return wynik/ile;
    }

    private void ucz(Scanner in, int k){
        double[] temp=new double[64];
        while (in.hasNext()&& !Thread.currentThread().isInterrupted()) {
            String[] zdanie = in.nextLine().split(";");
            for (int i = 0; i < 64; i++)
                temp[i] = Double.parseDouble(zdanie[i]);


            //------------Uczenie------------//

            //wyliczanie b?edu dla neuron?w wyj?ciowych

            siec.liczDelta(siec.oblicz_wyjscie(temp),k);
            siec.liczPoprawe(temp,(epoka/100)+1);

            //------------------------------------//
        }
    }
}
