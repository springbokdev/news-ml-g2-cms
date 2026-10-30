package space.springbok.example.newsitem.adapter.in.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import space.springbok.example.newsitem.domain.model.ArticleContent;

@Mapper
interface UpdateNewsItemResponseMapper {

    @Mapping(target = "guid", source = "guid.value")
    UpdateNewsItemResponse toResponse(ArticleContent content);
}
