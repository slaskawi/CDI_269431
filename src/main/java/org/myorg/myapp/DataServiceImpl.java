package org.myorg.myapp;

import org.infinispan.cdi.ConfigureCache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class DataServiceImpl {

    @ConfigureCache("name-cache")
    @NameCache
    @Produces
    public Configuration nameCacheConfig() {
        return new ConfigurationBuilder()
                .eviction().size(10)
                .build();
    }

}
