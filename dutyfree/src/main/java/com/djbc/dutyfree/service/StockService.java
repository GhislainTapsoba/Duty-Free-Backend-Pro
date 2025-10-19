package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.entity.Product;
import com.djbc.dutyfree.domain.entity.Sommier;
import com.djbc.dutyfree.domain.entity.Stock;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.ProductRepository;
import com.djbc.dutyfree.repository.SommierRepository;
import com.djbc.dutyfree.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final SommierRepository sommierRepository;

    @Transactional
    public Stock addStock(Long productId, Long sommierId, Integer quantity,
                          String location, String lotNumber, LocalDate expiryDate) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Sommier sommier = null;
        if (sommierId != null) {
            sommier = sommierRepository.findById(sommierId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sommier", "id", sommierId));
        }

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        Stock stock = Stock.builder()
                .product(product)
                .sommier(sommier)
                .quantity(quantity)
                .reservedQuantity(0)
                .availableQuantity(quantity)
                .location(location)
                .lotNumber(lotNumber)
                .expiryDate(expiryDate)
                .receivedDate(LocalDate.now())
                .build();

        stock = stockRepository.save(stock);
        log.info("Stock added for product {}: {} units", product.getSku(), quantity);

        return stock;
    }

    @Transactional
    public void adjustStock(Long stockId, Integer newQuantity) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "id", stockId));

        if (newQuantity < 0) {
            throw new BadRequestException("Quantity cannot be negative");
        }

        if (newQuantity < stock.getReservedQuantity()) {
            throw new BadRequestException("Quantity cannot be less than reserved quantity");
        }

        stock.setQuantity(newQuantity);
        stock.setAvailableQuantity(newQuantity - stock.getReservedQuantity());
        stockRepository.save(stock);

        log.info("Stock adjusted for product {}: {} units", stock.getProduct().getSku(), newQuantity);
    }

    @Transactional
    public void reserveStock(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        List<Stock> stocks = stockRepository.findActiveStocksByProductId(productId);

        Integer totalAvailable = stocks.stream()
                .mapToInt(Stock::getAvailableQuantity)
                .sum();

        if (totalAvailable < quantity) {
            throw new BadRequestException("Insufficient stock available");
        }

        Integer remaining = quantity;
        for (Stock stock : stocks) {
            if (remaining <= 0) break;

            Integer toReserve = Math.min(remaining, stock.getAvailableQuantity());
            stock.setReservedQuantity(stock.getReservedQuantity() + toReserve);
            stock.setAvailableQuantity(stock.getAvailableQuantity() - toReserve);
            stockRepository.save(stock);

            remaining -= toReserve;
        }

        log.info("Reserved {} units of product {}", quantity, productId);
    }

    @Transactional
    public void releaseReservedStock(Long productId, Integer quantity) {
        List<Stock> stocks = stockRepository.findActiveStocksByProductId(productId);

        Integer remaining = quantity;
        for (Stock stock : stocks) {
            if (remaining <= 0) break;

            Integer toRelease = Math.min(remaining, stock.getReservedQuantity());
            stock.setReservedQuantity(stock.getReservedQuantity() - toRelease);
            stock.setAvailableQuantity(stock.getAvailableQuantity() + toRelease);
            stockRepository.save(stock);

            remaining -= toRelease;
        }

        log.info("Released {} units of product {}", quantity, productId);
    }

    @Transactional
    public void reduceStock(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        List<Stock> stocks = stockRepository.findActiveStocksByProductId(productId);

        Integer totalReserved = stocks.stream()
                .mapToInt(Stock::getReservedQuantity)
                .sum();

        if (totalReserved < quantity) {
            throw new BadRequestException("Insufficient reserved stock");
        }

        Integer remaining = quantity;
        for (Stock stock : stocks) {
            if (remaining <= 0) break;

            Integer toReduce = Math.min(remaining, stock.getReservedQuantity());
            stock.setReservedQuantity(stock.getReservedQuantity() - toReduce);
            stock.setQuantity(stock.getQuantity() - toReduce);
            stockRepository.save(stock);

            remaining -= toReduce;
        }

        log.info("Reduced {} units of product {}", quantity, productId);
    }

    @Transactional(readOnly = true)
    public List<Stock> getStocksByProduct(Long productId) {
        return stockRepository.findActiveStocksByProductId(productId);
    }

    @Transactional(readOnly = true)
    public Integer getTotalStock(Long productId) {
        Integer total = stockRepository.getTotalQuantity(productId);
        return total != null ? total : 0;
    }

    @Transactional(readOnly = true)
    public Integer getAvailableStock(Long productId) {
        Integer available = stockRepository.getTotalAvailableQuantity(productId);
        return available != null ? available : 0;
    }

    @Transactional(readOnly = true)
    public List<Stock> getExpiringStock(int daysAhead) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAhead);
        return stockRepository.findExpiringStock(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Stock> getExpiredStock() {
        return stockRepository.findExpiredStock(LocalDate.now());
    }
}