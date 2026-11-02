# Laboratory 9: Transaction Management and Schema Versioning

**Tolegen Alikhan · 2406-IS · Variant 2**

The supplied assignment has no variant-specific task table. This solution implements all mandatory tasks A–E and uses variant 2 as the submission identifier.

This standalone Maven project uses Spring Boot 3.5.7, Java 21, Spring Data JPA, H2 and Flyway. It does not change the earlier labs in the repository root.

## Build and test

From the repository root, with Java 21 or later:

```sh
bash mvnw -f lab9/pom.xml verify
```

In the prepared Codex cloud environment:

```sh
export JAVA_HOME=/workspace/.tools/java25
export MAVEN_USER_HOME=/workspace/.cache/maven
export MAVEN_OPTS=-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts
bash mvnw -f lab9/pom.xml -s /workspace/.setup/maven-settings.xml \
  -Dmaven.repo.local=/workspace/.cache/maven/repository verify
```

## Run

```sh
cd lab9
java -jar target/lab9-1.0.0.jar
```

The application listens on port 8089. H2 persists to `lab9/data/txlab` when started from this directory. Restart from the same directory to reuse the database. No external database or credentials are required. The blank H2 password is for this local teaching example.

```sh
curl http://localhost:8089/api/accounts
curl -i -X POST http://localhost:8089/api/transfers \
  -H 'Content-Type: application/json' \
  -d '{"fromId":1,"toId":2,"amount":100.00}'
curl http://localhost:8089/api/accounts
```

A successful transfer returns HTTP 204. Invalid amounts or identical IDs return 400; unknown accounts return 404; insufficient funds and optimistic-lock conflicts return 409. Repeat transfers intentionally change the persistent balances. The tests use separate databases and reset fixtures; they do not depend on application data.

## Experiments and evidence

- `TransactionTests`: successful transfer, insufficient funds, rollback after flushed debit, invalid input, REQUIRED vs REQUIRES_NEW, READ_COMMITTED vs REPEATABLE_READ, and stale JPA update rejection.
- `MigrationTests`: empty file database → V1/V2; reconstructed migrator → zero reapplications; V3 upgrade → one new migration, with schema history and index assertions.
- SQL migrations are in `src/main/resources/db/migration`.
- The English report and captured verification logs are in `docs/`.

The deliberate failure service method is a test experiment; it is not exposed as a public HTTP endpoint. JDBC reads in the isolation experiment avoid confusing JPA's first-level cache with database isolation. Separate proxied services make propagation effective.
