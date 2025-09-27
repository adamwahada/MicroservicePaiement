package Projet.Microservice.Services.PaymentProviders;

import Projet.Microservice.Entities.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentProviderFactory {

    private final Map<String, PaymentProviderService> providers;

    @Autowired
    public PaymentProviderFactory(Map<String, PaymentProviderService> providers) {
        this.providers = providers;
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
}
