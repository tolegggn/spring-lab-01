# Laboratory 8 — PostgreSQL and Spring Data JPA (variant 2)

Requires Java 25, Maven (or the included Maven Wrapper), and PostgreSQL 16+.
For a disposable local database, start PostgreSQL with:

```sh
docker run --name pg-springlab -e POSTGRES_DB=springlab \
  -e POSTGRES_USER=springlab -e POSTGRES_PASSWORD=springlab \
  -p 5432:5432 -d postgres:16
```

From the project directory, run `bash ./mvnw test` and `bash ./mvnw spring-boot:run`.
The defaults connect to `jdbc:postgresql://localhost:5432/springlab`; override
them with `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and
`SPRING_DATASOURCE_PASSWORD` as needed.

The default `dev` profile **drops and recreates the schema** and seeds sample
data. Use only a disposable development database. Other profiles validate an
existing schema and do not run the sample-data initializer.

Demonstration requests:

```sh
curl 'http://localhost:8080/api/books?page=1&size=3&sort=year,desc'
curl 'http://localhost:8080/api/books/slice?size=3'
curl 'http://localhost:8080/api/books?sort=nosuchfield'
docker exec pg-springlab psql -U springlab -d springlab -c '\d book' -c '\d book_tag'
docker exec pg-springlab psql -U springlab -d springlab -c 'select * from book_tag;'
```

`BookTag` is the variant 2 association entity, with `book_id`, `tag_id` and
`added_at`. `Lab8PersistenceTest` exercises all required queries, verifies tag
timestamps, and measures N+1 and Page versus Slice with Hibernate statistics.
On the seeded data the measured statement counts are 4 versus 1 for lazy
loading versus fetch join, and 2 versus 1 for Page versus Slice. See
`Lab08_Variant2_Report.docx` for the observed schema, SQL and response.
