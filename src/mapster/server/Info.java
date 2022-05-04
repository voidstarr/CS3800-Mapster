package mapster.server;

public class Info {
    private String key;
    private String name;
    private String ip;
    private int port;

    public Info(String key,String name, String ip, int port) {
        this.key = key;
        this.name = name;
        this.ip = ip;
        this.port = port;
    }
    public String getKey() {
        return key;
    }
    public String getName() {
        return name;
    }
    public String getIp() {
        return ip;
    }
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Info{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
    
}
