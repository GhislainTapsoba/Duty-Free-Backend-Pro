package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.entity.Customer;
import com.djbc.dutyfree.domain.entity.LoyaltyCard;
import com.djbc.dutyfree.exception.BadRequestException;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.CustomerRepository;
import com.djbc.dutyfree.repository.LoyaltyCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {

    private final LoyaltyCardRepository loyaltyCardRepository;
    private final CustomerRepository customerRepository;

    private static final BigDecimal POINTS_PER_XOF = new BigDecimal("0.01"); // 1 point per 100 XOF
    private static final int CARD_VALIDITY_YEARS = 2;

    @Transactional
    public LoyaltyCard createLoyaltyCard(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        // Check if customer already has a loyalty card
        if (loyaltyCardRepository.findByCustomerId(customerId).isPresent()) {
            throw new BadRequestException("Customer already has a loyalty card");
        }

        String cardNumber = generateCardNumber();

        LoyaltyCard loyaltyCard = LoyaltyCard.builder()
                .customer(customer)
                .cardNumber(cardNumber)
                .pointsBalance(BigDecimal.ZERO)
                .walletBalance(BigDecimal.ZERO)
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(CARD_VALIDITY_YEARS))
                .active(true)
                .tier("STANDARD")
                .discountPercentage(BigDecimal.ZERO)
                .totalPurchases(0)
                .totalSpent(BigDecimal.ZERO)
                .build();

        loyaltyCard = loyaltyCardRepository.save(loyaltyCard);
        log.info("Loyalty card created for customer {}: {}", customerId, cardNumber);

        return loyaltyCard;
    }

    @Transactional
    public LoyaltyCard addPoints(String cardNumber, BigDecimal purchaseAmount) {
        LoyaltyCard card = loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));

        if (!card.getActive()) {
            throw new BadRequestException("Loyalty card is not active");
        }

        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Loyalty card has expired");
        }

        // Calculate points
        BigDecimal points = purchaseAmount.multiply(POINTS_PER_XOF);
        card.setPointsBalance(card.getPointsBalance().add(points));

        // Update statistics
        card.setTotalPurchases(card.getTotalPurchases() + 1);
        card.setTotalSpent(card.getTotalSpent().add(purchaseAmount));
        card.setLastUsedDate(LocalDate.now());

        // Update tier based on total spent
        updateTier(card);

        card = loyaltyCardRepository.save(card);
        log.info("Added {} points to card {}", points, cardNumber);

        return card;
    }

    @Transactional
    public LoyaltyCard redeemPoints(String cardNumber, BigDecimal points) {
        LoyaltyCard card = loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));

        if (!card.getActive()) {
            throw new BadRequestException("Loyalty card is not active");
        }

        if (card.getPointsBalance().compareTo(points) < 0) {
            throw new BadRequestException("Insufficient points balance");
        }

        card.setPointsBalance(card.getPointsBalance().subtract(points));

        // Convert points to wallet balance (1 point = 1 XOF)
        card.setWalletBalance(card.getWalletBalance().add(points));

        card = loyaltyCardRepository.save(card);
        log.info("Redeemed {} points from card {}", points, cardNumber);

        return card;
    }

    @Transactional
    public LoyaltyCard addToWallet(String cardNumber, BigDecimal amount) {
        LoyaltyCard card = loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));

        if (!card.getActive()) {
            throw new BadRequestException("Loyalty card is not active");
        }

        card.setWalletBalance(card.getWalletBalance().add(amount));
        card = loyaltyCardRepository.save(card);

        log.info("Added {} XOF to wallet of card {}", amount, cardNumber);
        return card;
    }

    @Transactional
    public LoyaltyCard deductFromWallet(String cardNumber, BigDecimal amount) {
        LoyaltyCard card = loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));

        if (!card.getActive()) {
            throw new BadRequestException("Loyalty card is not active");
        }

        if (card.getWalletBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient wallet balance");
        }

        card.setWalletBalance(card.getWalletBalance().subtract(amount));
        card = loyaltyCardRepository.save(card);

        log.info("Deducted {} XOF from wallet of card {}", amount, cardNumber);
        return card;
    }

    @Transactional(readOnly = true)
    public LoyaltyCard getCardByNumber(String cardNumber) {
        return loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));
    }

    @Transactional(readOnly = true)
    public LoyaltyCard getCardByCustomer(Long customerId) {
        return loyaltyCardRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard for customer", "customerId", customerId));
    }

    @Transactional(readOnly = true)
    public List<LoyaltyCard> getExpiringCards(int daysAhead) {
        LocalDate expiryDate = LocalDate.now().plusDays(daysAhead);
        return loyaltyCardRepository.findExpiringCards(expiryDate);
    }

    @Transactional
    public LoyaltyCard renewCard(String cardNumber) {
        LoyaltyCard card = loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));

        card.setExpiryDate(LocalDate.now().plusYears(CARD_VALIDITY_YEARS));
        card.setActive(true);
        card = loyaltyCardRepository.save(card);

        log.info("Loyalty card renewed: {}", cardNumber);
        return card;
    }

    @Transactional
    public void deactivateCard(String cardNumber) {
        LoyaltyCard card = loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));

        card.setActive(false);
        loyaltyCardRepository.save(card);

        log.info("Loyalty card deactivated: {}", cardNumber);
    }

    private String generateCardNumber() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return "LC" + uuid;
    }

    private void updateTier(LoyaltyCard card) {
        BigDecimal totalSpent = card.getTotalSpent();

        if (totalSpent.compareTo(new BigDecimal("5000000")) >= 0) { // 5M XOF
            card.setTier("PLATINUM");
            card.setDiscountPercentage(new BigDecimal("15"));
        } else if (totalSpent.compareTo(new BigDecimal("2000000")) >= 0) { // 2M XOF
            card.setTier("GOLD");
            card.setDiscountPercentage(new BigDecimal("10"));
        } else if (totalSpent.compareTo(new BigDecimal("500000")) >= 0) { // 500K XOF
            card.setTier("SILVER");
            card.setDiscountPercentage(new BigDecimal("5"));
        } else {
            card.setTier("STANDARD");
            card.setDiscountPercentage(BigDecimal.ZERO);
        }
    }
}