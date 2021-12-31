package xmg.client.connect.loadbalance;

import org.springframework.lang.NonNull;
import xmg.client.providers.Provider;

import java.util.List;

public interface RpcLoadBalance {
    Provider choose(@NonNull String name, List<Provider> providers);
}
