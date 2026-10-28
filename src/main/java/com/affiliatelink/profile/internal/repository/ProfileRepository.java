package com.affiliatelink.profile.internal.repository;

import com.affiliatelink.profile.internal.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile , Long> {
}
