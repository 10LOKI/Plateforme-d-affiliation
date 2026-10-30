package com.affiliatelink.profile;

import com.affiliatelink.common.GlobalExceptionHandler;
import com.affiliatelink.common.ProfileNotFoundException;
import com.affiliatelink.common.InvalidProfileStatusException;
import com.affiliatelink.profile.internal.controller.*;
import com.affiliatelink.profile.internal.dto.*;
import com.affiliatelink.profile.internal.entity.ProfileStatus;
import com.affiliatelink.profile.internal.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProfileHttpContractTest {
    private final PublisherService publishers = mock(PublisherService.class);
    private final AdvertiserService advertisers = mock(AdvertiserService.class);
    private final ProfileService profiles = mock(ProfileService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new PublisherController(publishers), new AdvertiserController(advertisers),
            new AdminProfileController(profiles))
            .setControllerAdvice(new GlobalExceptionHandler()).build();

    @Test
    void preservesTheSevenEndpoints() throws Exception {
        var publisher = new PublisherRegistrationResponse("pub@example.com", "Sara", "Amrani",
                com.affiliatelink.profile.internal.entity.Niche.TECH, "Maroc", "Tech");
        var advertiser = new AdvertiserRegistrationResponse(2L, "ad@example.com", "Demo", "TECH",
                "https://example.com", ProfileStatus.PENDING);
        when(publishers.create(any())).thenReturn(publisher);
        when(publishers.getById(1L)).thenReturn(publisher);
        when(publishers.getAll()).thenReturn(List.of(publisher));
        when(advertisers.create(any())).thenReturn(advertiser);
        when(advertisers.getById(2L)).thenReturn(advertiser);
        when(advertisers.getAll()).thenReturn(List.of(advertiser));
        mvc.perform(post("/api/publishers").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"pub@example.com","password":"TestJunior123!","firstName":"Sara",
                 "lastName":"Amrani","niche":"TECH","country":"Maroc","bio":"Tech"}
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.email").value("pub@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/publishers")).andExpect(status().isOk()).andExpect(jsonPath("$[0].niche").value("TECH"));
        mvc.perform(get("/api/publishers/1")).andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value("Sara"));
        mvc.perform(post("/api/advertisers").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"ad@example.com","password":"TestJunior123!","companyName":"Demo",
                 "sector":"TECH","website":"https://example.com"}
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/advertisers")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(2));
        mvc.perform(get("/api/advertisers/2")).andExpect(status().isOk()).andExpect(jsonPath("$.companyName").value("Demo"));
        mvc.perform(patch("/api/admin/profiles/2/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(profiles).changeStatus(2L, ProfileStatus.ACTIVE);
    }

    @Test
    void preservesValidationAndBusinessErrors() throws Exception {
        mvc.perform(post("/api/publishers").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.email").exists());
        mvc.perform(patch("/api/admin/profiles/2/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        when(advertisers.getById(99L)).thenThrow(new ProfileNotFoundException(99L));
        mvc.perform(get("/api/advertisers/99")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profil introuvable : 99"));
        doThrow(new InvalidProfileStatusException("Transition interdite")).when(profiles)
                .changeStatus(2L, ProfileStatus.SUSPENDED);
        mvc.perform(patch("/api/admin/profiles/2/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("Transition interdite"));
    }
}
