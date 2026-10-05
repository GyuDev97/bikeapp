package com.bike.webapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.bike.webapp.dto.HazardReview;

@Repository
public interface HazardAdminMapper {
    List<HazardReview> findAll(@Param("keyword") String keyword,
                               @Param("hazardType") String hazardType,
                               @Param("status") String status,
                               @Param("dateFrom") String dateFrom,
                               @Param("dateTo") String dateTo,
                               @Param("reported") String reported,
                               @Param("sort") String sort);
    List<HazardReview> findRecent(@Param("limit") int limit);
    List<HazardReview> findPublicApproved();
    HazardReview findById(@Param("id") int id);
    int countAll();
    int countByStatus(@Param("status") String status);
    int ensureReview(@Param("id") int id);
    int updateDangerDetails(HazardReview hazard);
    int updateReviewDetails(HazardReview hazard);
    int updateReviewStatus(@Param("id") int id,
                           @Param("status") String status,
                           @Param("adminId") String adminId,
                           @Param("reason") String reason);
    int updateLegacyStatus(@Param("id") int id, @Param("legacyStatus") String legacyStatus);
    int mergeHazard(@Param("id") int id, @Param("targetId") int targetId);
    int insertHistory(@Param("hazardId") int hazardId,
                      @Param("action") String action,
                      @Param("previousStatus") String previousStatus,
                      @Param("nextStatus") String nextStatus,
                      @Param("adminId") String adminId,
                      @Param("reason") String reason);
}
