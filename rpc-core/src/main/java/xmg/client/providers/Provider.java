package xmg.client.providers;


import java.net.InetSocketAddress;
import java.util.Objects;

public class Provider {
    private Integer id;
    private String name;
    private String host;
    private Integer port;

    private String description;
    private boolean trace;

    public Provider() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Provider provider = (Provider) o;
        return Objects.equals(name, provider.name)
                && Objects.equals(host, provider.host)
                && Objects.equals(port, provider.port)
                && Objects.equals(id, provider.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, host, port, id);
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

    public boolean isTrace() {
        return trace;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }
}
