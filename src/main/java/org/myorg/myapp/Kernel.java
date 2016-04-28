package org.myorg.myapp;

import org.infinispan.Cache;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class Kernel {

    @Inject
    private Cache<String, String> nameCache;

    public void putIntoCache(String key, String val) {
        nameCache.put(key, val);
    }

    public String getFromCache(String key) {
        return nameCache.get(key);
    }
}
