# Spring Boot SDD Starter

A starter for building Spring Boot services using a **Spec-Driven Development (SDD)** workflow with Claude Code. It 
bundles a working Spring Boot 4 / Gradle project together with a `.claude/` toolchain — skills, agents, and 
hooks — that turns business rules into specs, specs into acceptance tests, and tests into code, with the test suite enforced automatically on every change.

## Prerequisites

- **Java 25** — download from [Adoptium](https://adoptium.net/) or [SDKMAN](https://sdkman.io/)
- **Maven** — included via Maven Wrapper (`./mvnw`), no separate install needed
- **Docker** (optional) — only needed for PostgreSQL in later sections
- **Claude Code** — install from [claude.ai/claude-code](https://claude.ai/claude-code)

### CLAUDE.md

The main instruction file at the project root. Contains build commands, coding conventions (BigDecimal for money, Java 25 features, constructor injection), the four-step development process (Discover → Accept → TDD → Review), testing standards, and architecture rules.

### Rules (`.claude/rules/`)

Path-scoped rules that activate automatically when Claude edits files in specific directories:

| Rule File | Scope | Key Constraints |
|---|---|---|
| `domain-rules.md` | `src/**/domain/**` | No Spring imports, no JPA, pure Java only |
| `persistence-rules.md` | `src/**/adapter/out/persistence/**` | JPA entities here only, implement outbound ports |
| `test-rules.md` | `src/test/**` | Naming conventions (*Test vs *IT), never recalculate expected values |
| `web-rules.md` | `src/**/adapter/in/web/**` | Thin controllers, DTOs only, @Valid on request bodies |

### Skills (`.claude/skills/`)

Reusable, model-invocable skills for the spec-driven development workflow. Each
lives in its own directory as a `SKILL.md` (with supporting `references/` and
`templates/`) and can be invoked explicitly with `/<name>`:

| Skill | Model | Purpose |
|---|---|---|
| `/discover` | opus | Run Example Mapping to discover rules, examples, and questions from a user story |
| `/accept` | sonnet | Write a failing acceptance test for the next spec rule |
| `/tdd` | sonnet | Run one RED → GREEN → REFACTOR TDD cycle |
| `/review` | opus | Architecture and code quality review of uncommitted changes |

### Hooks (`.claude/settings.json`)

The project includes a PostToolUse hook that automatically runs `mvn test` after every file edit, ensuring Claude never moves forward with broken code.

## Architecture Decisions

**Money handling** — All monetary values use `BigDecimal` with explicit `RoundingMode.DOWN` and scale 2. Never `float`, `double`, or `int` for money.

**Hexagonal architecture** — Domain code is framework-free. Spring and JPA live only in adapters. This makes the domain testable with plain JUnit — no Spring context needed.

**In-memory repositories for tests** — Acceptance tests use in-memory implementations of outbound ports, so they run fast without a database. Repository tests use `@DataJpaTest` with H2 in PostgreSQL compatibility mode.

**Specs as the source of truth** — Feature specifications in `doc/specs/` define the contract. Acceptance tests verify the contract. Production code implements it. When specs change, tests change first.

## Useful Commands

```bash
./mvnw test                              # Unit tests only
./mvnw verify                            # Unit + acceptance tests
./mvnw -Dtest=DemoApplicationTest test # Single test class
./mvnw spring-boot:run                   # Run the app (needs PostgreSQL)
./mvnw clean package                     # Build the JAR
```

### Running locally with PostgreSQL

`spring-boot:run` needs a PostgreSQL instance. The connection is configured in
`src/main/resources/application.yaml` and overridable via `DB_URL` / `DB_USERNAME` /
`DB_PASSWORD`. To start one with Docker:

```bash
docker run --name demo-app-pg -e POSTGRES_DB=demo_app \
  -e POSTGRES_USER=demo_app -e POSTGRES_PASSWORD=demo_app -p 5432:5432 -d postgres:17
```

Tests don't need this — they run against H2 in PostgreSQL compatibility mode.

### With Claude Code

```bash
claude                                   # Start a Claude Code session
/discover "As a customer, I want..."     # Run Example Mapping on a user story
/accept Rule1 @doc/specs/feature.md      # Write an acceptance test for a spec rule
/tdd ClassName#methodName                # Run one TDD cycle
/review                                  # Review uncommitted changes
```