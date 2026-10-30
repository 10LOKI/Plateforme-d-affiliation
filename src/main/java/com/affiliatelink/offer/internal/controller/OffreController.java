package com.affiliatelink.offer.internal.controller;

import com.affiliatelink.offer.internal.dto.OffreCreateRequest;
import com.affiliatelink.offer.internal.dto.OffreResponse;
import com.affiliatelink.offer.internal.dto.OffreUpdateRequest;
import com.affiliatelink.offer.internal.dto.OffrePageResponse;
import com.affiliatelink.offer.internal.dto.OffreDetailResponse;
import com.affiliatelink.offer.internal.service.OffreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offres")
public class OffreController {
    private final OffreService offreService;

    public OffreController(OffreService offreService) {
        this.offreService = offreService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OffreResponse create(@Valid @RequestBody OffreCreateRequest request) {
        return offreService.create(request);
    }

    @PutMapping("/{id}")
    public OffreResponse update(@PathVariable Long id, @Valid @RequestBody OffreUpdateRequest request) {
        return offreService.update(id, request);
    }

    @GetMapping("/{id}")
    public OffreDetailResponse getById(@PathVariable Long id) {
        return offreService.getById(id);
    }

    @GetMapping
    public OffrePageResponse search(@RequestParam(required = false) String niche,
                                     @RequestParam(required = false) Long advertiserId,
                                     @RequestParam(required = false) String tag,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     @RequestParam(defaultValue = "id,asc") String sort) {
        return offreService.search(niche, advertiserId, tag, keyword, page, size, sort);
    }
}
