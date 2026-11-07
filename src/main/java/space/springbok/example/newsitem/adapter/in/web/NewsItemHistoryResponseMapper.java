package space.springbok.example.newsitem.adapter.in.web;

import java.util.List;
import org.mapstruct.Mapper;
import space.springbok.example.newsitem.domain.model.ArticleContent;

@Mapper
interface NewsItemHistoryResponseMapper {

    NewsItemHistoryEntryResponse toEntry(ArticleContent content);

    List<NewsItemHistoryEntryResponse> toHistory(List<ArticleContent> versions);
}
