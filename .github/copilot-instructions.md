# Copilot Instructions — Utilities Spend Tracker (UST)

This repository contains the backend for **Utilities Spend Tracker (UST)**.

Backend stack:
- Java + Spring Boot
- Apache Tika for PDF text extraction
- Relational DB (MySQL; H2 may be used for dev)

Primary flow:
PDF upload → extract text → parse bill data → persist → query summaries → export/display

These rules apply to the entire repo, but are **strictly enforced for `/backend`**.

---

## 1) Non-negotiables

- Make the **smallest possible change** that solves the request.
- Do **not** refactor unrelated files.
- Do **not** invent endpoints, database columns, config keys, or dependencies.
- Match existing naming, structure, and style.
- If something is missing or unclear, **infer conservatively and state the assumption**.

---

## 2) Backend structure rules

### Package boundaries (do not violate)

- `bill` package:
  - `BillController`: HTTP layer only
  - `BillService`: business logic and orchestration
  - `BillRepository`: database access only
  - `Bill`, `BillStatus`, `UtilityType`: domain model

- `config` package:
  - Security and application configuration only

### Explicit rules

- Controllers:
  - Validate inputs
  - Call services
  - Return DTOs / responses
  - **No business logic**

- Services:
  - Parse → normalize → persist → summarize
  - Coordinate repositories and parsers
  - **All domain logic lives here**

- Repositories:
  - JPA / DB access only
  - No parsing, no business rules, no formatting

- Parsing logic:
  - Keep Apache Tika extraction and bill parsing **out of controllers and repositories**
  - Prefer a small parsing component/helper (e.g., `BillParser`) rather than bloating services

- Do not expose JPA entities directly from controllers if DTOs are introduced.

---

## 3) UST domain rules (bills)

Bill parsing must be **best-effort**:
- Missing fields must **not** crash the request
- Store what is available, skip what isn’t

At minimum, track:
- `UtilityType` (water / electric / internet / gas)
- service month or billing period
- total amount due (`BigDecimal`, never `double`)
- usage (if present)
- source filename / upload metadata (if available)

Normalize:
- Dates → `LocalDate` or `YearMonth`
- Money → `BigDecimal`

Avoid provider-specific assumptions unless clearly scoped to a parser.

---

## 4) File uploads & parsing

- Accept **PDF only**
- Validate content type and size defensively
- Do not trust client input
- If files are stored locally, use temp storage and clean up safely
- Parsing should be extensible (adding a new utility/provider should not require rewrites)

---

## 5) Database & querying

- Avoid unbounded `findAll()` in controllers
- Use pagination where returning lists
- Prefer DB-level aggregation for charts/summaries
- Do not silently change schema
- Any schema change must be minimal and clearly justified

---

## 6) Error handling

- Use correct HTTP status codes:
  - 400 – validation errors
  - 404 – missing resources
  - 415 – unsupported media type
  - 500 – unexpected failures only

- Errors should be consistent and informative
- Never swallow exceptions
- Never log secrets or credentials

---

## 7) Testing expectations

- Parsing logic: unit tests using extracted text (string fixtures)
- Web layer: controller/service tests should be deterministic
- Tests must not depend on local machine state

---

## 8) AI output expectations

When suggesting changes, always include:
1. Files to edit or create
2. The code changes
3. Why this fits the current backend structure
4. Any assumptions made (keep minimal)

If any rule conflicts with a direct user request, ask for clarification.
