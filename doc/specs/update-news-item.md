# Update a News Item

**As an editor, I want to update an existing news item so that corrections and updates can be distributed.**

## Rules and Examples

### Rule: Must save an accepted update as a new version of the same news item

An update replaces the headline, body and language completely. The new version is exactly what the editor sent, and the GUID never changes.

- **Example:** The one where item `urn:newsml:springbok.space:20261016:3f2b9c1e` is at version 1 with headline "Floods hit Cape Town", and the editor sends headline "Floods hit Cape Town, two dead" with the same body and `en`. The item is now version 2 with the new headline and the same GUID.
- **Counter-example:** The one where the editor updates a GUID that was never issued. The result is "not found" and nothing is stored.

---

### Rule: Must require a headline, a body, a language and an author on every update

The same content rules as creation apply: whitespace-only counts as blank, and the language must be a well-formed BCP 47 tag.

| Headline | Body | Language | Author | Outcome |
|---|---|---|---|---|
| "Floods hit Cape Town, two dead" | 3 paragraphs | en | Thandi Nkosi | New version |
| *(blank)* | 3 paragraphs | en | Thandi Nkosi | Rejected: headline required |
| "Floods hit Cape Town, two dead" | 3 paragraphs | english | Thandi Nkosi | Rejected: not a valid language tag |
| "Floods hit Cape Town, two dead" | 3 paragraphs | en | *(blank)* | Rejected: author required |

A rejected update creates no version, and the item stays at its current version.

---

### Rule: Must require an author when a news item is created

This **supersedes** the first rule of `create-news-item.md`. Creation now requires a headline, a body, a language **and an author**.

- **Example:** The one where journalist Sipho Dlamini creates "Floods hit Cape Town", and version 1 records Sipho Dlamini as its author.
- **Counter-example:** The one where the create request has no author. It is rejected with "author required" and nothing is stored.

---

### Rule: Must assign the next version number automatically

| Current version | Update | Resulting version |
|---|---|---|
| 1 | accepted | 2 |
| 2 | accepted | 3 |
| 2 | accepted, content identical to version 2 | 3 |
| 2 | rejected | 2 (no change) |

The identical-content row is the boundary: every accepted update creates a version, even when nothing changed.

---

### Rule: Must reject an update that was not made against the latest version

The editor states which version they edited. The update is accepted only if that is still the latest version.

| Latest version | Version the editor edited | Outcome |
|---|---|---|
| 2 | 2 | Accepted as version 3 |
| 3 | 2 | Rejected as a conflict; item stays at version 3 |
| 2 | 5 | Rejected as a conflict; item stays at version 2 |

The second row is the boundary. Two editors both edit version 2, the first becomes version 3, and the second is rejected instead of silently overwriting the first.

---

### Rule: Must keep every previous version retrievable

Fetching by GUID alone returns the latest version. Any earlier version can be fetched by GUID and version number. Each version's XML carries its own `version` attribute and `versionCreated`.

- **Example:** The one where an item is at version 2, and fetching it by GUID returns the version 2 XML with headline "Floods hit Cape Town, two dead".
- **Example:** The one where an item is at version 2, and fetching version 1 returns XML with `version="1"`, headline "Floods hit Cape Town" and the original `versionCreated`.
- **Counter-example:** The one where version 3 of an item at version 2 is requested, and the result is "not found".

---

### Rule: Must record the author and timestamp of every version

- **Example:** The one where editor Thandi Nkosi updates the item at 2026-10-17T08:15:00+02:00. Version 2 records Thandi Nkosi as its author and 2026-10-17T08:15:00+02:00 as its `versionCreated`.
- **Counter-example:** The one where version 2 is created. Version 1 still records Sipho Dlamini and its original `versionCreated`.

---

### Rule: Must provide the modification history of a news item

Each history entry has the version number, the author and `versionCreated`, oldest first.

- **Example:** The one where an item has two versions, and its history is:

  | Version | Author | Version created |
  |---|---|---|
  | 1 | Sipho Dlamini | 2026-10-16T09:30:00+02:00 |
  | 2 | Thandi Nkosi | 2026-10-17T08:15:00+02:00 |

- **Counter-example:** The one where an item was never updated, and its history has only version 1.
- **Counter-example:** The one where the history of a GUID that was never issued is requested, and the result is "not found".

---

## Resolved decisions (for implementation)

- **Author:** a required free-text name in the create and update requests, with no authentication. It supersedes the create spec's required inputs.
- **Update semantics:** full replacement of headline, body and language, validated like creation. Language may change.
- **Concurrency:** optimistic. The update names the version it was made against, and a stale or unknown version is a conflict.
- **No-op updates:** still create a new version.
- **Corrections vs updates:** no distinction. `sig:correction` and `edNote` are out of scope.
- **Author in XML:** not rendered. The author appears only in the history. The XML changes only in `version` and `versionCreated`.
- **History:** version, author and `versionCreated`, oldest first.
- **Persistence:** every version is stored and none is overwritten. Rejected updates are never stored.
- **Out of scope:** deleting or withdrawing items or versions, `pubStatus` changes, correction signals, field-level diffs, sending updates to partners, and authentication and authorisation.
