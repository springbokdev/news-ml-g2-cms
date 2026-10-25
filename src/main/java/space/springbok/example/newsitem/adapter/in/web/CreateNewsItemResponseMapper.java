package space.springbok.example.newsitem.adapter.in.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import space.springbok.example.newsitem.domain.model.ArticleContent;

@Mapper
interface CreateNewsItemResponseMapper {

    @Mapping(target = "guid", source = "guid.value")
    @Mapping(target = "version", expression = "java(content.version())")
    CreateNewsItemResponse toResponse(ArticleContent content);
}
