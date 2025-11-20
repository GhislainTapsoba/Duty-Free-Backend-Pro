package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.ProductRequest;
import com.djbc.dutyfree.domain.dto.response.ApiResponse;
import com.djbc.dutyfree.domain.dto.response.ProductResponse;
import com.djbc.dutyfree.domain.entity.Category;
import com.djbc.dutyfree.domain.entity.Product;
import com.djbc.dutyfree.domain.entity.Supplier;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.CategoryRepository;
import com.djbc.dutyfree.repository.ProductRepository;
import com.djbc.dutyfree.repository.StockRepository;
import com.djbc.dutyfree.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final StockRepository stockRepository;

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        // Validate SKU uniqueness
        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with SKU " + request.getSku() + " already exists");
        }

        // Validate barcode uniqueness if provided
        if (request.getBarcode() != null && productRepository.existsByBarcode(request.getBarcode())) {
            throw new BadRequestException("Product with barcode " + request.getBarcode() + " already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Category categoryname = categoryRepository.findById(Long.valueOf(request.getCategoryName()))
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", request.getCategoryName()));

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .nameFr(request.getNameFr())
                .nameEn(request.getNameEn())
                .descriptionFr(request.getDescriptionFr())
                .descriptionEn(request.getDescriptionEn())
                .barcode(request.getBarcode())
                .category(category)
                .supplier(supplier)
                .purchasePrice(request.getPurchasePrice())
                .sellingPriceXOF(request.getSellingPriceXOF())
                .sellingPriceEUR(request.getSellingPriceEUR())
                .sellingPriceUSD(request.getSellingPriceUSD())
                .taxRate(request.getTaxRate())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() != null ? request.getActive() : true)
                .trackStock(request.getTrackStock() != null ? request.getTrackStock() : true)
                .minStockLevel(request.getMinStockLevel())
                .reorderLevel(request.getReorderLevel())
                .unit(request.getUnit() != null ? request.getUnit() : "PIECE")
                .build();

        product = productRepository.save(product);
        log.info("Product created: {}", product.getSku());

        return mapToResponse(product);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Validate SKU uniqueness (excluding current product)
        if (!product.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with SKU " + request.getSku() + " already exists");
        }

        // Validate barcode uniqueness if provided
        if (request.getBarcode() != null &&
                !request.getBarcode().equals(product.getBarcode()) &&
                productRepository.existsByBarcode(request.getBarcode())) {
            throw new BadRequestException("Product with barcode " + request.getBarcode() + " already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));
        }

        product.setSku(request.getSku());
        product.setNameFr(request.getNameFr());
        product.setNameEn(request.getNameEn());
        product.setDescriptionFr(request.getDescriptionFr());
        product.setDescriptionEn(request.getDescriptionEn());
        product.setBarcode(request.getBarcode());
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSellingPriceXOF(request.getSellingPriceXOF());
        product.setSellingPriceEUR(request.getSellingPriceEUR());
        product.setSellingPriceUSD(request.getSellingPriceUSD());
        product.setTaxRate(request.getTaxRate());
        product.setImageUrl(request.getImageUrl());
        product.setActive(request.getActive());
        product.setTrackStock(request.getTrackStock());
        product.setMinStockLevel(request.getMinStockLevel());
        product.setReorderLevel(request.getReorderLevel());
        product.setUnit(request.getUnit());

        product = productRepository.save(product);
        log.info("Product updated: {}", product.getSku());

        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "barcode", barcode));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products")
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAllActiveProducts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String search, Pageable pageable) {
        return productRepository.searchProducts(search, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategoryName(String categoryName) {
        return productRepository.findByCategoryId(Long.valueOf(categoryName)).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsNeedingReorder() {
        return productRepository.findProductsNeedingReorder().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setDeleted(true);
        product.setActive(false);
        productRepository.save(product);
        log.info("Product deleted: {}", product.getSku());
    }

    private ProductResponse mapToResponse(Product product) {
        Integer currentStock = stockRepository.getTotalAvailableQuantity(product.getId());

        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .nameFr(product.getNameFr())
                .nameEn(product.getNameEn())
                .descriptionFr(product.getDescriptionFr())
                .descriptionEn(product.getDescriptionEn())
                .barcode(product.getBarcode())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
                .supplierName(product.getSupplier() != null ? product.getSupplier().getName() : null)
                .purchasePrice(product.getPurchasePrice())
                .priceXOF(product.getSellingPriceXOF())
                .priceEUR(product.getSellingPriceEUR())
                .priceUSD(product.getSellingPriceUSD())
                .taxRate(product.getTaxRate())
                .imageUrl(product.getImageUrl())
                .active(product.getActive())
                .trackStock(product.getTrackStock())
                .currentStock(currentStock != null ? currentStock : 0)
                .minStockLevel(product.getMinStockLevel())
                .reorderLevel(product.getReorderLevel())
                .unit(product.getUnit())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsCurrentStock() {
        return productRepository.findAllActiveProducts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsPriceXOF() {
        return productRepository.findAllActiveProducts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProducts() {
        return productRepository.findAllActiveProducts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}