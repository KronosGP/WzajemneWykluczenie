package Data;

public class Message {
    private String Service="";
    private String subject="";
    private Object msg;

    public synchronized String getService() {
        return Service;
    }

    public synchronized void setService(String service) {
        Service = service;
    }

    public synchronized Object getMsg() {
        return msg;
    }

    public synchronized void setMsg(Object text) {
        this.msg=text;
    }
    public synchronized String getSubject() {
        return subject;
    }

    public synchronized void setSubject(String subject) {
        this.subject = subject;
    }

    public void ClearValue() {
        Service="";
        msg=null;
        subject="";

    }
}
