# Auction-System Testing Guide

This guide explains how to repeat the automated tests, how to decide whether a test passed, and which test areas still need attention.

## Current verified status

- Automated suite: **20 passed, 0 failed, 0 errors, 0 skipped**
- Build: **`BUILD SUCCESS`**, exit code `0`
- Database: disposable PostgreSQL 16 test database, not production
- Tested: selected user/authentication flows, CSRF enforcement, `SUPER_ADMIN` promotion of a regular user and regular `ADMIN` denial, admin user listing/disabling, admin auction cancel/close, `ADMIN` and `SUPER_ADMIN` payment listing, `SUPER_ADMIN` web payment screen and payment cancellation, artwork and seller-profile APIs, bids/comments, bidding rules, selected Thymeleaf views, and web sign-up auto-login
- Not fully tested: the buyer/seller payment lifecycle, remaining admin endpoints/transitions, all service branches and UI flows, test coverage percentage, and a repeatable browser E2E suite

The image linked below is a visual summary made from Maven Surefire's XML results; the actual detailed results are in `code/target/surefire-reports/`.

![Automated test results: 20 tests passed](../img/automated-test-results.svg)

## 1. Repeat the automated test suite

Use a disposable local database. Never use the production Neon database for test runs because the integration tests create records.

### Start PostgreSQL with Docker

Start Docker Desktop, then from PowerShell run:

```powershell
docker run --rm -d --name auction-test-db `
  -e POSTGRES_DB=auction_test `
  -e POSTGRES_USER=auction_test `
  -e POSTGRES_PASSWORD=auction_test_local_only `
  -p 5433:5432 postgres:16

docker exec auction-test-db pg_isready -U auction_test -d auction_test
```

Wait until `pg_isready` says the server accepts connections.

### Run the tests

```powershell
Set-Location 'D:\Coding project\Project\Auction-System\code'
$env:DB_URL='jdbc:postgresql://localhost:5433/auction_test'
$env:DB_USERNAME='auction_test'
$env:DB_PASSWORD='auction_test_local_only'
$env:REMEMBER_ME_KEY='ci-only-remember-me-key'
.\mvnw.cmd --batch-mode --no-transfer-progress verify
$testExitCode = $LASTEXITCODE
"Test command exit code: $testExitCode"
```

Stop the disposable database after the test run:

```powershell
docker stop auction-test-db
```

### Decide pass or fail

**PASS** only when all of these are true:

1. The Maven command exit code is `0`.
2. Output ends with `BUILD SUCCESS`.
3. The summary has zero failures, errors, and skipped tests.
4. The test count is consistent with the current Surefire report files.

**FAIL** if the command exit code is non-zero, Maven reports `BUILD FAILURE`, or any test has a failure/error. A partial pass is still a failed run. Read the first failing test and its stack trace under `code/target/surefire-reports/`, fix the cause, and rerun `verify`.

To run only one test class, use the same environment variables and replace the final command with:

```powershell
.\mvnw.cmd --batch-mode -Dtest=PageControllerIntegrationTest test
```

Other available focused classes include `AdminApiIntegrationTest`, `UserApiIntegrationTest`, `ArtworkApiIntegrationTest`, `SellerprofileApiIntegrationTest`, `BiddingApiIntegrationTest`, and `BiddingServiceImplTest`.

### Promote a registered user to admin

1. Register the person as a normal user and have a `SUPER_ADMIN` sign in.
2. Open **Admin → Users** and find the account in the user list.
3. Click **Promote to Admin** next to an account whose role is `USER`.
4. **Pass:** the page reports success and the user's role changes to `ADMIN`. The button is shown only to `SUPER_ADMIN`; regular `ADMIN` accounts cannot promote users.
5. The new admin should sign out and back in for the new role to take effect in their session.

## 2. Manual browser checks

Run these against local or staging data only. Use a fresh test email and a dummy password; do not use a real payment or production user.

### Sign-up automatically logs in

1. Open `/register`.
2. Register a new test account with matching passwords.
3. **Pass:** the app redirects to `/`, shows authenticated navigation (for example, Profile and Log out), and `/profile` opens without a separate login.
4. **Fail:** it returns to the login form, stays anonymous, or the new account is not persisted.

### Normal browser authentication and profile

1. Log out, then log in with the test account.
2. Update the profile name and save.
3. Change password using the correct current password; log out and back in using the new password.
4. **Pass:** success messages/redirects appear, the updated profile persists after reload, the new password works, and the old password no longer works.
5. **Fail:** a successful-looking response is shown but a reload loses the update, or the password behavior does not match the expected results.

### Auction, bid, and comment journey

1. Use a seller test account with a seller profile; create an artwork and an auction with valid future dates.
2. Open the auction as a different buyer account; place a first bid at the starting price and then a higher valid bid.
3. Add a comment and try the like/dislike controls.
4. Reload the detail page and check the persisted auction, highest bid, comment, and reactions.
5. **Pass:** the displayed values survive reload and invalid/under-increment bids are rejected without appearing in bid history.
6. **Fail:** another user's bid is attributed to the wrong account, invalid bids persist, or persisted values differ from the success response.

The corresponding core bid/comment API flows are automated already, but the rendered browser screens are not covered by an automated browser suite.

## 3. Remaining API and business-flow tests

Use local/staging accounts and the API operations documented in Swagger. Follow the request schema/status descriptions in Swagger and `doc/api-spec.md`; record actual values and response codes rather than assuming behavior from this checklist. Every state-changing API request must carry a CSRF token and the same session cookie.

| Priority | Area still needing test coverage | What to exercise | Pass evidence |
|---|---|---|---|
| High | Payments | Buyer submits a payment slip; seller confirms or rejects; seller ships with tracking details; buyer and seller list/detail views | Each allowed transition returns the documented success status; unauthorized roles are denied; reloading shows the new persisted state; invalid/repeated transitions are rejected |
| Medium | Remaining admin payment/user actions | Admin payment detail/filter; enabling a user; non-admin denial for additional admin routes | Allowed actions persist; unauthorized users receive 403 |
| Medium | Auction/admin edge cases | Owner cancellation; repeated/invalid admin cancel/close; resulting bid/payment state | Only authorized actor succeeds; action is visible on subsequent reads; invalid or repeated transition is rejected |
| Medium | Bid moderation | Admin lists bids and voids a test bid with a reason | Non-admin is denied; admin action records reason/actor; bid and highest-bid result are consistent afterward |
| Medium | Seller rating | Winner rates a seller; try invalid score, non-winner, duplicate rating, and missing seller | Valid rating persists; invalid/unauthorized/duplicate requests are rejected according to API contract |
| Medium | Permission and not-found cases | Try another user's profile/artwork/comment/payment and nonexistent IDs | No private data is exposed; expected 403/404 is returned without changing records |
| Medium | Date and value boundaries | Auction starts in future, at start/end boundary, already ended; zero/negative amounts and invalid date order | Boundary behavior matches business rules; rejected actions do not persist |
| Medium | Error handling and validation | Missing required fields, malformed JSON, invalid enum/page/sort inputs | Clear documented 4xx response; no stack trace, secret, or internal SQL details returned |

`AdminApiIntegrationTest` provisions its own unique test-only `SUPER_ADMIN` and `ADMIN` accounts in the disposable test database; no real admin credentials or production account are needed for those automated flows. For manual payment workflows, use test auctions and the app's test data only. Do not transfer real money or upload real bank slips. Never promote users in production for testing.

## 4. CSRF and API client check

CSRF protection is enabled for write requests, including `/api/v1/**`. Swagger loading is not enough to prove a write operation works.

- Browser forms should include their hidden CSRF token automatically.
- An API client must first establish/retain the session cookie, obtain the CSRF token (the local `/register` HTML form contains a hidden `_csrf` input), and send the token in `X-CSRF-TOKEN` with that same cookie on POST/PUT/PATCH/DELETE.
- **Pass:** a valid authenticated write with a token follows its API contract; the same write without a token returns `403` and leaves data unchanged.
- **Fail:** a write without a token changes persisted data.

The tokenless rejection is already covered by an automated regression test. A client-specific Swagger flow is still not verified.

## 5. SQL injection and security testing

The source review found no raw input-built SQL; persistence uses Spring Data JPA derived queries or bound JPQL. An injection-shaped comment string was also accepted as literal text. These checks do not prove that every input path is safe.

If you want additional dynamic testing:

1. Use only a local disposable database or an explicitly authorized staging environment.
2. Run OWASP ZAP's passive/baseline scan first.
3. Only run active scans with permission and disposable data; active scans can create/change records and generate load.
4. Review each alert manually and record the scanner/version, exact URL/role tested, evidence, severity, and whether it reproduced.
5. **Pass:** no confirmed injection, authentication bypass, or unexpected data change; investigate and document every high/medium alert before claiming a clean result.

Do not run SQL injection payloads or automated active scans against production without explicit authorization. Do not use destructive payloads.

## 6. What you should do next

1. Repeat `mvn verify` using the disposable database steps above and save the command output.
2. Manually finish the buyer/seller payment lifecycle using test accounts.
3. Exercise remaining admin endpoints/transitions, seller-rating, permission, and auction-boundary cases in the table above.
4. Test the bidding and account-management screens in a real browser, including failed form submissions.
5. If the course requires a security scan, run OWASP ZAP only on local/staging and add verified findings to the report.
6. Record each manual case as **Pass**, **Fail**, or **Not Run** with date, test data (no secrets), expected result, actual result, and evidence. Do not label unrun cases as passed.

The automated suite is evidence for its listed cases, not proof that every service, endpoint, browser page, or security property has been tested.
