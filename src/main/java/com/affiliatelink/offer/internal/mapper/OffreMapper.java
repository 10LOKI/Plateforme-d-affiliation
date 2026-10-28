package com.affiliatelink.offer.internal.mapper;

import com.affiliatelink.offer.internal.dto.*;
import com.affiliatelink.offer.internal.entity.Offre;
import com.affiliatelink.offer.internal.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OffreMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "campaigns", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Offre toEntity(OffreCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "advertiserId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "campaigns", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void update(OffreUpdateRequest request, @MappingTarget Offre offre);

    OffreResponse toResponse(Offre offre);

    TagResponse toResponse(Tag tag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "offres", ignore = true)
    @Mapping(target = "name", source = "name")
    Tag toTag(String name);
}
