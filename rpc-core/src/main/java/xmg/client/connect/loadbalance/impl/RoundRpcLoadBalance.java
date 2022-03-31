package xmg.client.connect.loadbalance.impl;

import org.springframework.lang.NonNull;
import xmg.client.connect.loadbalance.RpcLoadBalance;
import xmg.client.providers.Provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RoundRpcLoadBalance implements RpcLoadBalance {
    private final Map<String, AtomicInteger> counterMap;


    public RoundRpcLoadBalance() {
        counterMap = new HashMap<>( );
    }

    @Override
    public Provider choose(@NonNull String name, List<Provider> addProviders) {
        final List<Provider> providers = addProviders.stream()
                .filter(it -> name.equals(it.getName()))
                .collect(Collectors.toList());
        if (providers.isEmpty()) {
            return null;
        }
        final int count = providers.size();
        final int nextServerIndex = incrementAndGetModulo(count, name);
        return providers.get(nextServerIndex);
    }

    private int incrementAndGetModulo(int modulo, String name) {
        int current;
        int next;
        final AtomicInteger serverCyclicCounter = this.counterMap.computeIfAbsent(name, k -> new AtomicInteger(0));
        do {
            current = serverCyclicCounter.get();
            next = (current + 1) % modulo;
        } while (!serverCyclicCounter.compareAndSet(current, next));

        return next;
    }
}
