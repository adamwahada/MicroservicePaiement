package Projet.Microservice.Services.UserService;

import Projet.Microservice.Entities.AdminEntities.BanCause;
import Projet.Microservice.Entities.AdminEntities.UserAction;
import Projet.Microservice.Entities.AdminEntities.UserManagementAudit;
import Projet.Microservice.Repositories.AdminRepositories.UserManagementAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminManagementService {
    @Autowired
    private UserManagementAuditRepository auditRepository;

    // --- Record an audit ---
    public void recordUserAction(Long userId, Long adminId, UserAction action, BanCause reason) {
        UserManagementAudit audit = new UserManagementAudit();
        audit.setUserId(userId);
        audit.setAdminId(adminId);
        audit.setAction(action);
        audit.setReason(reason);
        audit.setTimestamp(LocalDateTime.now());
        auditRepository.save(audit);
    }

    // --- Fetch history ---
    public List<UserManagementAudit> getUserHistory(Long userId) {
        return auditRepository.findByUserId(userId);
    }

    public List<UserManagementAudit> getUserHistoryByAction(Long userId, UserAction action) {
        return auditRepository.findByUserIdAndAction(userId, action);
    }

    public List<UserManagementAudit> getUserHistoryByReason(Long userId, BanCause reason) {
        return auditRepository.findByUserIdAndReason(userId, reason);
    }

    public List<UserManagementAudit> getAdminActions(Long adminId) {
        return auditRepository.findByAdminId(adminId);
    }

    public List<UserManagementAudit> getAdminHistoryByAction(Long adminId, UserAction action) {
        return auditRepository.findByAdminIdAndAction(adminId, action);
    }

    public List<UserManagementAudit> getActionsByReason(BanCause reason) {
        return auditRepository.findByReason(reason);
    }


}
