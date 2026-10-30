package com.affiliatelink.offer.internal.repository;

import com.affiliatelink.offer.internal.entity.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OffreRepository extends JpaRepository<Offre, Long>, JpaSpecificationExecutor<Offre> {
}
