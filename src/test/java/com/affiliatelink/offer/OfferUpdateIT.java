package com.affiliatelink.offer;

import com.affiliatelink.offer.internal.entity.Offre;
import com.affiliatelink.offer.internal.entity.Tag;
import com.affiliatelink.offer.internal.repository.OffreRepository;
import com.affiliatelink.offer.internal.repository.TagRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Transactional
class OfferUpdateIT {
    @Autowired WebApplicationContext context;
    @Autowired OffreRepository offres;
    @Autowired TagRepository tags;
    @Autowired EntityManager entityManager;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void updatesFieldsWithoutDeletingATagSharedByAnotherOffer() throws Exception {
        var shared = new Tag();
        shared.setName("shared-" + UUID.randomUUID());
        tags.saveAndFlush(shared);
        var first = newOffre();
        var second = newOffre();
        first.addTag(shared);
        second.addTag(shared);
        offres.saveAndFlush(first);
        offres.saveAndFlush(second);
        Long firstId = first.getId();
        Long secondId = second.getId();
        Long sharedId = shared.getId();
        entityManager.clear();
        var originalDate = offres.findById(firstId).orElseThrow().getCreatedAt();
        entityManager.clear();
        String replacement = "new-" + UUID.randomUUID();
        mvc.perform(put("/api/offres/" + firstId).contentType(MediaType.APPLICATION_JSON)
                .content(body("Nouveau titre", "[\" " + replacement.toUpperCase(java.util.Locale.ROOT)
                        + " \",\"" + replacement + "\"]")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstId))
                .andExpect(jsonPath("$.advertiserId").value(123L))
                .andExpect(jsonPath("$.title").value("Nouveau titre"))
                .andExpect(jsonPath("$.tags.length()").value(1))
                .andExpect(jsonPath("$.tags[0].name").value(replacement));
        entityManager.clear();
        var saved = offres.findById(firstId).orElseThrow();
        assertEquals(originalDate, saved.getCreatedAt());
        assertEquals(123L, saved.getAdvertiserId());
        assertEquals("Nouvelle description", saved.getDescription());
        assertEquals("https://example.com/new", saved.getLandingUrl());
        assertEquals("SPORT", saved.getNiche());
        assertTrue(tags.existsById(sharedId));
        assertEquals(sharedId, offres.findById(secondId).orElseThrow().getTags().iterator().next().getId());
        assertEquals(1, tags.findById(sharedId).orElseThrow().getOffres().size());
    }

    @Test
    void emptyTagsClearOnlyTheAssociations() throws Exception {
        var tag = new Tag();
        tag.setName("clear-" + UUID.randomUUID());
        tags.saveAndFlush(tag);
        var offre = newOffre();
        offre.addTag(tag);
        offres.saveAndFlush(offre);
        entityManager.clear();
        mvc.perform(put("/api/offres/" + offre.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(body("Titre", "[]")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tags").isEmpty());
        entityManager.clear();
        assertTrue(offres.findById(offre.getId()).orElseThrow().getTags().isEmpty());
        assertTrue(tags.existsById(tag.getId()));
    }

    @Test
    void returns404ForAMissingOfferAnd400WithoutChangingAnExistingOffer() throws Exception {
        mvc.perform(put("/api/offres/" + Long.MAX_VALUE).contentType(MediaType.APPLICATION_JSON)
                .content(body("Titre", "[]")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").exists());
        var offre = offres.saveAndFlush(newOffre());
        mvc.perform(put("/api/offres/" + offre.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(body("", "[]")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").exists());
        entityManager.clear();
        assertEquals("Titre initial", offres.findById(offre.getId()).orElseThrow().getTitle());
    }

    private Offre newOffre() {
        var offre = new Offre();
        offre.setAdvertiserId(123L);
        offre.setTitle("Titre initial");
        offre.setDescription("Description initiale");
        offre.setLandingUrl("https://example.com/old");
        offre.setNiche("TECH");
        return offre;
    }

    private String body(String title, String tagJson) {
        return """
                {"title":"%s","description":"Nouvelle description",
                 "landingUrl":"https://example.com/new","niche":"SPORT","tags":%s}
                """.formatted(title, tagJson);
    }
}
