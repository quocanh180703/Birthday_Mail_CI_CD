# GitHub Actions - Workflow Management

## 📊 Available Workflows

### 1. CI/CD Pipeline (`.github/workflows/ci-cd.yml`)

**Kích hoạt bởi:**
- ✅ Push vào `main` hoặc `develop` branch
- ✅ Pull requests vào `main` hoặc `develop` branch

**Jobs:**
1. **Test** - Chạy unit tests
   - Runs: Mỗi push/PR
   - Duration: ~3 phút
   - Artifacts: Test reports

2. **Build** - Build Docker images
   - Runs: Sau test pass + push (không PR)
   - Duration: ~5 phút
   - Output: Docker images pushed to Docker Hub

3. **Integration-Test** - Full integration tests
   - Runs: Chỉ khi push vào `main` branch
   - Services: RabbitMQ 3.12, MySQL 8.0
   - Duration: ~8 phút

### 2. Dependabot Auto-Merge (`.github/workflows/dependabot.yml`)

**Kích hoạt bởi:**
- ✅ Dependabot PRs

**Jobs:**
- Auto-merge dependency updates
- Squash & merge commits

---

## 🎯 Trigger Workflows Manually

### Via Git Commands

```bash
# 1. Push to main (triggers full pipeline)
git add .
git commit -m "Feature: add something"
git push origin main

# 2. Push to develop (triggers test only)
git push origin develop

# 3. Create PR (triggers test only)
git checkout -b feature/something
git push origin feature/something
# Create PR on GitHub

# 4. Create release tag (triggers with version tag)
git tag v1.0.0
git push origin v1.0.0
```

### Via GitHub Web UI

1. Go to **Actions** tab
2. Select workflow
3. Click **Run workflow**
4. Choose branch & click green button

---

## 📈 Monitor Workflow Progress

### Real-time Status

1. **GitHub Actions Dashboard**
   - Go to **Actions** tab
   - See all workflow runs
   - Green ✅ = Success
   - Red ❌ = Failed
   - Yellow 🟡 = In progress

2. **Detailed Logs**
   - Click on workflow run
   - Click on job name
   - Click on step to expand logs
   - Search logs with Ctrl+F

### Workflow Badge

Add to README.md:
```markdown
[![CI/CD Pipeline](https://github.com/YOUR_USERNAME/Birthday-Mail/actions/workflows/ci-cd.yml/badge.svg?branch=main)](https://github.com/YOUR_USERNAME/Birthday-Mail/actions)
```

---

## 🐳 Docker Image Outputs

### After Successful Build

Images automatically pushed to:
```
docker.io/<username>/birthday-reader
docker.io/<username>/birthday-mailer
```

### Available Tags

```bash
# Latest version from main
docker pull <username>/birthday-reader:latest
docker pull <username>/birthday-mailer:latest

# Branch-specific
docker pull <username>/birthday-reader:main
docker pull <username>/birthday-reader:develop

# Commit-based
docker pull <username>/birthday-reader:main-abc1234
docker pull <username>/birthday-reader:develop-def5678

# Semantic versioning
docker pull <username>/birthday-reader:v1.0.0
docker pull <username>/birthday-reader:v1.0
docker pull <username>/birthday-reader:v1
```

---

## 📊 Job Matrix Strategy

### Services Matrix

Workflows run tests/build cho cả 2 services simultaneously:

```yaml
strategy:
  matrix:
    service: [ birthday-reader, birthday-mailer ]
```

**Lợi ích:**
- Parallel execution (faster)
- Independent failure detection
- Separate artifacts for each service

---

## 🔍 Viewing Test Results

### Download Test Reports

1. **Workflow Run Page**
   - Scroll to **Artifacts** section
   - Download `test-results-<service>.zip`

2. **Unzip & View**
   ```bash
   unzip test-results-birthday-reader.zip
   # Opens XML files with test details
   ```

3. **Test Report Format**
   ```
   TEST-<package>.<ClassName>.xml
   ```

### Parse Test XML

Each test generates:
- ✅ Passed tests
- ❌ Failed tests
- ⏱️ Execution time
- Stack traces for failures

---

## 🚨 Failure Handling

### Test Failures

If tests fail:
1. Workflow stops at **test** job
2. **Build** job skipped
3. Check artifacts for test reports

### Build Failures

If Docker build fails:
1. Workflow stops at **build** job
2. No images pushed
3. Check build logs for errors

### Recovery Steps

```bash
# 1. Fix issue locally
# 2. Run tests locally
mvn clean test

# 3. Push fix
git add .
git commit -m "Fix: issue description"
git push origin main

# 4. Workflow automatically retriggers
```

---

## ⚙️ Environment Variables

### Set Workflow Secrets

```yaml
env:
  REGISTRY: docker.io
  DOCKER_USERNAME: ${{ secrets.DOCKER_USERNAME }}
```

### Use in Steps

```yaml
- name: Login to Docker Hub
  with:
    username: ${{ env.DOCKER_USERNAME }}
    password: ${{ secrets.DOCKER_PASSWORD }}
```

---

## 🔄 Dependabot Configuration

### Automatic Dependency Checks

Configured in `.github/dependabot.yml`:

**Maven Dependencies:**
- Check: Weekly (Monday 3:00 AM UTC)
- Apply to: Both services

**Docker Images:**
- Check: Weekly (Monday 4:00 AM UTC)
- Apply to: Base images, Dockerfiles

**GitHub Actions:**
- Check: Weekly (Tuesday 3:00 AM UTC)
- Apply to: Action versions

### Review PRs

1. Dependabot creates PR automatically
2. Review changes
3. Workflow auto-merges if tests pass

---

## 📋 Workflow Files Reference

### Main Files

| File | Purpose |
|------|---------|
| `.github/workflows/ci-cd.yml` | Main CI/CD pipeline |
| `.github/workflows/dependabot.yml` | Auto-merge deps |
| `.github/dependabot.yml` | Dependabot config |

### Configuration Files

| File | Purpose |
|------|---------|
| `birthday-reader/pom.xml` | Maven build config |
| `birthday-mailer/pom.xml` | Maven build config |
| `birthday-reader/Dockerfile` | Docker image def |
| `birthday-mailer/Dockerfile` | Docker image def |

---

## 🆘 Troubleshooting

### Workflow Not Triggering

**Check:**
1. Branch name matches trigger (main/develop)
2. Workflow file in `.github/workflows/`
3. YAML syntax valid (use VS Code extension)
4. Events configured in `on:` section

### Slow Builds

**Common Causes:**
- First build (no cache)
- Large dependencies
- Network issues

**Solutions:**
- Builds cache Docker layers (subsequent faster)
- Pre-warm cache with full build
- Check network bandwidth

### Docker Push Failed

**Causes:**
- Invalid credentials
- Token expired
- Invalid image name

**Fix:**
1. Regenerate Docker Hub token
2. Update DOCKER_PASSWORD secret
3. Verify DOCKER_USERNAME

---

## 📚 Learn More

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Workflow Syntax](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [Docker Build Action](https://github.com/docker/build-push-action)
- [Dependabot Docs](https://docs.github.com/en/code-security/dependabot)

---

**Status:** ✅ Ready to use
**Last Updated:** May 18, 2026
