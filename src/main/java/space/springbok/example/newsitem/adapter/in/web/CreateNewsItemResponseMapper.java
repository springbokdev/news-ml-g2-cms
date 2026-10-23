package space.springbok.example.newsitem.adapter.in.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import space.springbok.example.newsitem.domain.model.NewsItemGuid;

@Mapper
interface CreateNewsItemResponseMapper {

    @Mapping(target = "guid", source = "value")
    @Mapping(target = "version", constant = "1")
    CreateNewsItemResponse toResponse(NewsItemGuid guid);
}
