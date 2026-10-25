package space.springbok.example.newsitem.adapter.in.web;

import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import space.springbok.example.newsitem.domain.model.ArticleContent;

/**
 * Renders a news item as NewsML-G2 News Item XML.
 */
@Component
class NewsItemXmlRenderer {

    private static final String NEWSML_G2_NAMESPACE = "http://iptc.org/std/nar/2006-10-01/";
    private static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
    private static final String IPTC_CATALOG = "http://www.iptc.org/std/catalog/catalog.IPTC-G2-Standards_38.xml";

    private final String provider;

    NewsItemXmlRenderer(@Value("${newsml.provider:springbok.space}") String provider) {
        this.provider = provider;
    }

    String render(ArticleContent content) {
        StringWriter out = new StringWriter();
        try {
            XMLStreamWriter xml = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
            xml.writeStartDocument("UTF-8", "1.0");
            xml.writeStartElement("newsItem");
            xml.writeDefaultNamespace(NEWSML_G2_NAMESPACE);
            xml.writeAttribute("guid", content.guid().value());
            xml.writeAttribute("version", String.valueOf(content.version()));
            xml.writeAttribute("standard", "NewsML-G2");
            xml.writeAttribute("standardversion", "2.34");
            xml.writeAttribute("xml", XMLConstants.XML_NS_URI, "lang", content.language());
            xml.writeEmptyElement("catalogRef");
            xml.writeAttribute("href", IPTC_CATALOG);
            writeItemMeta(xml, content);
            writeContentMeta(xml, content);
            writeContentSet(xml, content);
            xml.writeEndElement();
            xml.close();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("could not render the news item as XML", e);
        }
        return out.toString();
    }

    private void writeItemMeta(XMLStreamWriter xml, ArticleContent content) throws XMLStreamException {
        xml.writeStartElement("itemMeta");
        xml.writeEmptyElement("itemClass");
        xml.writeAttribute("qcode", "ninat:text");
        xml.writeEmptyElement("provider");
        xml.writeAttribute("literal", provider);
        xml.writeStartElement("versionCreated");
        xml.writeCharacters(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(content.versionCreated()));
        xml.writeEndElement();
        xml.writeEmptyElement("pubStatus");
        xml.writeAttribute("qcode", "stat:usable");
        xml.writeEndElement();
    }

    private void writeContentMeta(XMLStreamWriter xml, ArticleContent content) throws XMLStreamException {
        xml.writeStartElement("contentMeta");
        xml.writeStartElement("headline");
        xml.writeCharacters(content.headline());
        xml.writeEndElement();
        xml.writeEndElement();
    }

    private void writeContentSet(XMLStreamWriter xml, ArticleContent content) throws XMLStreamException {
        xml.writeStartElement("contentSet");
        xml.writeStartElement("inlineXML");
        xml.writeAttribute("contenttype", "application/xhtml+xml");
        xml.writeStartElement("html");
        xml.writeDefaultNamespace(XHTML_NAMESPACE);
        xml.writeStartElement("body");
        for (String paragraph : content.body().split("\r?\n\r?\n")) {
            xml.writeStartElement("p");
            xml.writeCharacters(paragraph);
            xml.writeEndElement();
        }
        xml.writeEndElement();
        xml.writeEndElement();
        xml.writeEndElement();
        xml.writeEndElement();
    }
}
