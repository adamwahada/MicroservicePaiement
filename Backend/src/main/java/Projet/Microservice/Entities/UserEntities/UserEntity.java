package Projet.Microservice.Entities.UserEntities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_id", unique = true)
    private String keycloakId;

    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String country;
    private String address;
    private String postalNumber;
    private LocalDate birthDate;
    private String referralCode;
    private boolean termsAccepted;

    private boolean active = true;
    // Optional field for temporary bans
    private LocalDateTime bannedUntil;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal pendingWithdrawals = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal withdrawableBalance = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal bonusBalance = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal pendingDeposits = BigDecimal.ZERO;


}
