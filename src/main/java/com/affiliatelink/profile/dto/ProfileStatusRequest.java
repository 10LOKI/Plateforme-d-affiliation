package com.affiliatelink.profile.dto;

import com.affiliatelink.profile.entity.ProfileStatus;
import jakarta.validation.constraints.NotNull;

public record ProfileStatusRequest(@NotNull ProfileStatus status) {
}
