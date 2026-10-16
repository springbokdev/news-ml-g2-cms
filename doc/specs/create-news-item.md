# Create a NewsML-G2 News Item

**As a journalist, I want to create a NewsML-G2 news item so that I can publish text-based news content to partner organisations.**

## Rules and Examples

### Rule: Must require a headline, a body and a language to create an article

| Headline | Body | Language | Outcome |
|---|---|---|---|
| "Floods hit Cape Town" | 3 paragraphs | en | Created |
| *(blank)* | 3 paragraphs | en | Rejected: headline required |
| "Floods hit Cape Town" | *(blank)* | en | Rejected: body required |
| "Floods hit Cape Town" | 3 paragraphs | *(missing)* | Rejected: language required |

A value that contains only whitespace counts as blank.

---

### Rule: Must accept the language only as a well-formed BCP 47 tag

| Language | Outcome |
|---|---|
| en | Accepted |
| zu-ZA | Accepted (language plus region) |
| english | Rejected: not a valid language tag |

---

### Rule: Must give every new news item a globally unique GUID

The GUID is a URN made from the configured provider, the creation date and a unique part:
`urn:newsml:<provider>:<yyyyMMdd>:<unique-id>`.

- **Example:** The one where an article is created on 16 Oct 2026 by provider springbok.space, and its GUID is `urn:newsml:springbok.space:20261016:<unique-id>`.
- **Counter-example:** The one where two articles with the same headline are created on the same day, and they get different GUIDs.
- **Counter-example:** The one where the journalist supplies their own GUID, and the system ignores it and assigns its own.

---

### Rule: Must give a newly created news item version 1

- **Example:** The one where a new article is created, and its version is 1.
- **Counter-example:** The one where the journalist supplies version 5 on creation, and the item still gets version 1.

---

### Rule: Must populate the mandatory NewsML-G2 metadata on every news item

| Element / attribute | Value |
|---|---|
| `standard` | NewsML-G2 |
| `standardversion` | 2.34 |
| `catalogRef` | IPTC catalog `http://www.iptc.org/std/catalog/catalog.IPTC-G2-Standards_38.xml` |
| `xml:lang` | the journalist's language, e.g. en |
| `itemMeta/itemClass` | `ninat:text` |
| `itemMeta/provider` | the configured provider (default springbok.space) |
| `itemMeta/versionCreated` | the creation timestamp, with timezone offset |
| `itemMeta/pubStatus` | `stat:usable` |
| `contentMeta/headline` | the journalist's headline |

The journalist cannot set the provider, the item class or the publication status.

---

### Rule: Must carry the body as XHTML paragraphs

The body goes in `contentSet/inlineXML` with content type `application/xhtml+xml`. Each paragraph
becomes one `<p>`, and paragraphs are separated by a blank line.

- **Example:** The one where the body is "Rain fell.⏎⏎Roads closed.⏎⏎Schools shut.", and it renders as three `<p>` elements in order.
- **Counter-example:** The one where the body is a single paragraph with no blank lines, and it renders as exactly one `<p>`.

---

### Rule: Must produce News Item XML that validates against the NewsML-G2 2.34 schema

- **Example:** The one where a created article's XML passes validation against the IPTC NewsML-G2 2.34 XSD.
- **Counter-example:** The one where the headline is `Q&A: <Budget> 2026`, and the characters are escaped so the XML stays valid and the headline reads exactly as entered.

---

### Rule: Must let partners retrieve the News Item XML by its GUID

Creating an item returns its GUID and version. Partners fetch the XML separately using the GUID.

- **Example:** The one where an item is created, and fetching it by its GUID returns its `<newsItem>` XML at version 1.
- **Counter-example:** The one where a GUID that was never issued is requested, and the result is "not found".

---

## Resolved decisions (for implementation)

- **Required inputs:** headline, body and a BCP 47 language tag.
- **GUID:** `urn:newsml:<provider>:<yyyyMMdd>:<unique-id>`, always assigned by the system.
- **Provider:** taken from configuration, with springbok.space as the default; one provider per deployment.
- **pubStatus:** new items are `stat:usable`.
- **Body:** XHTML `<p>` paragraphs in `inlineXML`, split on blank lines.
- **Standard:** NewsML-G2 2.34.
- **Delivery:** creating returns the GUID and version; the XML is fetched by GUID.
- **Out of scope:** updates and incrementing the version, withheld/draft workflow, sending items to partners, packages, and non-text media (images, video, audio).
