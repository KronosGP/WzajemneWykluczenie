package klient.MLP;
import java.util.Random;


public class Neuron {
    double [] wagi;
    int liczba_wejsc;

    public Neuron(){
        liczba_wejsc=0;
        wagi=null;
    }
    public Neuron(int liczba_wejsc){
        this.liczba_wejsc=liczba_wejsc;
        wagi=new double[liczba_wejsc+1];
        generuj();
    }
    private void generuj() {
        Random r=new Random();
        for(int i=0;i<=liczba_wejsc;i++) {
            //wagi[i]=(r.nextDouble()-0.5)*2.0*10;//do ogladania
            wagi[i]=(r.nextDouble()-0.5)*2.0*0.01;//do projektu
        }
    }
    public double oblicz_wyjscie(double [] wejscia){
        double fi=wagi[0];
        //double fi=0.0;
        for(int i=1;i<=liczba_wejsc;i++)
            fi+=wagi[i]*wejscia[i-1];
        double wynik=1.0/(1.0+Math.exp(-fi));// funkcja aktywacji sigma -unip
        //double wynik=(fi>0.0)?1.0:0.0;//skok jednostkowy
        //double wynik=fi; //f.a. liniowa
        return wynik;
    }


    //-----------------funkcje dodane -----------------//

    public void setWagi(double[] wagi) {
        this.wagi=wagi;
        liczba_wejsc=wagi.length;
    }

    //podanie ilości wejść (potrzebne do wylicznia błędów dla poprzedniej warstwy)
    public int getWejscia() {
        return liczba_wejsc;
    }
    //------------------------------------------------------//
    public double[] getWagi(){return wagi;}

    public void wyswietl() {
        for(int i=0;i<wagi.length;i++){
            System.out.println("        "+wagi[i]);
        }
    }
}