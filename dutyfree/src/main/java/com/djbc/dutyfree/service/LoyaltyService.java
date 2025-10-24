package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.dto.request.CreateLoyaltyCardRequest;
import com.djbc.dutyfree.domain.dto.response.LoyaltyCardResponse;
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
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {

    private final LoyaltyCardRepository loyaltyCardRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public LoyaltyCardResponse createCard(CreateLoyaltyCardRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        // Vérifier si le client a déjà une carte
        if (loyaltyCardRepository.findByCustomerId(request.getCustomerId()).isPresent()) {
            throw new BadRequestException("Customer already has a loyalty card");
        }

        // Générer un numéro de carte unique
        String cardNumber = generateCardNumber();

        // Date d'expiration : 2 ans par défaut
        LocalDate expiryDate = request.getExpiryDate() != null 
            ? request.getExpiryDate() 
            : LocalDate.now().plusYears(2);

        LoyaltyCard card = LoyaltyCard.builder()
                .cardNumber(cardNumber)
                .customer(customer)
                .points(0)
                .walletBalance(BigDecimal.ZERO)
                .tierLevel(request.getTierLevel())
                .expiryDate(expiryDate)
                .active(true)
                .lastUsedDate(LocalDate.now())
                .build();

        card = loyaltyCardRepository.save(card);
        log.info("Loyalty card created: {} for customer: {}", cardNumber, customer.getFirstName());

        return mapToResponse(card);
    }

    @Transactional(readOnly = true)
    public LoyaltyCardResponse getByCardNumber(String cardNumber) {
        LoyaltyCard card = loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));
        return mapToResponse(card);
    }

    @Transactional(readOnly = true)
    public LoyaltyCardResponse getByCustomerId(Long customerId) {
        LoyaltyCard card = loyaltyCardRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "customerId", customerId));
        return mapToResponse(card);
    }

    @Transactional(readOnly = true)
    public List<LoyaltyCardResponse> getExpiringCards(int daysAhead) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAhead);
        return loyaltyCardRepository.findExpiringCards(startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoyaltyCardResponse addPoints(String cardNumber, Integer points) {
        LoyaltyCard card = loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));

        if (!card.getActive()) {
            throw new BadRequestException("Loyalty card is not active");
        }

        card.setPoints(card.getPoints() + points);
        card.setLastUsedDate(LocalDate.now());

        // Mise à jour du niveau selon les points
        updateTierLevel(card);

        card = loyaltyCardRepository.save(card);
        log.info("Added {} points to card: {}", points, cardNumber);

        return mapToResponse(card);
    }

    @Transactional
    public LoyaltyCardResponse redeemPoints(String cardNumber, Integer points) {
        LoyaltyCard card = loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));

        if (!card.getActive()) {
            throw new BadRequestException("Loyalty card is not active");
        }

        if (card.getPoints() < points) {
            throw new BadRequestException("Insufficient points");
        }

        card.setPoints(card.getPoints() - points);
        card.setLastUsedDate(LocalDate.now());

        // Convertir points en argent (ex: 100 points = 1000 XOF)
        BigDecimal amount = BigDecimal.valueOf(points * 10);
        card.setWalletBalance(card.getWalletBalance().add(amount));

        card = loyaltyCardRepository.save(card);
        log.info("Redeemed {} points from card: {}", points, cardNumber);

        return mapToResponse(card);
    }

    @Transactional
    public LoyaltyCardResponse addToWallet(String cardNumber, BigDecimal amount) {
        LoyaltyCard card = loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));

        if (!card.getActive()) {
            throw new BadRequestException("Loyalty card is not active");
        }

        card.setWalletBalance(card.getWalletBalance().add(amount));
        card.setLastUsedDate(LocalDate.now());
        
        card = loyaltyCardRepository.save(card);
        log.info("Added {} to wallet of card: {}", amount, cardNumber);

        return mapToResponse(card);
    }

    @Transactional
    public LoyaltyCardResponse deductFromWallet(String cardNumber, BigDecimal amount) {
        LoyaltyCard card = loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));

        if (!card.getActive()) {
            throw new BadRequestException("Loyalty card is not active");
        }

        if (card.getWalletBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient wallet balance");
        }

        card.setWalletBalance(card.getWalletBalance().subtract(amount));
        card.setLastUsedDate(LocalDate.now());
        
        card = loyaltyCardRepository.save(card);
        log.info("Deducted {} from wallet of card: {}", amount, cardNumber);

        return mapToResponse(card);
    }

    @Transactional
    public LoyaltyCardResponse renewCard(String cardNumber) {
        LoyaltyCard card = loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));

        card.setExpiryDate(LocalDate.now().plusYears(2));
        card.setActive(true);
        
        card = loyaltyCardRepository.save(card);
        log.info("Renewed card: {}", cardNumber);

        return mapToResponse(card);
    }

    @Transactional
    public void deactivateCard(String cardNumber) {
        LoyaltyCard card = loyaltyCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyCard", "cardNumber", cardNumber));

        card.setActive(false);
        loyaltyCardRepository.save(card);
        log.info("Deactivated card: {}", cardNumber);
    }

    private void updateTierLevel(LoyaltyCard card) {
        int points = card.getPoints();
        
        if (points >= 10000) {
            card.setTierLevel("PLATINUM");
        } else if (points >= 5000) {
            card.setTierLevel("GOLD");
        } else if (points >= 2000) {
            card.setTierLevel("SILVER");
        } else {
            card.setTierLevel("BRONZE");
        }
    }

    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("LC-");
        
        for (int i = 0; i < 12; i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append("-");
            }
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }

    private LoyaltyCardResponse mapToResponse(LoyaltyCard card) {
        return LoyaltyCardResponse.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .customerId(card.getCustomer().getId())
                .customerName(card.getCustomer().getFirstName() + " " + card.getCustomer().getLastName())
                .points(card.getPoints())
                .walletBalance(card.getWalletBalance())
                .tierLevel(card.getTierLevel())
                .expiryDate(card.getExpiryDate())
                .active(card.getActive())
                .lastUsedDate(card.getLastUsedDate())
                .build();
    }
}