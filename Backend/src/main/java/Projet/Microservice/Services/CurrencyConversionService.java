package Projet.Microservice.Services;

import Projet.Microservice.Entities.Currency;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class CurrencyConversionService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${currency.api.key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        System.out.println("Currency API Key: " + apiKey); // Make sure the key is injected
    }

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        String fromCurrency = from.name();
        String toCurrency = to.name();

        // Correct URL format for ExchangeRate-API
        String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/" + fromCurrency;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response != null && "success".equals(response.get("result"))) {
            Map<String, Double> rates = (Map<String, Double>) response.get("conversion_rates");
            Double rate = rates.get(toCurrency);

            if (rate == null) {
                throw new RuntimeException("Target currency not found in API response");
            }

            return amount.multiply(BigDecimal.valueOf(rate));
        }

        throw new RuntimeException("Failed to fetch conversion rate");
    }
}
