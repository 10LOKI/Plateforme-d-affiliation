package com.affiliatelink;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {
    @Test
    void respectsModuleBoundaries() {
        ApplicationModules.of(DemoApplication.class).verify();
    }
}
