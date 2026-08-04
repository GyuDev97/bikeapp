package com.bike.webapp.controller.admin;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bike.webapp.dto.HazardReview;
import com.bike.webapp.service.HazardAdminService;

@RestController
@RequestMapping("/admin/api/hazards")
public class AdminHazardApiController {

    @Autowired
    private HazardAdminService hazardService;

    @PostMapping("/{id}/approve")
    public Map<String, Object> approve(@PathVariable int id, Principal principal) {
        hazardService.approve(id, principal.getName());
        return success(id);
    }

    @PostMapping("/{id}/reject")
    public Map<String, Object> reject(@PathVariable int id,
                                      @RequestBody(required = false) DecisionRequest request,
                                      Principal principal) {
        hazardService.reject(id, principal.getName(), request == null ? null : request.getReason());
        return success(id);
    }

    @PatchMapping("/{id}")
    public Map<String, Object> update(@PathVariable int id,
                                      @RequestBody HazardReview changes,
                                      Principal principal) {
        hazardService.update(id, changes, principal.getName());
        return success(id);
    }

    @PostMapping("/{id}/merge")
    public Map<String, Object> merge(@PathVariable int id,
                                     @RequestBody MergeRequest request,
                                     Principal principal) {
        hazardService.merge(id, request.getTargetId(), principal.getName());
        return success(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("message", exception.getMessage()));
    }

    private Map<String, Object> success(int id) {
        return Collections.<String, Object>singletonMap("hazard", hazardService.findById(id));
    }

    public static class DecisionRequest {
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class MergeRequest {
        private int targetId;
        public int getTargetId() { return targetId; }
        public void setTargetId(int targetId) { this.targetId = targetId; }
    }
}
