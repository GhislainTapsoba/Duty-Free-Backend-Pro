package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.response.StockMovementResponse;
import com.djbc.dutyfree.domain.entity.Product;
import com.djbc.dutyfree.domain.entity.Sommier;
import com.djbc.dutyfree.domain.entity.Stock;
import com.djbc.dutyfree.domain.entity.StockMovement;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.ProductRepository;
import com.djbc.dutyfree.repository.SommierRepository;
import com.djbc.dutyfree.repository.StockMovementRepository;
import com.djbc.dutyfree.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final SommierRepository sommierRepository;
    private final StockMovementRepository stockMovementRepository;

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
    public List<StockMovementResponse> getStockMovements() {
        log.info("Starting to fetch all stock movements");
        
        try {
            List<StockMovement> movements = stockMovementRepository.findAll();
            log.info("Found {} stock movements from database", movements != null ? movements.size() : 0);

            if (movements == null || movements.isEmpty()) {
                log.info("No stock movements found, returning empty list");
                return new ArrayList<>();
            }

            List<StockMovementResponse> responses = new ArrayList<>();
            
            for (StockMovement movement : movements) {
                try {
                    if (movement == null) {
                        log.warn("Skipping null movement");
                        continue;
                    }

                    Long stockId = null;
                    Long productId = null;
                    String productName = "Unknown Product";
                    String productCode = null;

                    // Récupérer les infos du produit via Stock (pas directement)
                    try {
                        Stock stock = movement.getStock();
                        if (stock != null) {
                            stockId = stock.getId();
                            Product product = stock.getProduct();
                            if (product != null) {
                                productId = product.getId();
                                productCode = product.getSku();
                                
                                // Essayer d'abord nameFr, puis nameEn
                                String nameFr = product.getNameFr();
                                if (nameFr != null && !nameFr.trim().isEmpty()) {
                                    productName = nameFr;
                                } else {
                                    String nameEn = product.getNameEn();
                                    if (nameEn != null && !nameEn.trim().isEmpty()) {
                                        productName = nameEn;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error accessing stock/product for movement {}: {}", movement.getId(), e.getMessage());
                    }

                    // Utiliser movementDate (pas createdAt)
                    LocalDateTime date = movement.getMovementDate() != null 
                        ? movement.getMovementDate() 
                        : movement.getCreatedAt();
                    
                    Integer quantity = movement.getQuantity() != null 
                        ? movement.getQuantity() 
                        : 0;
                    
                    // Utiliser type enum
                    String type = movement.getType() != null 
                        ? movement.getType().toString() 
                        : "UNKNOWN";

                    StockMovementResponse response = StockMovementResponse.builder()
                            .id(movement.getId())
                            .productId(productId)
                            .productName(productName)
                            .type(type)
                            .quantity(quantity)
                            .date(date)
                            .build();

                    responses.add(response);
                    
                } catch (Exception e) {
                    log.error("Error processing movement {}: {}", 
                        movement != null ? movement.getId() : "null", 
                        e.getMessage(), e);
                }
            }

            log.info("Successfully processed {} stock movements", responses.size());
            return responses;
            
        } catch (Exception e) {
            log.error("Fatal error fetching stock movements: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
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

    @Transactional(readOnly = true)
    public List<Stock> getLowStock(int threshold) {
        log.info("Fetching low stock with threshold: {}", threshold);
        return stockRepository.findLowStock(threshold > 0 ? threshold : 10);
    }

    @Transactional(readOnly = true)
    public List<Stock> getAllMovements() {
        return stockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovementsByProductId(Long productId) {
        try {
            List<StockMovement> allMovements = stockMovementRepository.findByProductId(productId);
            
            return allMovements.stream()
                    .map((StockMovement movement) -> {
                        String productName = "Unknown Product";
                        String productCode = null;
                        
                        try {
                            Stock stock = movement.getStock();
                            if (stock != null && stock.getProduct() != null) {
                                Product product = stock.getProduct();
                                productCode = product.getSku();
                                productName = product.getNameFr() != null 
                                    ? product.getNameFr() 
                                    : product.getNameEn();
                            }
                        } catch (Exception e) {
                            log.warn("Error accessing product name: {}", e.getMessage());
                        }
                        
                        LocalDateTime date = movement.getMovementDate() != null 
                            ? movement.getMovementDate() 
                            : movement.getCreatedAt();
                        
                        return StockMovementResponse.builder()
                                .id(movement.getId())
                                .productId(productId)
                                .productName(productName)
                                .quantity(movement.getQuantity())
                                .type(movement.getType() != null ? movement.getType().toString() : "UNKNOWN")
                                .date(date)
                                .build();
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error fetching movements for product {}: {}", productId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public Integer getTotalStockMovementsByProduct(Long productId) {
        try {
            return stockMovementRepository.findByProductId(productId).size();
        } catch (Exception e) {
            log.error("Error counting movements for product {}: {}", productId, e.getMessage());
            return 0;
        }
    }

    @Transactional
    public void recordStockMovement(Long productId, String type, Integer quantity, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        // Parse movement type
        com.djbc.dutyfree.domain.enums.MovementType movementType;
        try {
            movementType = com.djbc.dutyfree.domain.enums.MovementType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid movement type. Must be one of: IN, OUT, ADJUSTMENT");
        }

        // Create stock movement record
        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(movementType)
                .quantity(quantity)
                .notes(reason)
                .movementDate(LocalDateTime.now())
                .build();

        stockMovementRepository.save(movement);

        log.info("Stock movement recorded for product {}: {} {} units",
                product.getSku(), movementType, quantity);
    }
}