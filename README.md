# Birthday Greeting System — Demo

Two independent Spring Boot services communicating via RabbitMQ:

- `birthday-reader` — read an Excel file (Apache POI), convert rows to JSON and publish to RabbitMQ queue `birthday.employees`. Supports REST upload and scheduled daily run at 8AM.
- `birthday-mailer` — consumes messages from RabbitMQ, filters employees whose birthday is today, and sends greeting emails via Gmail SMTP (supports inline image attachments).

## Quick start

1. Configure environment (override application.properties or set env vars):

  - RabbitMQ: host/port/username/password
  - Gmail SMTP: `spring.mail.username` and `spring.mail.password` (use an App Password)

2. Build both services with Maven:

```bash
mvn -f birthday-reader/pom.xml package
mvn -f birthday-mailer/pom.xml package
```

3. Run RabbitMQ locally (or use hosted) and start both apps:

```bash
docker-compose up -d --build

java -jar birthday-reader/target/birthday-reader-0.0.1-SNAPSHOT.jar
java -jar birthday-mailer/target/birthday-mailer-0.0.1-SNAPSHOT.jar
```

4. To immediately publish employees, POST an `.xlsx` to the reader:

```bash
curl -F "file=@employees.xlsx" http://localhost:8080/api/upload
```

## Profiles

- `dev`: use for local Docker Compose or local development with the built-in service names (`rabbitmq`, `mysql`) and fixed ports (`8080`, `8081`).
- `uat`: use for UAT/staging by supplying environment variables for RabbitMQ, SMTP, and MySQL.

Example run commands:

```bash
java -jar birthday-reader/target/birthday-reader-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
java -jar birthday-mailer/target/birthday-mailer-0.0.1-SNAPSHOT.jar --spring.profiles.active=uat
```

Example UAT environment variables for mailer:

```bash
SPRING_PROFILES_ACTIVE=uat
SPRING_RABBITMQ_HOST=your-rabbitmq-host
SPRING_MAIL_HOST=smtp.your-company.com
SPRING_DATASOURCE_URL=jdbc:mysql://your-mysql-host:3306/mail_record
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
```

## Testing

This project includes comprehensive unit tests (22 tests total) for professional capstone/thesis quality:

### Run all tests:
```bash
mvn clean test
```

### Run tests for specific service:
```bash
mvn -f birthday-reader/pom.xml clean test
mvn -f birthday-mailer/pom.xml clean test
```

### Test Coverage:
- **birthday-reader:** 16 tests covering Excel service, RabbitMQ publisher, scheduler, and REST controllers
- **birthday-mailer:** 6 tests covering email service and RabbitMQ listener
- **Frameworks:** JUnit 5, Mockito, AssertJ, Spring Boot Test

See [TEST-SUMMARY.md](TEST-SUMMARY.md) for detailed test documentation.
## CI/CD Pipeline

Automated GitHub Actions CI/CD pipeline for continuous integration and deployment:

### ⚙️ What's Automated:
- ✅ **Unit Tests** - Runs on every push/PR (all 22 tests)
- 🐳 **Docker Build** - Builds images for both services
- 📦 **Docker Push** - Pushes to Docker Hub registry
- 🧪 **Integration Tests** - Runs on main branch with RabbitMQ & MySQL
- 🔄 **Dependency Updates** - Auto-updates Maven, Docker, GitHub Actions

### 🚀 Quick Setup:
1. Push code to GitHub
2. Add Docker Hub secrets: `DOCKER_USERNAME`, `DOCKER_PASSWORD`
3. Workflow runs automatically!

See [QUICKSTART-CICD.md](QUICKSTART-CICD.md) for 5-minute setup guide.
See [CI-CD-SETUP.md](CI-CD-SETUP.md) for complete documentation.

## Excel template (first sheet):

Header row, then rows:
- Name | Email | DOB (yyyy-MM-dd or Excel date) | ImagePath (optional local path)

## Notes / next steps

- Consider adding retries, persistent send status, and HTML templating via Thymeleaf (starter included).
- Scheduler in reader triggers at 8:00 local time reading `data/employees.xlsx`.
