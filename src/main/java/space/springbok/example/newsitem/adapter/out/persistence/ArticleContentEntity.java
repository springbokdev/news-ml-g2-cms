package space.springbok.example.newsitem.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "article_content")
@Getter
@Setter
@NoArgsConstructor
class ArticleContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255, unique = true)
    private String guid;

    @Column(nullable = false, length = 1000)
    private String headline;

    @Column(nullable = false)
    private String body;

    @Column(nullable = false, length = 35)
    private String language;

    @Column(nullable = false)
    private int version;
}
