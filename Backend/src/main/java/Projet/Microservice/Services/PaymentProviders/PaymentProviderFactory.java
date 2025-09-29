package Projet.Microservice.Services.PaymentProviders;

import Projet.Microservice.Entities.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentProviderFactory {

    private final Map<String, PaymentProviderService> providers;
    private final Map<String, List<PaymentMethod>> currencyToMethods = new HashMap<>();

    @Autowired
    public PaymentProviderFactory(Map<String, PaymentProviderService> providers) {
        this.providers = providers;
        initializeCurrencyMap();
    }

    private void initializeCurrencyMap() {
        // Map PayPal
        PayPalPaymentService paypal = (PayPalPaymentService) providers.get("payPalPaymentService");
        for (String currency : PayPalPaymentService.getSupportedCurrencies()) {
            currencyToMethods
                    .computeIfAbsent(currency, k -> new ArrayList<>())
                    .add(PaymentMethod.PAYPAL);
        }

        // Map Stripe
        StripePaymentService stripe = (StripePaymentService) providers.get("stripePaymentService");
        for (String currency : StripePaymentService.getSupportedCurrencies()) {
            currencyToMethods
                    .computeIfAbsent(currency, k -> new ArrayList<>())
                    .add(PaymentMethod.STRIPE);
        }

    }

    public PaymentProviderService getProvider(PaymentMethod method) {
        switch (method) {
            case PAYPAL:
                return providers.get("payPalPaymentService");
            case STRIPE:
                return providers.get("stripePaymentService");
            default:
                throw new IllegalArgumentException("No provider available for method: " + method);
        }
    }

    public List<PaymentMethod> getAvailableMethods(String currency) {
        return currencyToMethods.getOrDefault(currency.toUpperCase(), List.of());
    }
}
