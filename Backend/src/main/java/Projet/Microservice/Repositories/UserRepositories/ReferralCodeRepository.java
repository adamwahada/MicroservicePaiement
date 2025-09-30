package Projet.Microservice.Repositories.UserRepositories;


import Projet.Microservice.Entities.UserEntities.ReferralCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReferralCodeRepository extends JpaRepository<ReferralCode, Long> {

    Optional<ReferralCode> findByCode(String code);
}