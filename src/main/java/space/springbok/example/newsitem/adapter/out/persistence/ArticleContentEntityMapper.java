package space.springbok.example.newsitem.adapter.out.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import space.springbok.example.newsitem.domain.model.ArticleContent;

@Mapper
interface ArticleContentEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "guid", source = "guid.value")
    @Mapping(target = "version", expression = "java(content.version())")
    ArticleContentEntity toEntity(ArticleContent content);
}
