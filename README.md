# Tanulás a tanulásért — *Learning for learning*

A web application that recommends **elective university courses** to informatics
students based on the grades they (and their peers) have already earned. The core of
the app is a hand-written **AdaBoost** classifier (decision stumps + Gini impurity)
that learns which electives tend to be a good fit for a given student.

> The UI is in **Hungarian**. It targets the BSc Computer Science programme at ELTE
> (Eötvös Loránd University), where students follow one of three specialization tracks
> — **A**, **B** or **C** — and must pick elective ("kötelezően választható") subjects
> each semester.

---

## What it does

| Area | Description |
|------|-------------|
| **Accounts** | Register / log in. Each student belongs to a specialization track (A, B, C, or *none yet*). |
| **Grade book** | Students record their grade (1–5) for any subject offered on their track. |
| **Elective recommendation** | For a chosen semester, the AdaBoost engine suggests the single elective that best fits the student, and stores it in their calculation history. |
| **Teacher reputation** | Students up-/down-vote and comment on teachers. The landing page surfaces the best-rated teacher and recent comments. |
| **Suggestions ("Javítási észrevételek")** | Any logged-in user can suggest a new teacher/subject, or dispute whether a teacher currently teaches a subject. |
| **Admin moderation** | Admins approve or delete suggested teachers/subjects and resolve activity disputes. |
| **PWA** | Installable, offline-capable progressive web app (manifest + service worker). |

### How the recommender works

1. Every student is labelled a **"good selector"** if their average grade in elective
   subjects is at least as high as their overall average — i.e. their electives helped
   rather than hurt them.
2. Each elective a student has taken becomes a **training sample**.
3. **AdaBoost** runs up to six rounds. Each round it scores every *candidate* elective
   (the ones the requesting student hasn't taken yet, offered in the chosen semester)
   by the **weighted Gini impurity** of splitting students into good/bad selectors on
   that subject, picks the lowest-impurity subject as that round's **decision stump**,
   converts its error into an *amount of say*, reweights the samples, and draws a fresh
   weighted resample.
4. In the final vote, each student's grade in a stump subject adds (or subtracts) the
   stump's say. The highest-scoring candidate is recommended.

The classifier has two deliberate characteristics worth noting (weights are reset every
round; the round-error term is squared), documented in
[`AdaBoostClassifier`](src/main/java/com/learnforlearning/recommendation/AdaBoostClassifier.java).
They define this recommender's behaviour; switching to a textbook AdaBoost would be a
one-line change in `roundError(...)`.

---

## Tech stack

- **Java 17**, **Spring Boot 3.3** (Spring MVC, Spring Security, Spring Data JPA, Bean Validation)
- **Thymeleaf** server-rendered views (shared layout fragment) + **Bootstrap 5** with a custom theme (Inter / Roboto Slab fonts, brand palette)
- **H2** file database by default (zero setup); **PostgreSQL** for the `prod` profile
- **Gradle** (Kotlin DSL) with the wrapper
- **JUnit 5** for tests

---

## Running it with Docker (recommended)

No JDK or database needed on the host — just Docker:

```bash
docker compose up --build
```

This builds the app image (multi-stage: Gradle build → slim JRE runtime) and starts it
alongside a **PostgreSQL** container. The app waits for the database to be healthy, then
creates the schema and seeds sample data automatically (the `docker` Spring profile).

Open <http://localhost:8080> — default admin login `admin@lfl.hu` / `password`.

```bash
docker compose down        # stop
docker compose down -v     # stop and wipe the database volume
```

## Running it locally (without Docker)

### Prerequisites

- **JDK 17** or newer (e.g. [Eclipse Temurin](https://adoptium.net/temurin/releases/?version=17)).
  Verify with `java -version`.
- No database to install — the default profile uses an embedded H2 file under `./data`.

### Start the app

```bash
# from the project root
./gradlew bootRun           # macOS / Linux
.\gradlew.bat bootRun       # Windows (PowerShell / cmd)
```

Then open <http://localhost:8080>.

On first start a small, deterministic **sample data set** is seeded (subjects,
teachers and students with grades that carry enough signal for the recommender to
work). To start from an empty database, delete the `data/` folder or set
`app.seed-on-startup=false`.

### Useful URLs

- App: <http://localhost:8080>
- H2 console: <http://localhost:8080/h2-console>
  (JDBC URL `jdbc:h2:file:./data/lfl`, user `sa`, empty password)

### Build a runnable jar

```bash
./gradlew clean bootJar
java -jar build/libs/learnforlearning-1.0.0.jar
```

### Run the tests

```bash
./gradlew test
```

This includes deterministic unit tests for the AdaBoost classifier and the Gini math.

### Production / PostgreSQL

```bash
DB_URL=jdbc:postgresql://localhost:5432/learnforlearning \
DB_USERNAME=postgres DB_PASSWORD=secret \
./gradlew bootRun --args='--spring.profiles.active=prod'
```

The `prod` profile disables the H2 console and the seeder, switches Hibernate to
`validate` (bring a migration tool such as Flyway), and turns on Thymeleaf caching.

---

## Project structure

```
src/main/java/com/learnforlearning
├── domain/            JPA entities + the Specialization enum
├── repository/        Spring Data repositories (fetch-join queries avoid N+1)
├── recommendation/    The AdaBoost engine and recommendation service
├── service/           Application services (users, subjects, teachers, admin, stats)
├── security/          Spring Security config, UserDetails adapter
├── web/               Controllers
│   ├── dto/           Validated form objects
│   └── view/          Small read-model records for the templates
└── config/            Security, data seeding
src/main/resources
├── templates/         Thymeleaf views (layout.html skeleton + shared fragments.html)
├── static/            CSS, JS, PWA manifest, service worker, icon
└── application.yml    Default (H2) + prod (PostgreSQL) profiles
```
