package com.antenapro.hotelcheck.sitecheck.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * One completed run (plan §4 "DB"): scores and findings as JSON, never the pages; the visitor's email and consent
 * time only once they asked for the PDF; a salted hash of the caller's address, never the address.
 */
@Entity
@Table(name = "site_check_runs", indexes = @Index(name = "idx_site_check_runs_host_created", columnList = "host, createdAt"))
public class SiteCheckRunJpaEntity {

    @Id
    private UUID id;
    @Column(nullable = false, length = 2048)
    private String requestedUrl;
    @Column(length = 2048)
    private String finalUrl;
    @Column(nullable = false)
    private String host;
    @Column(nullable = false)
    private String status;
    private Integer score;
    @Column(nullable = false, columnDefinition = "text")
    private String stagesJson;
    @Column(nullable = false, columnDefinition = "text")
    private String findingsJson;
    @Column(columnDefinition = "text")
    private String limitationsJson;
    private String email;
    private Instant consentAt;
    @Column(nullable = false)
    private String consentSource;
    @Column(nullable = false, length = 64)
    private String ipHash;
    @Column(nullable = false)
    private Instant createdAt;

    protected SiteCheckRunJpaEntity() {
    }

    public SiteCheckRunJpaEntity(UUID id, String requestedUrl, String finalUrl, String host, String status, Integer score,
                                 String stagesJson, String findingsJson, String limitationsJson, String ipHash, Instant createdAt) {
        this.id = id;
        this.requestedUrl = requestedUrl;
        this.finalUrl = finalUrl;
        this.host = host;
        this.status = status;
        this.score = score;
        this.stagesJson = stagesJson;
        this.findingsJson = findingsJson;
        this.limitationsJson = limitationsJson;
        this.consentSource = "hotel_check_form";
        this.ipHash = ipHash;
        this.createdAt = createdAt;
    }

    public void recordConsent(String email, Instant at) {
        this.email = email;
        this.consentAt = at;
    }

    public UUID getId() {
        return id;
    }

    public String getRequestedUrl() {
        return requestedUrl;
    }

    public String getFinalUrl() {
        return finalUrl;
    }

    public String getHost() {
        return host;
    }

    public String getStatus() {
        return status;
    }

    public Integer getScore() {
        return score;
    }

    public String getStagesJson() {
        return stagesJson;
    }

    public String getFindingsJson() {
        return findingsJson;
    }

    public String getLimitationsJson() {
        return limitationsJson;
    }

    public String getEmail() {
        return email;
    }

    public Instant getConsentAt() {
        return consentAt;
    }

    public String getConsentSource() {
        return consentSource;
    }

    public String getIpHash() {
        return ipHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
