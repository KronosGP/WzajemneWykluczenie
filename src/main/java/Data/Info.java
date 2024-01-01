package Data;

public class Info {
    private static String ServerWSIp;
    private int WSport;

    private static String ServerUDPIp;
    private int UDPport;

    private int ticketNumber=-1;
    private int connNumber=-1;

    private boolean ImServer=false;
    public boolean accept=false;

    public static String getServerWSIp() {
        return ServerWSIp;
    }

    public static void setServerWSIp(String serverWSIp) {
        ServerWSIp = serverWSIp;
    }

    public int getWSport() {
        return WSport;
    }

    public void setWSport(int WSport) {
        this.WSport = WSport;
    }

    public static String getServerUDPIp() {
        return ServerUDPIp;
    }

    public static void setServerUDPIp(String serverUDPIp) {
        ServerUDPIp = serverUDPIp;
    }

    public int getUDPport() {
        return UDPport;
    }

    public void setUDPport(int UDPport) {
        this.UDPport = UDPport;
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(int ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public int getConnNumber() {
        return connNumber;
    }

    public void setConnNumber(int connNumber) {
        this.connNumber = connNumber;
    }

    public boolean isImServer() {
        return ImServer;
    }

    public void setImServer(boolean imServer) {
        ImServer = imServer;
    }
}
