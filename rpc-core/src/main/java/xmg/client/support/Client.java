package xmg.client.support;


import xmg.client.providers.Provider;

import java.util.Objects;

/**
 * @author makui
 * @created on  2022/3/5
 **/
public class Client {

    private Object proxy;

    private Provider provider;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Client client = (Client) o;
        return Objects.equals(provider, client.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider);
    }

    public Object getProxy() {
        return proxy;
    }

    public void setProxy(final Object proxy) {
        this.proxy = proxy;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(final Provider provider) {
        this.provider = provider;
    }
}
