# Auction System To-Do List

**Status checked:** 2026-10-03

The items below describe work to add, fix, or update. Ownership is intentionally not assigned.

1. [✅] Fix the incorrect member branch names and Jinnawat's student-ID/branch mismatch in `README.md`.
2. [ ] Complete the README member table with each member's feature responsibility.
3. [ ] Add full CRUD endpoints for the Artwork and Bidding resources.
4. [ ] Add pagination and sorting to at least one list endpoint.
5. [ ] Add a global REST exception handler and a consistent error-response format for validation errors, missing records, conflicts, and unexpected errors.
6. [ ] Complete Bean Validation for request DTOs and return appropriate HTTP status codes (`200`, `201`, `204`, `400`, `404`, `409`, and `500`).
7. [ ] Add Swagger/OpenAPI dependency and configuration, including API documentation for the endpoints.
8. [ ] Add a database migration with Flyway/Liquibase, or working `schema.sql` and `data.sql` files.
9. [ ] Add the required database indexes and foreign-key constraints; set appropriate cascade and fetch behavior on entity relationships.
10. [ ] Implement the Thymeleaf UI in `code/src/main/resources/templates/` with assets in `static/`: artwork listing/detail, place-bid, registration/sign-in, and profile screens.
11. [ ] Add responsive layouts, accessible controls, navigation, and loading, empty, success, and error states to the UI.
12. [ ] Connect UI actions through the presentation and service layers; do not call repositories directly from controllers or views.
13. [ ] Add JUnit/Mockito service unit tests for successful operations and business-rule failures.
14. [ ] Add API/integration tests for success, validation failure, not-found, conflict, and database relationships/constraints.
15. [ ] Expand the current context-load test into meaningful test coverage, run the full suite, and save a test report.
16. [ ] Add UI and test-result screenshots to the top-level `test/` folder.
17. [ ] Create `doc/diagrams/` and add a Use Case Diagram with descriptions, Domain Model, and Class Diagram showing design patterns.
18. [ ] Add at least three Sequence Diagrams and an Activity Diagram for the main scenarios.
19. [ ] Add the ER Diagram, Component Diagram, Deployment Diagram, and a State Diagram for auction/bidding states.
20. [ ] Add a data dictionary that describes the implemented database schema.
21. [ ] Complete `doc/solid-analysis.md` with file/line references and concise explanations for SRP, OCP, LSP, ISP, and DIP.
22. [ ] Create `doc/design-patterns.md` documenting at least three patterns from one GoF group, the problem each solves, participating classes, and class diagrams.
23. [ ] Add a `Dockerfile` and `docker-compose.yml` for the application and SQL database.
24. [ ] Deploy the application and database to a cloud/server environment; add the public URL to `README.md`.
25. [ ] Update `README.md` with the system summary, Tech Stack, System Architecture, ER Diagram, Installation & Setup, How to Run, API Documentation, How to Run Tests, Deployment URL, and Project Structure.
26. [ ] Update the README structure description so Java tests are listed under `code/src/test/` and the top-level `test/` folder is for screenshots/evidence.
27. [ ] Add presentation slides under `doc/slide/` and required media under `img/`.
28. [ ] Add GitHub Actions workflows for build and test, then deployment automation for the chosen hosting platform.
