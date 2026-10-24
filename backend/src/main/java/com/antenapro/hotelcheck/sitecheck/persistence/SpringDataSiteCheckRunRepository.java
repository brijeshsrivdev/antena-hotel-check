package com.antenapro.hotelcheck.sitecheck.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataSiteCheckRunRepository extends JpaRepository<SiteCheckRunJpaEntity, UUID> {
}
