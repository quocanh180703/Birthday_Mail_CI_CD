# Quick Start: GitHub Actions CI/CD

## 🚀 5 Minutes Setup

### Step 1: Create GitHub Repository
```bash
# 1. Tạo repo trên GitHub (https://github.com/new)
# 2. Đặt tên: Birthday-Mail
# 3. Chọn Public hoặc Private

# 4. Push code từ local
git init
git add .
git commit -m "Initial commit: Birthday-Mail"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/Birthday-Mail.git
git push -u origin main
```

### Step 2: Add Docker Hub Secrets
1. GitHub > **Settings** > **Secrets and variables** > **Actions**
2. Click **New repository secret**

**Secret 1: DOCKER_USERNAME**
- Value: Your Docker Hub username

**Secret 2: DOCKER_PASSWORD**
- Value: Docker Hub Personal Access Token
- [Get Token](https://hub.docker.com/settings/security) → New Access Token → Read & Write

### Step 3: Verify Workflow
1. GitHub > **Actions** tab
2. Watch "CI/CD Pipeline" run
3. Wait for ✅ All jobs pass

### Done! 🎉
Your pipeline is now live. Every push triggers:
- ✅ Unit Tests (all 22 tests)
- 🐳 Docker Build & Push
- 🧪 Integration Tests (on main only)

---

## 📝 What Each Job Does

| Job | Trigger | Duration | Output |
|-----|---------|----------|--------|
| **test** | Every push/PR | ~3 min | Test artifacts |
| **build** | After test pass | ~5 min | Docker images |
| **integration-test** | Main branch only | ~8 min | Full system test |

---

## 🔍 Monitor Runs

```bash
# View all workflows
GitHub > Actions tab

# View specific run
Click on run number > See job logs

# Download test results
Artifacts section > Download ZIP
```

---

## 🐳 Docker Images Location

After first successful build:
```
docker.io/YOUR_USERNAME/birthday-reader:latest
docker.io/YOUR_USERNAME/birthday-mailer:latest
```

Pull locally:
```bash
docker pull YOUR_USERNAME/birthday-reader:latest
docker pull YOUR_USERNAME/birthday-mailer:latest
```

---

## 📋 Checklist

- [ ] Repository created on GitHub
- [ ] Code pushed to main branch
- [ ] Actions tab shows workflow
- [ ] Docker Hub secrets added
- [ ] First workflow run completed
- [ ] Docker images in Docker Hub
- [ ] Tests all passing ✅

---

## 🆘 Quick Troubleshooting

**Workflow not running?**
- Check Settings > Actions > "Allow all actions" enabled

**Docker push failed?**
- Verify DOCKER_USERNAME & DOCKER_PASSWORD secrets
- Regenerate Docker Hub token

**Tests failing?**
- Check logs in workflow run
- Verify local tests pass: `mvn clean test`

**Need help?**
- See [CI-CD-SETUP.md](CI-CD-SETUP.md) for full guide
- Check [GitHub Actions docs](https://docs.github.com/en/actions)

---

**Status:** ✅ Ready to use
