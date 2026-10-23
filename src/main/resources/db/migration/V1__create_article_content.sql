    CREATE TABLE article_content (
    id       UUID          NOT NULL PRIMARY KEY,
    headline VARCHAR(1000) NOT NULL,
    body     VARCHAR       NOT NULL,
    language VARCHAR(35)   NOT NULL
);
