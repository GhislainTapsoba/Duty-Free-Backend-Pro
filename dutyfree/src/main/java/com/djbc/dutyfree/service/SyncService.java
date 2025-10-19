package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.entity.Sale;
import com.djbc.dutyfree.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SaleRepository saleRepository;

    private static final String OFFLINE_SALE_PREFIX = "offline:sale:";
    private static final String SYNC_STATUS_KEY = "sync:status";

    /**
     * Store sale data when offline
     */
    public void storeOfflineSale(String saleId, Sale sale) {
        String key = OFFLINE_SALE_PREFIX + saleId;
        redisTemplate.opsForValue().set(key, sale, Duration.ofDays(7));
        log.info("Stored offline sale: {}", saleId);
    }

    /**
     * Synchronize offline sales when connection is restored
     */
    @Transactional
    public void synchronizeOfflineSales() {
        Set<String> keys = redisTemplate.keys(OFFLINE_SALE_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            log.info("No offline sales to synchronize");
            return;
        }

        int successCount = 0;
        int failureCount = 0;

        for (String key : keys) {
            try {
                Sale sale = (Sale) redisTemplate.opsForValue().get(key);

                if (sale != null) {
                    // Save to database
                    saleRepository.save(sale);

                    // Remove from Redis
                    redisTemplate.delete(key);

                    successCount++;
                    log.info("Synchronized offline sale: {}", sale.getSaleNumber());
                }
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to synchronize sale from key: {}", key, e);
            }
        }

        log.info("Synchronization completed. Success: {}, Failures: {}", successCount, failureCount);

        // Update sync status
        updateSyncStatus(successCount, failureCount);
    }

    /**
     * Get count of pending offline sales
     */
    public long getPendingSalesCount() {
        Set<String> keys = redisTemplate.keys(OFFLINE_SALE_PREFIX + "*");
        return keys != null ? keys.size() : 0;
    }

    /**
     * Check if there are pending sales to sync
     */
    public boolean hasPendingSales() {
        return getPendingSalesCount() > 0;
    }

    /**
     * Get last sync status
     */
    public Object getLastSyncStatus() {
        return redisTemplate.opsForValue().get(SYNC_STATUS_KEY);
    }

    private void updateSyncStatus(int successCount, int failureCount) {
        var status = new SyncStatus(
                LocalDateTime.now(),
                successCount,
                failureCount,
                getPendingSalesCount()
        );

        redisTemplate.opsForValue().set(SYNC_STATUS_KEY, status, Duration.ofDays(30));
    }

    // Inner class for sync status
    public record SyncStatus(
            LocalDateTime timestamp,
            int successCount,
            int failureCount,
            long pendingCount
    ) {}
}