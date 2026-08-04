package com.bike.webapp.dto;

import java.time.LocalDateTime;

public class HazardReview {
    private Integer id;
    private String reporter;
    private String latitude;
    private String longitude;
    private String address;
    private String image;
    private String description;
    private String legacyStatus;
    private String title;
    private String hazardType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private String rejectionReason;
    private Integer confirmCount;
    private Integer reportCount;
    private Boolean active;
    private Integer mergedInto;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }
    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }
    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLegacyStatus() { return legacyStatus; }
    public void setLegacyStatus(String legacyStatus) { this.legacyStatus = legacyStatus; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getHazardType() { return hazardType; }
    public void setHazardType(String hazardType) { this.hazardType = hazardType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Integer getConfirmCount() { return confirmCount; }
    public void setConfirmCount(Integer confirmCount) { this.confirmCount = confirmCount; }
    public Integer getReportCount() { return reportCount; }
    public void setReportCount(Integer reportCount) { this.reportCount = reportCount; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Integer getMergedInto() { return mergedInto; }
    public void setMergedInto(Integer mergedInto) { this.mergedInto = mergedInto; }

    public String getStatusLabel() {
        if ("APPROVED".equals(status)) return "승인 완료";
        if ("REJECTED".equals(status)) return "반려됨";
        if ("REVIEWING".equals(status)) return "검토 중";
        if ("INACTIVE".equals(status)) return "비활성";
        if ("EXPIRED".equals(status)) return "만료";
        return "검토 대기";
    }

    public String getHazardTypeLabel() {
        if ("ROCKFALL".equals(hazardType)) return "낙석";
        if ("STEEP_SLOPE".equals(hazardType)) return "급경사";
        if ("ROAD_DAMAGE".equals(hazardType)) return "노면 파손";
        if ("VEHICLE_TRAFFIC".equals(hazardType)) return "차량 통행";
        if ("CONSTRUCTION".equals(hazardType)) return "공사 중";
        if ("BLOCKED".equals(hazardType)) return "길 막힘";
        return "기타";
    }
}
