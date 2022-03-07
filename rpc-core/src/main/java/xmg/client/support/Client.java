package xmg.client.support;


import xmg.client.providers.Provider;

/**
 * @author makui
 * @created on  2022/3/5
 **/
public class Client {

    private Object proxy;

    private Provider provider;


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
