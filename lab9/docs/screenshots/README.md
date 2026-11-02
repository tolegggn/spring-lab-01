# Browser screenshots — Laboratory 9

Tolegen Alikhan · 2406-IS · Variant 2

Images were captured with Chromium from an actual running build. Application screenshots use an isolated H2 database so the initial conditions are reproducible. The browser captures show page content, without desktop window decorations or the browser's tab bar.

| Image | What it demonstrates |
| --- | --- |
| [01 — Initial accounts](01-initial-accounts.png) | Live GET response rendered as a table: Alice 1000.00, Bob 500.00, both version 0. |
| [02 — Successful transfer](02-successful-transfer.png) | Browser submits 100.00; actual HTTP 204; balances 900.00/600.00 and versions 1. |
| [03 — Rejected transfer](03-rejected-transfer.png) | Browser submits 99999.00; actual HTTP 409; balances remain 900.00/600.00. This is insufficient-funds rejection; the separate flushed-debit rollback experiment appears in image 05. |
| [04 — H2 SQL console](04-h2-console.png) | Actual SQL results: three successful Flyway versions and persisted account values. |
| [05 — Transaction test results](05-transaction-test-results.png) | Browser-rendered excerpts from `../verification.log`: rollback after flushed debit, propagation, isolation, optimistic-lock conflict and 10 passing tests. This is recorded log evidence, not a live terminal. |
| [06 — Migration restarts](06-migration-restarts.png) | Browser-rendered original startup logs: two migrations initially, zero on restart, one V3 upgrade. |

## Demonstrate locally

Build from the repository root:

```powershell
.\mvnw.cmd -f lab9/pom.xml clean verify
cd lab9
java -jar target/lab9-1.0.0.jar
```

Open `http://localhost:8089/demo.html` in your browser. Submit 100.00, then 99999.00, and inspect the status, balances, version and request history. Existing persistent data is not reset on refresh or restart.

For a separate, temporary database and the SQL console, stop that application and run:

```powershell
java -jar target/lab9-1.0.0.jar "--spring.datasource.url=jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1" --spring.h2.console.enabled=true
```

Open `/h2-console` on the same server. Connect using JDBC URL `jdbc:h2:mem:demo`, user `sa`, blank password. Run:

```sql
SELECT "version", "description", "success"
FROM "flyway_schema_history"
WHERE "version" IS NOT NULL
ORDER BY "installed_rank";

SELECT id, owner_name, balance, version, status
FROM accounts ORDER BY id;
```

The captured session used port 18090 and database `jdbc:h2:mem:screenshots`; normal application defaults remain port 8089 and a persistent file database. H2 Console is enabled only by the demonstration command above.
