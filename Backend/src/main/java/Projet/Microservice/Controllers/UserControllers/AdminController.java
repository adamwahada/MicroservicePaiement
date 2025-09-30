package Projet.Microservice.Controllers.UserControllers;


import Projet.Microservice.Entities.AdminEntities.BanCause;
import Projet.Microservice.Entities.UserEntities.UserEntity;
import Projet.Microservice.Services.UserService.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        List<UserEntity> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // ✅ Credit user balance
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/users/{userId}/credit/{adminId}")
    public ResponseEntity<String> credit(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount,
            @PathVariable Long adminId
    ) {
        userService.creditBalance(userId, amount, adminId);
        return ResponseEntity.ok("Balance updated");
    }
//
//
//    // ✅ Debit user balance
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/users/{userId}/debit/{adminId}")
    public ResponseEntity<String> debit(@PathVariable Long userId,
                                        @RequestParam BigDecimal amount,
    @PathVariable Long adminId) {
        userService.debitBalance(userId, amount,adminId);
        return ResponseEntity.ok("Balance debited");
    }

    // =================== USER BAN MANAGEMENT ===================

    @PostMapping("/users/{userId}/ban-temporary/{adminId}")
    public ResponseEntity<String> banUserTemporarily(
            @PathVariable Long userId,
            @RequestParam int days,@PathVariable Long adminId,@RequestBody BanCause reason) {

        userService.banUserTemporarily(userId, days,adminId,reason);
        return ResponseEntity.ok("User temporarily banned for " + days + " day(s)");
    }

    @PostMapping("/users/{userId}/ban-permanent/{adminId}")
    public ResponseEntity<String> banUserPermanently(@PathVariable Long userId,@PathVariable Long adminId,@RequestBody BanCause reason) {

        userService.banUserPermanently(userId,adminId,reason);
        return ResponseEntity.ok("User permanently banned");
    }

    @PostMapping("/users/{userId}/unban/{adminId}")
    public ResponseEntity<String> unbanUser(@PathVariable Long userId,@PathVariable Long adminId) {

        userService.unbanUser(userId,adminId);
        return ResponseEntity.ok("User unbanned successfully");
    }

    @GetMapping("/users/{userId}/ban-status")
    public ResponseEntity<String> getUserBanStatus(@PathVariable Long userId) {

        String status = userService.getUserBanStatus(userId);
        return ResponseEntity.ok("User status: " + status);
    }

}
