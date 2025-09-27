package Projet.Microservice.Controllers;

import Projet.Microservice.Entities.Currency;
import Projet.Microservice.Services.CurrencyConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    @Autowired
    private CurrencyConversionService conversionService;

    @GetMapping("/convert")
    public BigDecimal convert(
            @RequestParam BigDecimal amount,
            @RequestParam Currency from,
            @RequestParam Currency to
    ) {
        return conversionService.convert(amount, from, to);
    }
}
