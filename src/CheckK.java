import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CheckK implements Runnable{
    private boolean run=true;
    private String ipK;

    public void setRun(boolean run) {
        this.run = run;
    }

    public void setIpK(String ipK) {
        this.ipK = ipK;
    }

    @Override
    public void run() {

        while(run){
            try {
                InetAddress inet=InetAddress.getByName(ipK);
                if(inet.isReachable(100));
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            } catch (UnknownHostException e) {
            } catch (IOException e) {
            }
        }
    }


}
