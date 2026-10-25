package space.springbok.example.newsitem.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import space.springbok.example.newsitem.domain.model.ArticleContent;

@DisplayName("Rendering a news item as NewsML-G2 XML")
class NewsItemXmlRendererTest {

    private static final String NEWSML_G2_NAMESPACE = "http://iptc.org/std/nar/2006-10-01/";
    private static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

    private final NewsItemXmlRenderer renderer = new NewsItemXmlRenderer("springbok.space");

    @Test
    @DisplayName("The one where an article is rendered, and the document starts with the UTF-8 XML declaration")
    void documentStartsWithTheXmlDeclaration() {
        ArticleContent article = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        assertThat(renderer.render(article)).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    }

    @Test
    @DisplayName("The one where an article is rendered, and its standard is NewsML-G2")
    void standardIsNewsMlG2() throws Exception {
        ArticleContent article = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        Element newsItem = parse(renderer.render(article));

        assertThat(newsItem.getAttribute("standard")).isEqualTo("NewsML-G2");
    }

    @Test
    @DisplayName("The one where an article is rendered, and its guid is the GUID of the news item")
    void guidIsTheNewsItemGuid() throws Exception {
        ArticleContent article = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        Element newsItem = parse(renderer.render(article));

        assertThat(newsItem.getAttribute("guid")).isEqualTo("urn:newsml:springbok.space:20261016:3f2b9c1e");
    }

    @Test
    @DisplayName("The one where a new article is rendered, and its version is 1")
    void versionIsTheNewsItemVersion() throws Exception {
        ArticleContent article = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        Element newsItem = parse(renderer.render(article));

        assertThat(newsItem.getAttribute("version")).isEqualTo("1");
    }

    @Test
    @DisplayName("The one where an article is rendered, and its standardversion is 2.34")
    void standardVersionIs234() throws Exception {
        ArticleContent article = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        Element newsItem = parse(renderer.render(article));

        assertThat(newsItem.getAttribute("standardversion")).isEqualTo("2.34");
    }

    @Test
    @DisplayName("The one where an article in zu-ZA is rendered, and its xml:lang is zu-ZA")
    void xmlLangIsTheJournalistsLanguage() throws Exception {
        ArticleContent article = ArticleContent.create(
                "Izikhukhula eKapa", "Imvula ina kakhulu.", "zu-ZA",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        Element newsItem = parse(renderer.render(article));

        assertThat(newsItem.getAttributeNS("http://www.w3.org/XML/1998/namespace", "lang")).isEqualTo("zu-ZA");
    }

    @Test
    @DisplayName("The one where an article is rendered, and its catalogRef is the IPTC catalog")
    void catalogRefIsTheIptcCatalog() throws Exception {
        ArticleContent article = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        Element newsItem = parse(renderer.render(article));

        assertThat(attributeValues(newsItem, "catalogRef", "href"))
                .containsExactly("http://www.iptc.org/std/catalog/catalog.IPTC-G2-Standards_38.xml");
    }

    @Test
    @DisplayName("The one where an article is rendered, and its itemMeta/itemClass is ninat:text")
    void itemClassIsText() throws Exception {
        ArticleContent article = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        Element newsItem = parse(renderer.render(article));

        assertThat(elementsAt(newsItem, "itemMeta", "itemClass"))
                .extracting(itemClass -> itemClass.getAttribute("qcode"))
                .containsExactly("ninat:text");
    }

    @Test
    @DisplayName("The one where the provider is configured as example.org, and the itemMeta/provider is example.org")
    void providerIsTheConfiguredProvider() throws Exception {
        NewsItemXmlRenderer configuredRenderer = new NewsItemXmlRenderer("example.org");
        ArticleContent article = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "example.org", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        Element newsItem = parse(configuredRenderer.render(article));

        assertThat(elementsAt(newsItem, "itemMeta", "provider"))
                .extracting(provider -> provider.getAttribute("literal"))
                .containsExactly("example.org");
    }

    @Test
    @DisplayName("The one where an article is rendered, and its itemMeta/pubStatus is stat:usable")
    void pubStatusIsUsable() throws Exception {
        ArticleContent article = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        Element newsItem = parse(renderer.render(article));

        assertThat(elementsAt(newsItem, "itemMeta", "pubStatus"))
                .extracting(pubStatus -> pubStatus.getAttribute("qcode"))
                .containsExactly("stat:usable");
    }

    @Test
    @DisplayName("The one where an article headlined 'Dam levels rise' is rendered, and its contentMeta/headline is 'Dam levels rise'")
    void headlineIsTheJournalistsHeadline() throws Exception {
        ArticleContent article = ArticleContent.create(
                "Dam levels rise", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        Element newsItem = parse(renderer.render(article));

        assertThat(elementsAt(newsItem, "contentMeta", "headline"))
                .extracting(Element::getTextContent)
                .containsExactly("Dam levels rise");
    }

    @Test
    @DisplayName("The one where a version created at 2026-03-01T14:05:09-05:00 is rendered, and its itemMeta/versionCreated keeps that instant and offset")
    void versionCreatedIsTheCreationTimestampWithItsOffset() throws Exception {
        ArticleContent article = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-03-01T14:05:09-05:00"), "3f2b9c1e");

        Element newsItem = parse(renderer.render(article));

        assertThat(elementsAt(newsItem, "itemMeta", "versionCreated"))
                .extracting(Element::getTextContent)
                .containsExactly("2026-03-01T14:05:09-05:00");
    }

    @Test
    @DisplayName("The one where the body is three paragraphs separated by blank lines, and it renders as three <p> elements in order inside contentSet/inlineXML")
    void bodyParagraphsAreRenderedAsXhtmlParagraphsInOrder() throws Exception {
        ArticleContent article = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        Element newsItem = parse(renderer.render(article));

        assertThat(elementsAt(newsItem, "contentSet", "inlineXML"))
                .singleElement()
                .satisfies(inlineXml -> assertThat(textOf(inlineXml.getElementsByTagNameNS(XHTML_NAMESPACE, "p")))
                        .containsExactly("Rain fell.", "Roads closed.", "Schools shut."));
    }

    @Test
    @DisplayName("The one where paragraphs are separated by Windows line endings (CRLF blank lines), and they render as two <p> elements")
    void crlfSeparatedParagraphsAreRenderedAsSeparateParagraphElements() throws Exception {
        ArticleContent article = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\r\n\r\nRoads closed.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        Element newsItem = parse(renderer.render(article));

        assertThat(elementsAt(newsItem, "contentSet", "inlineXML"))
                .singleElement()
                .satisfies(inlineXml -> assertThat(textOf(inlineXml.getElementsByTagNameNS(XHTML_NAMESPACE, "p")))
                        .containsExactly("Rain fell.", "Roads closed."));
    }

    @Test
    @DisplayName("The one where an article is rendered, and its contentSet/inlineXML has content type application/xhtml+xml")
    void inlineXmlContentTypeIsXhtml() throws Exception {
        ArticleContent article = ArticleContent.create(
                "Floods hit Cape Town", "Rain fell.\n\nRoads closed.\n\nSchools shut.", "en",
                "springbok.space", OffsetDateTime.parse("2026-10-16T09:30:00+02:00"), "3f2b9c1e");

        Element newsItem = parse(renderer.render(article));

        assertThat(elementsAt(newsItem, "contentSet", "inlineXML"))
                .extracting(inlineXml -> inlineXml.getAttribute("contenttype"))
                .containsExactly("application/xhtml+xml");
    }

    private static Element parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)))
                .getDocumentElement();
    }

    private static List<Element> elementsAt(Element root, String... path) {
        List<Element> current = List.of(root);
        for (String name : path) {
            List<Element> next = new ArrayList<>();
            for (Element parent : current) {
                for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child instanceof Element element
                            && NEWSML_G2_NAMESPACE.equals(element.getNamespaceURI())
                            && name.equals(element.getLocalName())) {
                        next.add(element);
                    }
                }
            }
            current = next;
        }
        return current;
    }

    private static List<String> textOf(NodeList elements) {
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < elements.getLength(); i++) {
            texts.add(elements.item(i).getTextContent());
        }
        return texts;
    }

    private static List<String> attributeValues(Element root, String elementName, String attributeName) {
        NodeList elements = root.getElementsByTagNameNS(NEWSML_G2_NAMESPACE, elementName);
        List<String> values = new ArrayList<>();
        for (int i = 0; i < elements.getLength(); i++) {
            values.add(((Element) elements.item(i)).getAttribute(attributeName));
        }
        return values;
    }
}
