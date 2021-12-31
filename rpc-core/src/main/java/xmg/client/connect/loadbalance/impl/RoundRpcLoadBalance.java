package xmg.client.connect.loadbalance.impl;

import org.springframework.lang.NonNull;
import xmg.client.connect.loadbalance.RpcLoadBalance;
import xmg.client.providers.Provider;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RoundRpcLoadBalance implements RpcLoadBalance {
    private final AtomicInteger nextServerCyclicCounter;

    public RoundRpcLoadBalance() {
        this.nextServerCyclicCounter = new AtomicInteger(0);
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
        final int nextServerIndex = incrementAndGetModulo(count);
        return providers.get(nextServerIndex);
    }

    private int incrementAndGetModulo(int modulo) {
        int current;
        int next;
        do {
            current = this.nextServerCyclicCounter.get();
            next = (current + 1) % modulo;
        } while (!this.nextServerCyclicCounter.compareAndSet(current, next));

        return next;
    }
}
