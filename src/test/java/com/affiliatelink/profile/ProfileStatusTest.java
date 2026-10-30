package com.affiliatelink.profile;

import com.affiliatelink.common.InvalidProfileStatusException;
import com.affiliatelink.profile.internal.entity.ProfileStatus;
import com.affiliatelink.profile.internal.entity.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfileStatusTest {
    @Test
    void acceptsOnlyTheThreeTransitionsFromTheBrief() {
        for (ProfileStatus current : ProfileStatus.values()) {
            for (ProfileStatus target : ProfileStatus.values()) {
                Publisher profile = new Publisher();
                profile.setStatus(current);
                boolean allowed = (current == ProfileStatus.PENDING && target == ProfileStatus.ACTIVE)
                        || (current == ProfileStatus.ACTIVE && target == ProfileStatus.SUSPENDED)
                        || (current == ProfileStatus.SUSPENDED && target == ProfileStatus.ACTIVE);
                if (allowed) {
                    profile.changeStatus(target);
                    assertEquals(target, profile.getStatus());
                } else {
                    assertThrows(InvalidProfileStatusException.class, () -> profile.changeStatus(target));
                    assertEquals(current, profile.getStatus());
                }
            }
        }
    }
}
