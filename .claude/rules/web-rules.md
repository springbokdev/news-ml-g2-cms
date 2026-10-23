---
paths:
  - "src/main/java/**/adapter/in/web/**"
---

You are editing web adapter code.

- Controllers are THIN — delegate to application services immediately.
- NEVER put business logic in controllers.
- Constructor injection only — no field @Autowired.
- Use DTOs (records), not domain types, over HTTP.
- @Valid on all request bodies.
- HTTP status codes such as 201 create, 200 query, 400 validation, 404 not found.
- Handle unexpected server errors with HTTP 500 and a client-safe error response.
- Never expose stack traces, SQL details, or internal implementation details in API error responses.
- REST controller endpoints MUST return `ResponseEntity<T>` rather than returning response DTOs or domain objects directly.
- Map domain exceptions to HTTP status HERE — never swallow exceptions or leak infrastructure details.
- Test with @WebMvcTest (one controller).
- Centralize REST API exception handling using `@RestControllerAdvice` and `@ExceptionHandler`.
