# GitHub Actions CI/CD - Complete Setup Checklist

## Phase 1: Initial Repository Setup

### Repository Creation
- [ ] Create GitHub repository (https://github.com/new)
  - [ ] Name: `Birthday-Mail`
  - [ ] Visibility: Public or Private
  - [ ] Initialize with: No template

### Code Push to GitHub
- [ ] Initialize local git repo
  ```bash
  git init
  git add .
  git commit -m "Initial commit: Birthday-Mail with CI/CD"
  git branch -M main
  git remote add origin https://github.com/YOUR_USERNAME/Birthday-Mail.git
  git push -u origin main
  ```
- [ ] Verify code on GitHub

### GitHub Actions Setup
- [ ] Go to Repository **Settings**
- [ ] Click **Actions** > **General**
- [ ] ✅ Enable "Allow all actions and reusable workflows"
- [ ] Save settings

---

## Phase 2: Docker Hub Configuration

### Docker Hub Account
- [ ] Create Docker Hub account (https://hub.docker.com)
- [ ] Username: `YOUR_DOCKER_USERNAME`
- [ ] Email: verified

### Generate Docker Hub Token
- [ ] Login to Docker Hub
- [ ] Go to **Account Settings** > **Security**
- [ ] Click **New Access Token**
- [ ] Name: `github-actions`
- [ ] Permissions: **Read & Write**
- [ ] Generate & copy token
- [ ] Keep token safe (can't view again)

### Add GitHub Secrets
- [ ] Go to GitHub Repository
- [ ] **Settings** > **Secrets and variables** > **Actions**

**Secret 1: DOCKER_USERNAME**
- [ ] Click **New repository secret**
- [ ] Name: `DOCKER_USERNAME`
- [ ] Value: Your Docker Hub username
- [ ] Click **Add secret**

**Secret 2: DOCKER_PASSWORD**
- [ ] Click **New repository secret**
- [ ] Name: `DOCKER_PASSWORD`
- [ ] Value: Docker Hub token (NOT your password)
- [ ] Click **Add secret**

### Verify Secrets
- [ ] Both secrets appear in list
- [ ] Status shows ✅ (masked)
- [ ] Cannot view values (security)

---

## Phase 3: Workflow Configuration

### Check GitHub Files
- [ ] `.github/workflows/ci-cd.yml` exists
  - [ ] Has `jobs: test`, `build`, `integration-test`
  - [ ] YAML syntax valid
  - [ ] Secrets referenced correctly

- [ ] `.github/workflows/dependabot.yml` exists
  - [ ] Configured for auto-merge
  - [ ] Syntax valid

- [ ] `.github/dependabot.yml` exists
  - [ ] Maven, Docker, GitHub Actions configured
  - [ ] Schedule set

### Verify Workflow Files
```bash
# Check YAML syntax
cat .github/workflows/ci-cd.yml | grep -E "^(name|on|jobs):"

# Should show:
# name: CI/CD Pipeline
# on:
# jobs:
```

---

## Phase 4: First Workflow Run

### Trigger First Workflow
- [ ] Push code to main branch
  ```bash
  git add .
  git commit -m "Setup CI/CD workflows"
  git push origin main
  ```

### Monitor Workflow
- [ ] Go to GitHub Repository
- [ ] Click **Actions** tab
- [ ] See "CI/CD Pipeline" workflow running
- [ ] Watch progress bars fill up

### Wait for Completion
- [ ] **test** job: Runs 2-3 minutes
  - [ ] birthday-reader tests: ✅ Pass
  - [ ] birthday-mailer tests: ✅ Pass

- [ ] **build** job: Runs 5 minutes (after test)
  - [ ] Docker login: ✅ Success
  - [ ] Image build: ✅ Success
  - [ ] Image push: ✅ Success

- [ ] **integration-test** job: Runs 8 minutes
  - [ ] Services start: ✅ RabbitMQ, MySQL
  - [ ] Tests run: ✅ Integration tests
  - [ ] Tests pass: ✅ All pass

### Check Results
- [ ] All jobs show ✅ green checkmark
- [ ] No red ❌ failures
- [ ] No yellow 🟡 in-progress (all finished)

---

## Phase 5: Docker Hub Verification

### Check Docker Images
- [ ] Login to Docker Hub
- [ ] Go to **Repositories**
- [ ] See `birthday-reader` repository
  - [ ] Tags show: `latest`, `main`, `main-<commit-sha>`
  - [ ] Image created within last 5 minutes
  
- [ ] See `birthday-mailer` repository
  - [ ] Tags show: `latest`, `main`, `main-<commit-sha>`
  - [ ] Image created within last 5 minutes

### Test Docker Pulls
- [ ] Pull locally to verify
  ```bash
  docker pull YOUR_USERNAME/birthday-reader:latest
  docker pull YOUR_USERNAME/birthday-mailer:latest
  ```
- [ ] Verify images available
  ```bash
  docker images | grep birthday
  ```

---

## Phase 6: Dependabot Setup

### Enable Dependabot
- [ ] GitHub Repository **Settings**
- [ ] **Code security & analysis**
- [ ] ✅ Enable "Dependabot alerts"
- [ ] ✅ Enable "Dependabot security updates"
- [ ] ✅ Enable "Dependabot version updates"

### Configure Dependabot
- [ ] `.github/dependabot.yml` already in repo
- [ ] Configured for Maven, Docker, GitHub Actions
- [ ] First scan runs automatically

### Wait for PRs
- [ ] Check back in a few hours
- [ ] Dependabot creates PRs for updates
- [ ] Workflow auto-runs on PRs
- [ ] Auto-merge enabled (if configured)

---

## Phase 7: Branch Protection Rules (Optional but Recommended)

### Setup Branch Protection
- [ ] Go to Repository **Settings**
- [ ] Click **Branches**
- [ ] Click **Add rule** for `main` branch

### Configure Protection
- [ ] **Pattern:** `main`
- [ ] ✅ **Require status checks to pass**
  - [ ] Select: `test`, `build`
- [ ] ✅ **Require pull request reviews before merging**
  - [ ] Required approvals: 1
- [ ] ✅ **Require branches to be up to date**
- [ ] ✅ **Require conversation resolution before merging**
- [ ] Save changes

### Result
- [ ] Push to `main` requires PR
- [ ] PR requires CI/CD pass + approval
- [ ] Protects production from failures

---

## Phase 8: Testing Your Workflows

### Test Unit Tests
- [ ] Create branch: `git checkout -b test/workflow`
- [ ] Modify code (any change)
- [ ] Commit & push
- [ ] Create PR
- [ ] ✅ Workflow runs (test only)
- [ ] ✅ All tests pass
- [ ] ✅ Shows green ✅ on PR

### Test Full Pipeline
- [ ] Merge PR to main
- [ ] Watch Actions tab
- [ ] ✅ test job runs
- [ ] ✅ build job runs
- [ ] ✅ Docker images created
- [ ] ✅ integration-test job runs
- [ ] ✅ All jobs pass

### Test Failure Handling
- [ ] Create branch: `git checkout -b test/failure`
- [ ] Break a test (modify test to always fail)
- [ ] Commit & push
- [ ] ✅ Workflow runs
- [ ] ✅ test job fails ❌
- [ ] ✅ build job skipped (dependency)
- [ ] Fix test & push again
- [ ] ✅ Workflow passes

---

## Phase 9: Documentation & Team Handoff

### Add Workflow Badge to README
- [ ] Edit README.md
- [ ] Add badge after title:
  ```markdown
  [![CI/CD Pipeline](https://github.com/YOUR_USERNAME/Birthday-Mail/actions/workflows/ci-cd.yml/badge.svg?branch=main)](https://github.com/YOUR_USERNAME/Birthday-Mail/actions)
  ```

### Share Documentation
- [ ] Share [QUICKSTART-CICD.md](QUICKSTART-CICD.md) with team
- [ ] Share [CI-CD-SETUP.md](CI-CD-SETUP.md) for detailed info
- [ ] Share [WORKFLOW-GUIDE.md](WORKFLOW-GUIDE.md) for operations

### Team Access
- [ ] Add team members as Contributors
- [ ] Grant write access if needed
- [ ] Document secrets location for team

---

## Phase 10: Ongoing Maintenance

### Weekly Tasks
- [ ] Check GitHub Actions for failed runs
- [ ] Review Dependabot PRs (if any)
- [ ] Monitor Docker Hub images
- [ ] Check build times & optimize if slow

### Monthly Tasks
- [ ] Review workflow logs for patterns
- [ ] Update action versions if needed
- [ ] Check Docker Hub storage usage
- [ ] Verify Dependabot settings

### Security Tasks
- [ ] Rotate Docker Hub token if needed
- [ ] Review GitHub Secrets access
- [ ] Check branch protection rules
- [ ] Update action to latest versions

---

## ✅ Final Verification Checklist

### Repository
- [ ] Code committed to GitHub
- [ ] `.github/workflows/` directory exists
- [ ] Workflow files have valid YAML syntax
- [ ] All 22 tests pass locally

### Secrets & Auth
- [ ] DOCKER_USERNAME secret added
- [ ] DOCKER_PASSWORD secret added
- [ ] Docker Hub token is valid
- [ ] GitHub token has correct permissions

### Workflows
- [ ] First workflow run completed successfully
- [ ] test job: ✅ All 22 tests pass
- [ ] build job: ✅ Docker images built
- [ ] Docker images pushed to Docker Hub
- [ ] integration-test job: ✅ Services up, tests pass

### Docker Hub
- [ ] `birthday-reader` repository exists
- [ ] `birthday-mailer` repository exists
- [ ] Images have multiple tags
- [ ] Images can be pulled locally

### Documentation
- [ ] README.md updated with CI/CD section
- [ ] QUICKSTART-CICD.md created
- [ ] CI-CD-SETUP.md created
- [ ] WORKFLOW-GUIDE.md created

### Team Ready
- [ ] Team can push code & trigger workflows
- [ ] Team knows where to find logs
- [ ] Team understands failure handling
- [ ] Team can pull Docker images

---

## 🎉 You're Done!

Your Birthday-Mail project now has:
- ✅ Automated testing on every push
- ✅ Automated Docker image building
- ✅ Automated image registry (Docker Hub)
- ✅ Integration tests on main branch
- ✅ Automatic dependency updates
- ✅ Professional CI/CD pipeline

**Next steps:**
1. Continue developing features
2. Push code & let CI/CD handle testing
3. Pull Docker images from Docker Hub
4. Monitor workflows for issues
5. Celebrate automation! 🎉

---

**Completion Date:** _________________
**Status:** ✅ Ready for Production
**Last Updated:** May 18, 2026
