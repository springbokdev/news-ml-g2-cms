package space.springbok.example.newsitem.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ArticleContentJpaRepository extends JpaRepository<ArticleContentEntity, UUID> {

    Optional<ArticleContentEntity> findFirstByGuidOrderByVersionDesc(String guid);

}
