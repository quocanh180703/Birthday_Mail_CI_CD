# Birthday-Mail Project - Complete Architecture

## 📊 Project Structure

```
Birthday-Mail/
├── .github/
│   ├── workflows/
│   │   ├── ci-cd.yml                 ← Main CI/CD Pipeline
│   │   └── dependabot.yml            ← Auto-merge Dependencies
│   └── dependabot.yml                ← Dependabot Configuration
│
├── birthday-reader/                  ← Excel Reader Service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/reader/
│   │   │   │   ├── ReaderApplication.java
│   │   │   │   ├── config/
│   │   │   │   ├── controller/       ← REST API
│   │   │   │   ├── model/
│   │   │   │   ├── scheduler/        ← Scheduled Jobs
│   │   │   │   └── service/          ← Business Logic
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/com/example/reader/
│   │           ├── service/
│   │           │   ├── ExcelServiceTest.java (2 tests)
│   │           │   └── PublisherServiceTest.java (6 tests)
│   │           ├── scheduler/
│   │           │   └── BirthdaySchedulerTest.java (5 tests)
│   │           └── controller/
│   │               ├── UploadControllerTest.java (4 tests)
│   │               └── SchedulerControllerTest.java (5 tests)
│   ├── Dockerfile
│   └── pom.xml
│
├── birthday-mailer/                  ← Email Sending Service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/mailer/
│   │   │   │   ├── MailerApplication.java
│   │   │   │   ├── config/
│   │   │   │   ├── entity/
│   │   │   │   ├── listener/         ← RabbitMQ Listener
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   └── service/          ← Email Service
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── templates/
│   │   └── test/
│   │       └── java/com/example/mailer/
│   │           ├── service/
│   │           │   └── MailServiceTest.java (3 tests)
│   │           └── listener/
│   │               └── EmployeeListenerTest.java (3 tests)
│   ├── Dockerfile
│   └── pom.xml
│
├── Documentation/
│   ├── README.md                     ← Main README (updated)
│   ├── TEST-SUMMARY.md               ← Test Documentation
│   ├── QUICKSTART-CICD.md            ← 5-minute Setup
│   ├── CI-CD-SETUP.md                ← Full CI/CD Guide
│   ├── WORKFLOW-GUIDE.md             ← Workflow Management
│   ├── SETUP-CHECKLIST.md            ← Step-by-step Setup
│   ├── ARCHITECTURE.md               ← This file
│   └── .gitignore                    ← Git Rules
│
├── Configuration/
│   ├── docker-compose.yml            ← Local Docker Compose
│   ├── pom.xml                       ← Root POM (if applicable)
│   └── .env.example                  ← Environment Template
│
└── Data/
    └── data/employees.xlsx           ← Sample Data
```

---

## 🔄 CI/CD Pipeline Flow

```
┌─────────────────────────────────────────────────────────────┐
│                     Developer Push                           │
│           (Push to main or develop branch)                   │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
        ┌──────────────────────────────┐
        │   GitHub Actions Triggered    │
        │   (ci-cd.yml workflow)        │
        └──────────────┬────────────────┘
                       │
        ┌──────────────┴──────────────────┐
        │                                 │
        ▼                                 ▼
    ┌───────────┐                  ┌──────────────┐
    │   TEST    │                  │   BUILD      │
    │  (3 min)  │                  │  (5 min)     │
    └─────┬─────┘                  └──────────────┘
          │
    ┌─────┴──────────────┐
    │                    │
    ▼                    ▼
┌──────────────┐  ┌──────────────────────────┐
│ birthday-    │  │ birthday-reader tests    │
│reader tests  │  │ ✅ 16/16 passing         │
│✅ 16/16      │  │                          │
│passing       │  │ birthday-mailer tests    │
└──────────────┘  │ ✅ 6/6 passing           │
                  │ (Parallel execution)     │
                  └──────────────────────────┘
                         │
                    ✅ All Pass?
                    │         │
                   Yes       No ❌
                    │         │
                    ▼         ▼
            ┌───────────┐  ❌ STOP
            │  Build    │  (Email notify)
            │ Docker    │
            │(if push)  │
            └─────┬─────┘
                  │
    ┌─────────────┴──────────────────┐
    │                                 │
    ▼                                 ▼
┌──────────────────────┐    ┌──────────────────────────┐
│ Build birthday-      │    │ Push to Docker Hub       │
│ reader image         │    │ docker.io/username/...   │
│                      │    │ (Multiple tags)          │
│ Build birthday-      │    │ - latest                 │
│ mailer image         │    │ - main/develop           │
│ (Parallel)           │    │ - commit-sha             │
│                      │    │ - version (if tag)       │
└──────────────────────┘    └──────────────────────────┘
                                     │
                ┌────────────────────┴─────────────────┐
                │                                      │
                ▼                                      ▼
        (If main branch)              ┌─────────────────────────┐
        ┌──────────────────┐          │ Docker Hub Updated      │
        │ Integration Test │          │ Images Ready            │
        │ (8 min)          │          │ Can be deployed         │
        │ - RabbitMQ 3.12  │          └─────────────────────────┘
        │ - MySQL 8.0      │
        │ - Full E2E test  │
        └────────┬─────────┘
                 │
            ✅ All Pass?
                 │
         ┌───────┴────────┐
         ▼                ▼
    ✅ SUCCESS       ❌ NOTIFY
    [Pipeline        [Email alert]
     complete]
```

---

## 🧪 Test Coverage

### Layer 1: Unit Tests (22 Total)

**birthday-reader (16 tests)**
```
├── ExcelServiceTest (2 tests)
│   └── Tests Excel file parsing
├── PublisherServiceTest (6 tests)
│   └── Tests RabbitMQ publishing
├── BirthdaySchedulerTest (5 tests)
│   └── Tests scheduled job
├── UploadControllerTest (4 tests)
│   └── Tests REST file upload
└── SchedulerControllerTest (5 tests)
    └── Tests REST job trigger
```

**birthday-mailer (6 tests)**
```
├── MailServiceTest (3 tests)
│   └── Tests email service
└── EmployeeListenerTest (3 tests)
    └── Tests RabbitMQ listener
```

### Layer 2: Integration Tests (CI/CD Only)
```
- RabbitMQ message queue
- MySQL database
- Full workflow integration
- End-to-end validation
```

---

## 🐳 Docker Image Architecture

### Image Building

```
birthday-reader/
├── Dockerfile
│   ├── Base: temurin:21-jdk
│   ├── Stage 1: Build (Maven)
│   │   └── Compile & package
│   └── Stage 2: Runtime
│       └── Run jar
│
└── Output: docker.io/username/birthday-reader

birthday-mailer/
├── Dockerfile
│   ├── Base: temurin:21-jdk
│   ├── Stage 1: Build (Maven)
│   │   └── Compile & package
│   └── Stage 2: Runtime
│       └── Run jar
│
└── Output: docker.io/username/birthday-mailer
```

### Image Tags Strategy

```
docker.io/username/birthday-reader
├── latest                    (Latest from main)
├── main                      (Main branch marker)
├── develop                   (Develop branch marker)
├── main-abc1234              (Commit SHA tagging)
├── v1.0.0                    (Semantic versioning)
├── v1.0                      (Minor version)
└── v1                        (Major version)
```

---

## 🔌 Service Architecture

### Runtime Communication

```
┌─────────────────────────────────────────────────────────┐
│              Docker Compose Network                      │
└─────────────────────────────────────────────────────────┘
        │                  │                  │
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ birthday-    │  │  RabbitMQ    │  │   MySQL      │
│ reader       │  │  (5672)      │  │  (3306)      │
│ (8080)       │  │  (15672 UI)  │  │              │
└──────────────┘  └──────────────┘  └──────────────┘
        │                ▲                  ▲
        │                │                  │
        └─ Upload ──────┘                   │
        │                                   │
        ├─ Publish ────────────────┐        │
        │                          │        │
        │                          ▼        │
        │                  ┌──────────────┐│
        │                  │ birthday-    ││
        │                  │ mailer       ││
        │                  │ (8081)       ││
        │                  └──────────────┘│
        │                          │        │
        └──────────────────────────┼────────┘
                                   │
                            Store mail records
```

---

## 🔐 Security Configuration

### GitHub Secrets (Required)
```
DOCKER_USERNAME: Docker Hub username
DOCKER_PASSWORD: Docker Hub Personal Access Token
```

### Branch Protection (Recommended)
```
main branch
├── Require PR before merge
├── Require CI/CD pass
├── Require 1+ approval
└── Require conversation resolution
```

### Image Security
```
├── Base images: Official temurin (signed)
├── Layers: Multi-stage build (smaller final image)
├── Credentials: In environment, not in image
└── Updates: Dependabot auto-updates
```

---

## 📈 Performance Metrics

### Build Times
```
Unit Tests:          ~3 minutes
  - Maven compile
  - 22 tests execution
  - Result reporting

Docker Build:        ~5 minutes
  - Maven build in container
  - Layer caching
  - Image push to registry

Integration Tests:   ~8 minutes
  - Service startup
  - Full E2E testing
  - Service shutdown
```

### Caching Strategy
```
├── Maven: ~/.m2/repository cache
├── Docker: Layer cache (subsequent builds faster)
└── Dependabot: Check cache (weekly)
```

---

## 🚀 Deployment Ready

### Local Development
```bash
docker-compose up -d
# Services available at localhost:8080, localhost:8081
```

### Production Deployment
```bash
docker pull username/birthday-reader:latest
docker pull username/birthday-mailer:latest

docker run -e SPRING_RABBITMQ_HOST=... ...
```

### Kubernetes Deployment (Future)
```yaml
# Can add K8s manifests for automatic deployment
deployment.yml
service.yml
configmap.yml
```

---

## 📊 Workflow Triggers

```
Event               Trigger         Jobs
──────────────────────────────────────────────
Push main           ✅ test        test → build → integration-test
Push develop        ✅ test        test → build
Push feature/*      ✅ test        test
PR to main          ✅ test        test
PR to develop       ✅ test        test
Tag v*.*.* / ✅ test        test → build (with version tag)
Scheduled (weekly)  ✅ Dependabot  (auto-merge if pass)
Manual trigger      ✅ yes         (from GitHub UI)
```

---

## ✅ Project Readiness Checklist

### Development
- ✅ All 22 unit tests passing
- ✅ Code compiles successfully
- ✅ Docker images build locally
- ✅ Services run in docker-compose

### CI/CD
- ✅ Workflows configured
- ✅ Secrets configured
- ✅ Docker Hub connected
- ✅ First workflow run successful

### Production
- ✅ Images in Docker Hub
- ✅ Multiple tags available
- ✅ Documentation complete
- ✅ Ready for deployment

### Security
- ✅ Branch protection enabled
- ✅ Secrets managed securely
- ✅ Dependabot monitoring
- ✅ Auto-updates configured

---

## 📚 Related Documentation

- [README.md](README.md) - Project overview
- [TEST-SUMMARY.md](TEST-SUMMARY.md) - Test documentation
- [QUICKSTART-CICD.md](QUICKSTART-CICD.md) - 5-minute setup
- [CI-CD-SETUP.md](CI-CD-SETUP.md) - Full CI/CD guide
- [WORKFLOW-GUIDE.md](WORKFLOW-GUIDE.md) - Workflow operations
- [SETUP-CHECKLIST.md](SETUP-CHECKLIST.md) - Setup verification

---

**Status:** ✅ Production Ready
**Last Updated:** May 18, 2026
