# Backend Test Report — 10 ตุลาคม 2026

## 1. ข้อมูลการทดสอบ

| รายการ | รายละเอียด |
|---|---|
| Project | Auction-System |
| Automated test command | `.\mvnw.cmd --batch-mode --no-transfer-progress verify` |
| Database | Isolated PostgreSQL 16 container (`auction_test`, localhost:5433) |
| Test frameworks | JUnit 5, Mockito, Spring Boot Test, MockMvc |
| Automated test result | 12 passed, 0 failed, 0 errors, 0 skipped |
| Build result | `BUILD SUCCESS`, exit code 0 |

Tests use generated test accounts and a disposable database. Do not point them at the production Neon database.

## 2. Test Cases and Results

### Application and user/authentication

| Test Case ID | Endpoint | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| BE-001 | Spring Boot startup | Start app context and apply Flyway migrations | Application context starts | `ProjectApplicationTests.contextLoads` passed against PostgreSQL | Pass |
| BE-002 | `POST /api/v1/users` | Register with valid data | HTTP 201; account can be read back | HTTP 201; authenticated `GET /me` returned the registered user | Pass |
| BE-003 | `POST /api/v1/users` | Empty name, invalid email, short password | HTTP 400 with validation error | HTTP 400; response contained `Validation failed` | Pass |
| BE-004 | `POST /api/v1/users` | Register the same email twice | Second request returns HTTP 409 | Duplicate registration returned HTTP 409 | Pass |
| BE-005 | `GET /api/v1/users/me` | No authentication | HTTP 401 | HTTP 401 | Pass |
| BE-006 | `GET /api/v1/users/me` | Valid Basic authentication | HTTP 200 and current user details | HTTP 200; returned email matched the test account | Pass |
| BE-007 | `PUT /api/v1/users/me` | Update name, phone and address | HTTP 200 and updated profile | HTTP 200; updated name returned | Pass |
| BE-008 | `PUT /api/v1/users/me/password` | Change current password | HTTP 204; new password works, old password fails | HTTP 204; new credentials succeeded and old credentials returned HTTP 401 | Pass |

### Seller and artwork

| Test Case ID | Endpoint | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| BE-009 | `POST /api/v1/seller-profiles` | Authenticated user creates seller profile | HTTP 201 | HTTP 201 | Pass |
| BE-010 | `POST /api/v1/artworks` | Seller creates artwork | HTTP 201 and generated ID | HTTP 201; returned artwork ID | Pass |
| BE-011 | `POST /api/v1/artworks` | Unauthenticated request / blank title | HTTP 401 / HTTP 400 | HTTP 401 / HTTP 400 | Pass |
| BE-012 | `GET /api/v1/artworks?page=0&size=10&sort=id,desc` | Paginated and sorted listing | HTTP 200 with paged content | HTTP 200; content array included the created artwork | Pass |
| BE-013 | `GET /api/v1/artworks/{id}` | Read created artwork | HTTP 200 and matching ID | HTTP 200; ID matched | Pass |
| BE-014 | `PUT /api/v1/artworks/{id}` | Owner updates artwork | HTTP 200 with changed details | HTTP 200; updated title returned | Pass |
| BE-015 | `PUT /api/v1/artworks/{id}` | Different user attempts update | HTTP 403 | HTTP 403 | Pass |
| BE-016 | `DELETE /api/v1/artworks/{id}` | Owner deletes artwork without bidding history | HTTP 204; subsequent read returns 404 | HTTP 204; subsequent read returned HTTP 404 | Pass |

### Bidding, bids and comments

| Test Case ID | Endpoint | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| BE-017 | `POST /api/v1/biddings` | Seller creates a bidding with owned artwork and valid dates/prices | HTTP 201 | HTTP 201; bidding was readable afterward | Pass |
| BE-018 | `GET /api/v1/biddings?page=0&size=5&sort=id,asc` | Paginated listing | HTTP 200 with paged content | HTTP 200; content array returned | Pass |
| BE-019 | `GET /api/v1/biddings/{id}` | Read created bidding | HTTP 200 and `ACTIVE` status | HTTP 200; status was `ACTIVE` | Pass |
| BE-020 | `POST /api/v1/biddings/{id}/bids` | No authentication | HTTP 401 | HTTP 401 | Pass |
| BE-021 | `POST /api/v1/biddings/{id}/bids` | Auction owner bids on own auction | HTTP 409 | HTTP 409 | Pass |
| BE-022 | `POST /api/v1/biddings/{id}/bids` | First bid at starting price 100.00 | HTTP 201 | HTTP 201; amount was 100.00 | Pass |
| BE-023 | `POST /api/v1/biddings/{id}/bids` | Next bid at minimum increment 102.50 | HTTP 201 | HTTP 201; amount was 102.50 | Pass |
| BE-024 | `POST /api/v1/biddings/{id}/bids` | Bid of 102.49, below the required increment | HTTP 409 | HTTP 409 | Pass |
| BE-025 | `GET /api/v1/biddings/{id}/bids/highest` | Read highest bid | HTTP 200; amount is 102.50 | HTTP 200; amount was 102.50 | Pass |
| BE-026 | `PUT /api/v1/biddings/{id}` | Owner edits bidding after bids exist | HTTP 409 | HTTP 409 | Pass |
| BE-027 | `POST /api/v1/biddings/{id}/comments` | Authenticated bidder adds comment | HTTP 201 | HTTP 201; message matched input | Pass |
| BE-028 | `POST /api/v1/biddings/{id}/comments/{commentId}/like` | Different user likes comment | HTTP 200; thumbs-up count is 1 | HTTP 200; thumbs-up count was 1 | Pass |
| BE-029 | `PUT /api/v1/biddings/{id}/comments/{commentId}` | Comment author edits comment | HTTP 200 and updated message | HTTP 200; updated message was readable | Pass |
| BE-030 | `DELETE /api/v1/biddings/{id}/comments/{commentId}` | Comment author deletes comment | HTTP 204; list is empty afterward | HTTP 204; subsequent list was empty | Pass |

### Additional service-level bidding rules

| Test Case ID | Scenario | Expected Result | Actual Result | Status |
|---|---|---|---|---|
| BE-031 | Accept a bid at seller-configured increment | Bid accepted and last bid updated | Passed with amount 102.50 | Pass |
| BE-032 | Bid below seller-configured increment | HTTP 409; bid is not saved | HTTP 409; repository save was not called | Pass |
| BE-033 | First bid equals starting price | Bid accepted and last bid updated | Passed with amount 50.00 | Pass |
| BE-034 | Auction owner tries to bid | HTTP 409; bid is not saved | HTTP 409; repository save was not called | Pass |
| BE-035 | Bidding state does not accept bids | HTTP 409; bid is not saved | HTTP 409; repository save was not called | Pass |

### Browser/deployment smoke checks

| Test Case ID | Endpoint | Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| BE-036 | `https://auction-system-68vk.onrender.com/` | Open home page in browser | Auction page renders | “Live auctions” page and listing rendered after Render cold start | Pass |
| BE-037 | `https://auction-system-68vk.onrender.com/swagger-ui/index.html` | Open Swagger UI in browser | Swagger UI renders | Swagger UI page loaded | Pass |

These are browser smoke checks, not automated Playwright end-to-end tests.

## 3. Test Result Summary

| Test group | Tests | Passed | Failed | Skipped |
|---|---:|---:|---:|---:|
| User/authentication integration | 2 | 2 | 0 | 0 |
| Artwork integration | 2 | 2 | 0 | 0 |
| Bidding/comment integration | 2 | 2 | 0 | 0 |
| Spring Boot application context | 1 | 1 | 0 | 0 |
| Bidding service unit tests | 5 | 5 | 0 | 0 |
| **Automated total (`mvn verify`)** | **12** | **12** | **0** | **0** |
| Browser smoke checks (manual browser) | 2 | 2 | 0 | 0 |

## 4. Test Execution

The full verification was run on 10 October 2026 against a fresh, disposable PostgreSQL 16 container using the `auction_test` database on port 5433. The project's existing PostgreSQL data volume was not used.

```powershell
Set-Location .\code
$env:DB_URL="jdbc:postgresql://localhost:5433/auction_test"
$env:DB_USERNAME="auction_test"
$env:DB_PASSWORD="auction_test_local_only"
$env:REMEMBER_ME_KEY="ci-only-remember-me-key"
.\mvnw.cmd --batch-mode --no-transfer-progress verify
```

Final result: `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS`, exit code 0.

Detailed Surefire XML/text results are under `code/target/surefire-reports/`.

## 5. Coverage Limits / Not Tested

This is broad testing of the implemented user, artwork, bidding, and comment journeys—not proof that every feature or every branch in the project is tested. The following still need dedicated tests:

- Payment submission, confirmation, rejection, shipping, and admin payment actions
- Admin user/status/moderation and bid-voiding APIs
- Seller rating and seller-profile update flows
- Bidding cancellation/closing, time-window boundaries, and other invalid date scenarios
- Comment dislike and ownership/permission edge cases
- Full automated browser-based user journey with Playwright
- JaCoCo line/branch coverage (not configured or measured)

## 6. Defect Log

No failures were found in the 12 automated tests or the two browser smoke checks. Untested areas above are coverage gaps, not confirmed defects.

## 7. Conclusion

The complete automated Maven suite passes: 12 tests, 0 failures. The added MockMvc integration tests exercise registration/authentication, profile updates, artwork CRUD and access control, bidding creation and bid rules, and comment create/react/update/read/delete flows against PostgreSQL. The public home page and Swagger UI also rendered during browser smoke checks. Additional payment/admin testing, full Playwright E2E coverage, and code coverage measurement remain outstanding.
