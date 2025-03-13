package net.jo.http;

/**
 *
 * @Title: Proxy.java
 * @Package net.jolog.bean
 * @author:JO
 * @Company:JO
 * @copyright:JO
 * @date:2019年9月18日 上午9:17:53
 * @version V2.0
 */
public class Proxy {
    private String IP;
    private int Port;
    private String un;
    private String pwd;
    private int weight;
    private boolean deaded;

    public Proxy(){}

    public Proxy(String ip,int port){
        this.IP = ip;
        this.Port = port;
    }

    public Proxy(String ip,int port,String un,String pwd){
        this.IP = ip;
        this.Port = port;
        this.un = un;
        this.pwd = pwd;
    }

    public String getUn() {
        return this.un;
    }

    public void setUn(String un) {
        this.un = un;
    }

    public String getPwd() {
        return this.pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getIP() {
        return this.IP;
    }

    public void setIP(String ip) {
        this.IP = ip;
    }

    public int getPort() {
        return this.Port;
    }

    public void setPort(int port) {
        this.Port = port;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isDeaded() {
        return deaded;
    }

    public void setDeaded(boolean deaded) {
        this.deaded = deaded;
    }

    public String toString(){
        StringBuffer p = new StringBuffer();
        p.append(IP);
        p.append(":");
        p.append(Port);
        if(un != null && !un.equals("")){
            p.append(":");
            p.append(un);
            p.append(":");
            p.append(pwd);
        }
        return p.toString();
    }
}