package Replan.AI.controller;

import Replan.AI.dto.RecoveryRequest;
import Replan.AI.model.RecoveryPlan;
import Replan.AI.service.RecoveryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recovery")
public class RecoveryController {

    private final RecoveryService recoveryService;

    public RecoveryController(
            RecoveryService recoveryService
    ) {
        this.recoveryService = recoveryService;
    }

    @PostMapping("/habits/{habitId}/suggest")
    @ResponseStatus(HttpStatus.CREATED)
    public RecoveryPlan suggestRecovery(
            @PathVariable String habitId,
            @Valid @RequestBody RecoveryRequest request
    ) {
        return recoveryService.suggestRecovery(
                habitId,
                request
        );
    }

    @PatchMapping("/{recoveryId}/confirm")
    public RecoveryPlan confirmRecovery(
            @PathVariable String recoveryId
    ) {
        return recoveryService.confirmRecovery(
                recoveryId
        );
    }

    @PatchMapping("/{recoveryId}/reject")
    public RecoveryPlan rejectRecovery(
            @PathVariable String recoveryId
    ) {
        return recoveryService.rejectRecovery(
                recoveryId
        );
    }

    @GetMapping("/user/{userId}")
    public List<RecoveryPlan> getUserRecoveryPlans(
            @PathVariable String userId
    ) {
        return recoveryService.getUserRecoveryPlans(
                userId
        );
    }
}