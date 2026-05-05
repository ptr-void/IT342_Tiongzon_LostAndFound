package edu.cit.tiongzon.lostandfound.feature.items;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemDTO {
    private Long id;
    private String title;
    private String description;
    private Item.ItemStatus status;
    private Item.ItemCategory category;
    private Double locationLat;
    private Double locationLng;
    private String locationDescription;
    private String imagePath;
    private Long reporterId;
    private String reporterName;
    private String reporterEmail;
    private int reporterWarningMarks;
    private String reporterAvatar;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Item.ItemStatus getStatus() { return status; }
    public void setStatus(Item.ItemStatus status) { this.status = status; }
    public Item.ItemCategory getCategory() { return category; }
    public void setCategory(Item.ItemCategory category) { this.category = category; }
    public Double getLocationLat() { return locationLat; }
    public void setLocationLat(Double locationLat) { this.locationLat = locationLat; }
    public Double getLocationLng() { return locationLng; }
    public void setLocationLng(Double locationLng) { this.locationLng = locationLng; }
    public String getLocationDescription() { return locationDescription; }
    public void setLocationDescription(String locationDescription) { this.locationDescription = locationDescription; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }
    public int getReporterWarningMarks() { return reporterWarningMarks; }
    public void setReporterWarningMarks(int reporterWarningMarks) { this.reporterWarningMarks = reporterWarningMarks; }
    public String getReporterAvatar() { return reporterAvatar; }
    public void setReporterAvatar(String reporterAvatar) { this.reporterAvatar = reporterAvatar; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
}
