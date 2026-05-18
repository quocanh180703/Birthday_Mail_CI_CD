# GitHub Actions CI/CD Setup Guide

## 📋 Overview

Dự án Birthday-Mail đã được cấu hình với GitHub Actions CI/CD pipeline để tự động:
- ✅ Chạy unit tests trên mỗi push/PR
- 🐳 Build Docker images
- 📦 Push images lên Docker Hub
- 🧪 Chạy integration tests trên main branch
- 🔄 Tự động update dependencies

---

## 🔧 Setup Instructions

### 1. GitHub Repository Configuration

#### Bước 1: Push code lên GitHub
```bash
git init
git add .
git commit -m "Initial commit: Birthday-Mail with CI/CD"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/Birthday-Mail.git
git push -u origin main
```

#### Bước 2: Enable GitHub Actions
- Vào **Settings** > **Actions** > **General**
- Chọn "Allow all actions and reusable workflows"
- Lưu settings

---

### 2. Configure GitHub Secrets for Docker Hub

#### Các Secrets cần thêm:

**Settings > Secrets and variables > Actions**

| Secret Name | Giá trị | Mô tả |
|-----------|--------|-------|
| `DOCKER_USERNAME` | Docker Hub username | Username đăng nhập Docker Hub |
| `DOCKER_PASSWORD` | Docker Hub access token | Personal Access Token từ Docker Hub |

#### Lấy Docker Hub Token:
1. Đăng nhập vào [Docker Hub](https://hub.docker.com)
2. Vào **Account Settings** > **Security** > **New Access Token**
3. Chọn "Read & Write" permissions
4. Copy token và paste vào GitHub secret

---

### 3. Workflow Files

#### Main CI/CD Workflow: `.github/workflows/ci-cd.yml`

**Jobs:**

- **test** - Chạy unit tests cho cả 2 services
  - Triggers: mỗi push hoặc PR trên main/develop
  - Services: birthday-reader, birthday-mailer
  - Upload test results as artifacts

- **build** - Build và push Docker images
  - Triggers: sau khi test pass, chỉ khi push (không PR)
  - Push đến: `docker.io/<username>/<service>`
  - Tags: branch name, version tags, latest

- **integration-test** - Chạy integration tests trên main
  - Services: RabbitMQ 3.12, MySQL 8.0
  - Chỉ chạy trên main branch

#### Dependabot Workflow: `.github/workflows/dependabot.yml`

- Tự động auto-merge Dependabot PRs cho dependencies
- Squash commits trước khi merge

---

## 📊 Workflow Triggers

### Trigger Events

```yaml
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]
```

**Khi nào triggers:**
- ✅ Khi push code lên main hoặc develop
- ✅ Khi tạo PR vào main hoặc develop

**Branches:**
- `main` - Production branch (đủ điều kiện chạy integration tests)
- `develop` - Development branch (chỉ chạy unit tests)

---

## 🐳 Docker Hub Integration

### Image Naming Convention

Images sẽ được push với các tags:

```
docker.io/<username>/birthday-reader:main
docker.io/<username>/birthday-reader:v1.0.0
docker.io/<username>/birthday-reader:latest
docker.io/<username>/birthday-reader:main-abc1234
```

### Image Tagging Strategy

| Tag | Khi nào | Ví dụ |
|-----|--------|-------|
| `latest` | Push vào main branch | `birthday-reader:latest` |
| `main` | Push vào main branch | `birthday-reader:main` |
| `develop` | Push vào develop branch | `birthday-reader:develop` |
| `v1.0.0` | Semantic version tag | `birthday-reader:v1.0.0` |
| `main-abc1234` | Commit SHA trên main | `birthday-reader:main-abc1234` |

---

## 📝 Dependabot Configuration

### `.github/dependabot.yml`

Cấu hình tự động kiểm tra và cập nhật dependencies:

**Maven Dependencies:**
- Kiểm tra hàng tuần (thứ 2 lúc 3:00 AM UTC)
- Cho cả 2 services

**Docker Images:**
- Kiểm tra hàng tuần (thứ 2 lúc 4:00 AM UTC)
- Cập nhật base images (temurin, mysql, rabbitmq, etc.)

**GitHub Actions:**
- Kiểm tra hàng tuần (thứ 3 lúc 3:00 AM UTC)
- Cập nhật action versions

---

## 🚀 Usage Examples

### Trigger CI/CD Pipeline

**Khi nào workflow chạy:**

```bash
# 1. Push code -> Triggers test + build (nếu main)
git add .
git commit -m "Add new feature"
git push origin main

# 2. Tạo PR -> Triggers test only
git checkout -b feature/new-feature
git push origin feature/new-feature
# Tạo PR trên GitHub -> test chạy

# 3. Create release tag -> Triggers with version tag
git tag v1.0.0
git push origin v1.0.0
# Images push với tag: birthday-reader:v1.0.0
```

### Check Workflow Status

**Trên GitHub:**
1. Vào **Actions** tab
2. Xem danh sách workflows
3. Click vào workflow run để xem details
4. Xem logs từng step

---

## ✅ Test Artifacts

### Test Results Storage

Mỗi workflow run sẽ upload test results artifacts:

```
test-results-birthday-reader/
  ├── TEST-com.example.reader.service.ExcelServiceTest.xml
  ├── TEST-com.example.reader.service.PublisherServiceTest.xml
  ├── TEST-com.example.reader.scheduler.BirthdaySchedulerTest.xml
  ├── TEST-com.example.reader.controller.UploadControllerTest.xml
  └── TEST-com.example.reader.controller.SchedulerControllerTest.xml

test-results-birthday-mailer/
  ├── TEST-com.example.mailer.service.MailServiceTest.xml
  └── TEST-com.example.mailer.listener.EmployeeListenerTest.xml
```

**Tải artifacts:**
1. Vào workflow run
2. Click **Artifacts** section
3. Download `test-results-<service>.zip`

---

## 🔐 Security Best Practices

### 1. GitHub Secrets
✅ Luôn sử dụng GitHub Secrets cho sensitive data
❌ NEVER commit credentials vào code

### 2. Token Permissions
- Docker Hub token: "Read & Write" only
- GitHub token: Default permissions tự động được set

### 3. Branch Protection Rules
Recommend settings cho main branch:

**Settings > Branches > Branch protection rules**

```
✅ Require a pull request before merging
✅ Require status checks to pass before merging
✅ Require branches to be up to date before merging
✅ Require code reviews before merging (1 review)
✅ Dismiss stale pull request approvals when new commits are pushed
✅ Require conversation resolution before merging
```

---

## 📈 Monitoring & Logs

### View Workflow Logs

**GitHub Actions UI:**
1. **Actions** tab
2. Select workflow (`CI/CD Pipeline`)
3. Click run number
4. Xem step-by-step logs

### Docker Build Logs

Build logs include:
- Maven compile output
- Docker layer caching
- Image push results

### Test Results

Test results tự động được:
- Uploaded làm artifacts
- Displayed trong workflow summary
- Available để download

---

## 🆘 Troubleshooting

### Common Issues & Solutions

#### 1. Authentication Failed to Docker Hub
```
Error: denied: requested access to the resource is denied
```
**Solution:**
- Verify `DOCKER_USERNAME` secret value
- Verify `DOCKER_PASSWORD` is valid Docker Hub token
- Regenerate token nếu cần

#### 2. Build Fails - Cache Issues
```
Error: no cache found...
```
**Solution:**
- First push có thể chậm (build from scratch)
- Subsequent pushes sẽ sử dụng cache
- Safe to ignore on first run

#### 3. Tests Failing in CI
```
Tests pass locally but fail in CI
```
**Solution:**
- Check environment variables khác nhau
- Verify RabbitMQ/MySQL services running
- Check file permissions in Docker context

#### 4. Docker Login Failed
```
Error: write /root/.docker/config.json: permission denied
```
**Solution:**
- Ensure proper Docker buildx setup
- Use `docker/login-action@v2`
- Verify secrets configured correctly

---

## 🔄 Continuous Deployment Options

### Current Setup
- Tests: ✅ Every push/PR
- Build: ✅ Every push to main/develop
- Push: ✅ To Docker Hub

### Optional Enhancements

#### 1. Deploy to Kubernetes
```yaml
- name: Deploy to K8s
  uses: actions-hub/kubectl@master
  with:
    args: apply -f k8s/deployment.yml
```

#### 2. Deploy to Azure Container Instances
```yaml
- name: Deploy to ACI
  uses: azure/aci-deploy@v1
```

#### 3. Slack Notifications
```yaml
- name: Notify Slack
  uses: slackapi/slack-github-action@v1
```

---

## 📚 References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Build Action](https://github.com/docker/build-push-action)
- [Dependabot Documentation](https://docs.github.com/en/code-security/dependabot)
- [Docker Hub API](https://docs.docker.com/docker-hub/api/latest/)

---

## ✅ Checklist

Trước khi sử dụng CI/CD:

- [ ] GitHub repository tạo & push code
- [ ] GitHub Actions enabled
- [ ] Docker Hub account tạo
- [ ] Docker Hub token generated
- [ ] `DOCKER_USERNAME` secret added
- [ ] `DOCKER_PASSWORD` secret added
- [ ] `.github/workflows/ci-cd.yml` trong repo
- [ ] `.github/dependabot.yml` trong repo
- [ ] Test workflow chạy successfully
- [ ] Docker images push thành công

---

**Last Updated:** May 18, 2026
**Status:** ✅ Ready for Production
