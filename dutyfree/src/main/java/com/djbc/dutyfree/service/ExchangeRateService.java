package com.djbc.dutyfree.service;

import com.djbc.dutyfree.domain.entity.ExchangeRate;
import com.djbc.dutyfree.domain.enums.Currency;
import com.djbc.dutyfree.exception.ResourceNotFoundException;
import com.djbc.dutyfree.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional
    @CacheEvict(value = "exchangeRates", allEntries = true)
    public ExchangeRate createExchangeRate(Currency currency, BigDecimal rateToXOF,
                                           LocalDate effectiveDate, LocalDate expiryDate, String source) {
        ExchangeRate exchangeRate = ExchangeRate.builder()
                .currency(currency)
                .rateToXOF(rateToXOF)
                .effectiveDate(effectiveDate)
                .expiryDate(expiryDate)
                .active(true)
                .source(source)
                .build();

        exchangeRate = exchangeRateRepository.save(exchangeRate);
        log.info("Exchange rate created for {}: {}", currency, rateToXOF);

        return exchangeRate;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "exchangeRates", key = "#currency")
    public BigDecimal getExchangeRate(Currency currency) {
        if (currency == Currency.XOF) {
            return BigDecimal.ONE;
        }

        ExchangeRate exchangeRate = exchangeRateRepository
                .findActiveByCurrencyAndDate(currency, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exchange rate not found for currency: " + currency));

        return exchangeRate.getRateToXOF();
    }

    @Transactional(readOnly = true)
    public List<ExchangeRate> getExchangeRatesByCurrency(Currency currency) {
        return exchangeRateRepository.findLatestByCurrency(currency);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRate> getAllActiveExchangeRates() {
        return exchangeRateRepository.findByActiveTrue();
    }

    @Transactional
    @CacheEvict(value = "exchangeRates", allEntries = true)
    public ExchangeRate updateExchangeRate(Long id, BigDecimal newRate) {
        ExchangeRate exchangeRate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExchangeRate", "id", id));

        exchangeRate.setRateToXOF(newRate);
        exchangeRate = exchangeRateRepository.save(exchangeRate);

        log.info("Exchange rate updated for {}: {}", exchangeRate.getCurrency(), newRate);
        return exchangeRate;
    }

    public BigDecimal convertAmount(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency == toCurrency) {
            return amount;
        }

        // Convert to XOF first
        BigDecimal amountInXOF;
        if (fromCurrency == Currency.XOF) {
            amountInXOF = amount;
        } else {
            BigDecimal fromRate = getExchangeRate(fromCurrency);
            amountInXOF = amount.multiply(fromRate);
        }

        // Convert from XOF to target currency
        if (toCurrency == Currency.XOF) {
            return amountInXOF;
        } else {
            BigDecimal toRate = getExchangeRate(toCurrency);
            return amountInXOF.divide(toRate, 2, BigDecimal.ROUND_HALF_UP);
        }
    }
}