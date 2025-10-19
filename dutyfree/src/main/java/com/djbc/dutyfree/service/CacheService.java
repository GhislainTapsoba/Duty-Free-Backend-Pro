package com.djbc.dutyfree.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final CacheManager cacheManager;

    public void evictAllCaches() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> {
                    Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
                    log.info("Cache cleared: {}", cacheName);
                });
    }

    public void evictCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cache cleared: {}", cacheName);
        } else {
            log.warn("Cache not found: {}", cacheName);
        }
    }

    public void evictCacheKey(String cacheName, Object key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.info("Cache key evicted: {} from {}", key, cacheName);
        } else {
            log.warn("Cache not found: {}", cacheName);
        }
    }
}