# CLAUDE.md

## Project

SpringBoot microservice starter for implementing a CMS solution, based on the NewsML-G2 format for exchanging both individual items 
and packages of text, images, video, audio news and event or sports data.

## Build & Run

Maven wrapper (`./mvnw`, not `mvn`); Java 25 required (see `pom.xml`).

- `./mvnw test` — unit tests (H2 in PostgreSQL mode, no Docker needed).
- `./mvnw verify` — unit + acceptance (`*IT`) tests.
- `./mvnw -Dtest=ClassName#method test` — single test class/method.
- `./mvnw spring-boot:run` — needs a running PostgreSQL (see Database below).

Stack: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, Flyway, PostgreSQL driver (runtime), H2 (test).

### Database
Production persistence is PostgreSQL. The schema is owned by Flyway migrations in
`src/main/resources/db/migration` (`V<n>__description.sql`); Hibernate is `ddl-auto: validate`
and never generates schema. Connection is in `src/main/resources/application.yaml`,
overridable via `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`. (Local-run setup is in the README.)

## Coding Conventions

### REST & Spring
Constructor injection only (no field @Autowired).
@Valid on request bodies. 201 create, 200 query, 400 validation, 404 not found.
Domain exceptions for business rule violations. Map to HTTP in controller only.
Never swallow exceptions or leak infrastructure details.

## Development Process

One feature at a time, in order, no steps skipped. Each skill owns the
detailed method and stop conditions — do not inline them here.

1. Discovery: use the `discover` skill. Save the spec to doc/specs/, then STOP for user review.
2. Acceptance test: use the `accept` skill for the NEXT rule only.
3. TDD inner loop: use the `tdd` skill, one cycle per invocation.
4. Review: use the `review` skill before committing.

## Testing Standards

Acceptance tests live in .../acceptance/, unit tests beside their production code.
Domain tests: plain JUnit + AssertJ, NO Spring.
Repository tests: @DataJpaTest.
Web tests: @WebMvcTest.
Acceptance tests: @SpringBootTest + MockMvc.
For money: isEqualByComparingTo("1.60").
Inline test data per test. No shared fixtures.

## Architecture: Hexagonal (Ports & Adapters)
Domain (domain/): Pure Java. NO Spring, NO framework dependencies.
model/ — entities and value objects
service/ — business rules
Application (application/): port/in/ and port/out/ interfaces.
@Service orchestration only — no business logic here.
Adapters:
adapter/in/web/ — @RestController, DTOs only.
adapter/out/persistence/ — JPA repos and entities (NOT in domain).

Domain NEVER imports org.springframework or jakarta.persistence.
Controllers NEVER contain business logic.
Dependencies flow inward: adapter → application → domain.

## Design Principles
SOLID applies to all production code:
- SRP: one reason to change per class. Split domain services by business rule.
- OCP: add behaviour through new types (Strategy, polymorphism), not by growing switch/if chains.
- LSP: subtypes keep their parent's contracts. No UnsupportedOperationException overrides.
- ISP: keep ports in application/port/in and port/out narrow (one use case per in-port).
- DIP: the domain depends only on its own abstractions. Adapters implement ports.

GoF patterns: use one only when the problem actually calls for it, and name it in the class
(e.g. `PricingStrategy`, `NewsItemFactory`). Never add a pattern just in case.
Likely fits here: Strategy (per-media-type rules for text, image, video, etc.),
Composite (NewsML-G2 packages that contain items), Builder (complex items),
Factory Method (creating items by item class), Adapter (already the basis of hexagonal).

## Use Project Lombok
* Do not use Lombok in domain classes. Domain classes should be pure Java, with no framework dependencies.
* Use Lombok to reduce boilerplate code.
* Enable annotation processing for your IDE to generate boilerplate code for you.
* When adding builder to a class, if the class extends another class, add `@SuperBuilder`for the builder.

## Use Mapstruct for Type Conversions
* Do not use Mapstruct in domain classes. Domain classes should be pure Java, with no framework dependencies.
* Use Mapstruct to convert between domain objects and DTOs.
* Use `@Mapper` to configure the mapping between the two classes.
* Use `@Mapping` to configure the mapping between the two fields.
* After modifying a Mapper, recompile the project to generate the new Mapper implementation.
* Use Mappers to update existing entities.
