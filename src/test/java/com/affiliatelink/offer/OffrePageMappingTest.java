package com.affiliatelink.offer;

import com.affiliatelink.offer.internal.dto.OffreResponse;
import com.affiliatelink.offer.internal.mapper.OffreMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

class OffrePageMappingTest {
    @Test
    void anEmptyPageContainsAnEmptyListRatherThanNull() {
        OffreMapper mapper = Mappers.getMapper(OffreMapper.class);
        var response = mapper.toPageResponse(Page.<OffreResponse>empty(PageRequest.of(0, 5)));
        assertNotNull(response.content());
        assertTrue(response.content().isEmpty());
        assertEquals(0, response.page());
        assertEquals(5, response.size());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
    }
}
