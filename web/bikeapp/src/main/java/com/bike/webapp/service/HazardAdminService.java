package com.bike.webapp.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bike.webapp.dto.HazardReview;
import com.bike.webapp.dto.HazardStats;
import com.bike.webapp.mapper.HazardAdminMapper;

@Service
@Transactional
public class HazardAdminService {

    private static final Set<String> ALLOWED_STATUSES = new HashSet<>(Arrays.asList(
            "PENDING", "REVIEWING", "APPROVED", "REJECTED", "EXPIRED", "INACTIVE"));
    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
            "ROCKFALL", "STEEP_SLOPE", "ROAD_DAMAGE", "VEHICLE_TRAFFIC",
            "CONSTRUCTION", "BLOCKED", "OTHER"));

    @Autowired
    private HazardAdminMapper mapper;

    @Transactional(readOnly = true)
    public List<HazardReview> findAll(String keyword, String hazardType, String status,
                                      String dateFrom, String dateTo, String reported, String sort) {
        return mapper.findAll(trimToNull(keyword), trimToNull(hazardType), trimToNull(status),
                trimToNull(dateFrom), trimToNull(dateTo), trimToNull(reported), sort);
    }

    @Transactional(readOnly = true)
    public List<HazardReview> findRecent(int limit) {
        return mapper.findRecent(limit);
    }

    @Transactional(readOnly = true)
    public HazardReview findById(int id) {
        return mapper.findById(id);
    }

    @Transactional(readOnly = true)
    public HazardStats getStats() {
        HazardStats stats = new HazardStats();
        stats.setTotal(mapper.countAll());
        stats.setPending(mapper.countByStatus("PENDING"));
        stats.setReviewing(mapper.countByStatus("REVIEWING"));
        stats.setApproved(mapper.countByStatus("APPROVED"));
        stats.setRejected(mapper.countByStatus("REJECTED"));
        return stats;
    }

    public void approve(int id, String adminId) {
        changeStatus(id, "APPROVED", adminId, null);
    }

    public void reject(int id, String adminId, String reason) {
        changeStatus(id, "REJECTED", adminId, trimToNull(reason));
    }

    public void update(int id, HazardReview changes, String adminId) {
        HazardReview before = required(id);
        mapper.ensureReview(id);
        changes.setId(id);
        validateStatus(changes.getStatus());
        validateType(changes.getHazardType());
        mapper.updateDangerDetails(changes);
        mapper.updateReviewDetails(changes);
        String nextStatus = trimToNull(changes.getStatus());
        if (nextStatus != null && !nextStatus.equals(before.getStatus())) {
            mapper.updateReviewStatus(id, nextStatus, adminId, null);
            mapper.updateLegacyStatus(id, legacyStatus(nextStatus));
            mapper.insertHistory(id, "UPDATE", before.getStatus(), nextStatus, adminId, "관리자 수정");
        } else {
            mapper.insertHistory(id, "UPDATE", before.getStatus(), before.getStatus(), adminId, "관리자 수정");
        }
    }

    public void merge(int id, int targetId, String adminId) {
        if (id == targetId) throw new IllegalArgumentException("같은 위험지역으로 병합할 수 없습니다.");
        HazardReview source = required(id);
        required(targetId);
        mapper.ensureReview(id);
        mapper.mergeHazard(id, targetId);
        mapper.updateLegacyStatus(id, "4");
        mapper.insertHistory(id, "MERGE", source.getStatus(), "INACTIVE", adminId,
                "위험지역 #" + targetId + "로 병합");
    }

    private void changeStatus(int id, String nextStatus, String adminId, String reason) {
        HazardReview before = required(id);
        mapper.ensureReview(id);
        mapper.updateReviewStatus(id, nextStatus, adminId, reason);
        mapper.updateLegacyStatus(id, legacyStatus(nextStatus));
        mapper.insertHistory(id, nextStatus, before.getStatus(), nextStatus, adminId, reason);
    }

    private HazardReview required(int id) {
        HazardReview hazard = mapper.findById(id);
        if (hazard == null) throw new IllegalArgumentException("위험지역을 찾을 수 없습니다.");
        return hazard;
    }

    private String legacyStatus(String status) {
        if ("APPROVED".equals(status)) return "1";
        if ("REJECTED".equals(status)) return "2";
        if ("REVIEWING".equals(status)) return "3";
        if ("INACTIVE".equals(status) || "EXPIRED".equals(status)) return "4";
        return "0";
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }

    private void validateStatus(String status) {
        if (status != null && !ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("허용되지 않은 검수 상태입니다.");
        }
    }

    private void validateType(String hazardType) {
        if (hazardType != null && !ALLOWED_TYPES.contains(hazardType)) {
            throw new IllegalArgumentException("허용되지 않은 위험 유형입니다.");
        }
    }
}
