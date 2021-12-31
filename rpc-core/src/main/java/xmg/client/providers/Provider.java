package xmg.client.providers;


import java.net.InetSocketAddress;
import java.util.Objects;

public class Provider {
    private String name;
    private String host;
    private Integer port;
    private String description;


    public InetSocketAddress getInetAddress() {
        if (host == null || port == null) {
            return null;
        }
        try {
            return new InetSocketAddress(host, port);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Provider{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Provider)) return false;
        Provider provider = (Provider) o;
        return Objects.equals(name, provider.name) && Objects.equals(host, provider.host) && Objects.equals(port, provider.port) && Objects.equals(description, provider.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, host, port, description);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
