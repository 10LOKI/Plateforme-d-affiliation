package com.affiliatelink.profile.internal.dto;

import com.affiliatelink.profile.internal.entity.ProfileStatus;
import jakarta.validation.constraints.NotNull;

public record ProfileStatusRequest(@NotNull ProfileStatus status) {
}
