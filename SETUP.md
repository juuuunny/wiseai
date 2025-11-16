# 운영 환경 설정 가이드

## ⚠️ 현재 개발 단계별 설정 가이드

### 🟢 지금 필요한 설정 (develop 브랜치 개발용)

**로컬 개발 환경**:

- [x] Docker Compose 실행 (로컬)
- [x] SonarQube 토큰 환경변수 설정 (로컬 빌드용)

**GitHub Actions (develop 브랜치)**:

- [ ] SonarQube Secrets (선택사항, 권장)
  - `SONAR_HOST_URL`: SonarQube 서버 URL
  - `SONAR_TOKEN`: SonarQube 토큰

**현재 상태**: develop 브랜치 push 시 빌드/테스트/소나 분석만 실행 (배포 없음)

---

### 🟡 나중에 필요한 설정 (main 브랜치 배포용)

**AWS 인프라**:

- [ ] EC2 인스턴스 생성 및 설정
- [ ] RDS MySQL 생성 및 설정

**GitHub Secrets (배포용)**:

- [ ] Docker Hub 설정
- [ ] EC2 배포 설정
- [ ] RDS 데이터베이스 설정

**활성화 시점**: 도메인 구현 완료 후, 최종 배포 전

---

## 📋 필수 설정 체크리스트

### 1. GitHub Secrets 설정 (CI/CD 파이프라인용)

**설정 위치**: GitHub 저장소 → Settings → Secrets and variables → Actions → New repository secret

#### 1-1. SonarQube 설정 (선택사항, 권장)

```
SONAR_HOST_URL
- 값: http://localhost:9000 (로컬 SonarQube) 또는 https://sonarcloud.io (SonarCloud)
- 설명: SonarQube 서버 URL

SONAR_TOKEN
- 값: SonarQube에서 발급한 토큰
- 발급 방법:
  1. SonarQube UI 접속 (http://localhost:9000)
  2. 로그인 (admin / WiseAi1234@@)
  3. My Account → Security → Generate Token
  4. 토큰 복사하여 Secrets에 저장
```

#### 1-2. Docker Hub 설정 (필수 - main 브랜치 배포 시)

```
DOCKERHUB_USERNAME
- 값: Docker Hub 사용자명
- 예: your-dockerhub-username

DOCKERHUB_TOKEN
- 값: Docker Hub Access Token
- 발급 방법:
  1. Docker Hub 로그인
  2. Account Settings → Security → New Access Token
  3. 토큰 생성 후 복사

DOCKER_IMAGE_NAME
- 값: Docker Hub 저장소명
- 예: your-dockerhub-username/assignment
- 형식: <username>/<repository-name>
```

#### 1-3. EC2 배포 설정 (필수 - main 브랜치 배포 시)

```
EC2_HOST
- 값: EC2 인스턴스 Public IP 또는 도메인
- 예: 13.125.123.45 또는 api.yourdomain.com

EC2_USER
- 값: EC2 SSH 사용자명
- 예: ubuntu (Ubuntu AMI) 또는 ec2-user (Amazon Linux)

EC2_SSH_KEY
- 값: EC2 인스턴스 접속용 Private Key (전체 내용)
- 형식: -----BEGIN RSA PRIVATE KEY----- ... -----END RSA PRIVATE KEY-----
- 주의: 줄바꿈 포함하여 전체 복사

SERVER_PORT
- 값: EC2에서 노출할 포트 번호
- 예: 8080
```

#### 1-4. RDS 데이터베이스 설정 (필수 - main 브랜치 배포 시)

```
DB_URL
- 값: RDS 엔드포인트 JDBC URL
- 예: jdbc:mysql://assignment-db.xxxxx.ap-northeast-2.rds.amazonaws.com:3306/assignment?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8&serverTimezone=UTC
- 형식: jdbc:mysql://<rds-endpoint>:3306/<database-name>?<parameters>

DB_USERNAME
- 값: RDS 마스터 사용자명
- 예: admin

DB_PASSWORD
- 값: RDS 마스터 비밀번호
- 예: YourSecurePassword123!
```

---

### 2. AWS 인프라 설정

#### 2-1. EC2 인스턴스 설정

**인스턴스 타입**: t3.micro (프리티어)

**AMI**: Ubuntu 22.04 LTS

**보안 그룹 인바운드 규칙**:

```
Type        Protocol    Port Range    Source
SSH         TCP         22             My IP (또는 0.0.0.0/0)
HTTP        TCP         80             0.0.0.0/0 (선택사항)
Custom TCP  TCP         8080          0.0.0.0/0
```

**초기 설정 (EC2 접속 후)**:

```bash
# Docker 설치
sudo apt-get update
sudo apt-get install -y docker.io docker-compose

# Docker 서비스 시작
sudo systemctl start docker
sudo systemctl enable docker

# 사용자를 docker 그룹에 추가 (sudo 없이 docker 명령 실행)
sudo usermod -aG docker $USER
# 로그아웃 후 재로그인 필요

# 디렉토리 생성
mkdir -p ~/app
cd ~/app
```

#### 2-2. RDS MySQL 설정

**엔진**: MySQL 8.0

**인스턴스 클래스**: db.t3.micro (프리티어)

**스토리지**: 20GB (프리티어 최소)

**데이터베이스 이름**: assignment

**마스터 사용자명**: admin (또는 원하는 이름)

**마스터 비밀번호**: 강력한 비밀번호 설정

**보안 그룹 설정**:

- EC2 보안 그룹의 인바운드 규칙에 RDS 보안 그룹 추가
- 또는 RDS 보안 그룹에 EC2 보안 그룹을 소스로 하는 인바운드 규칙 추가

**연결 정보 확인**:

- RDS 콘솔 → 데이터베이스 → 엔드포인트 확인
- 예: `assignment-db.xxxxx.ap-northeast-2.rds.amazonaws.com:3306`

---

### 3. 로컬 개발 환경 설정

#### 3-1. SonarQube 토큰 (로컬 빌드용)

**터미널 환경변수 설정**:

```bash
export SONAR_HOST_URL=http://localhost:9000
export SONAR_TOKEN=squ_5cf73283dee57d0806e17c2e2883ed2dc8c77b84
```

**영구 설정 (선택사항)**:

```bash
# ~/.zshrc 또는 ~/.bashrc에 추가
echo 'export SONAR_HOST_URL=http://localhost:9000' >> ~/.zshrc
echo 'export SONAR_TOKEN=squ_5cf73283dee57d0806e17c2e2883ed2dc8c77b84' >> ~/.zshrc
source ~/.zshrc
```

#### 3-2. Docker Compose 실행

**전체 스택 기동**:

```bash
./gradlew bootJar -x test
docker-compose up --build
```

**확인 URL**:

- Swagger: http://localhost:8081/docs
- Health: http://localhost:8081/actuator/health
- SonarQube: http://localhost:9000
- WireMock: http://localhost:8089/\_\_admin/health

---

### 4. 설정 검증 방법

#### 4-1. GitHub Secrets 검증

**로컬에서 테스트**:

```bash
# Secrets 없이도 빌드는 가능 (SonarQube만 제외)
./gradlew clean build
```

**GitHub Actions에서 확인**:

1. GitHub 저장소 → Actions 탭
2. 최근 워크플로우 실행 확인
3. 실패 시 로그에서 누락된 Secret 확인

#### 4-2. EC2 연결 검증

**SSH 접속 테스트**:

```bash
ssh -i ~/.ssh/your-key.pem ubuntu@<EC2_PUBLIC_IP>
```

**Docker 설치 확인**:

```bash
docker --version
docker compose version
```

#### 4-3. RDS 연결 검증

**EC2에서 RDS 연결 테스트**:

```bash
# MySQL 클라이언트 설치
sudo apt-get install -y mysql-client

# RDS 연결 테스트
mysql -h <RDS_ENDPOINT> -u admin -p
# 비밀번호 입력 후 연결 확인
```

---

### 5. 배포 프로세스 확인

#### 5-1. develop 브랜치 push 시

**실행되는 작업**:

- ✅ Checkstyle 검사
- ✅ Spotless 검사
- ✅ 빌드/테스트
- ✅ JaCoCo 리포트 생성
- ✅ SonarQube 분석 (토큰 설정 시)
- ❌ Docker 빌드/배포 없음

#### 5-2. main 브랜치 push 시

**실행되는 작업**:

- ✅ Checkstyle 검사
- ✅ Spotless 검사
- ✅ 빌드/테스트
- ✅ JaCoCo 리포트 생성
- ✅ SonarQube 분석 (토큰 설정 시)
- ✅ Docker 이미지 빌드/푸시
- ✅ EC2 자동 배포

**배포 확인**:

```bash
# EC2에서 확인
ssh ubuntu@<EC2_PUBLIC_IP>
docker ps
docker logs assignment-app
```

**브라우저 확인**:

- http://<EC2_PUBLIC_IP>:8080/docs
- http://<EC2_PUBLIC_IP>:8080/actuator/health

---

### 6. 문제 해결

#### 6-1. GitHub Actions 실패 시

**Checkstyle/Spotless 실패**:

- 로컬에서 `./gradlew checkstyleMain checkstyleTest` 실행
- `./gradlew spotlessApply` 실행 후 재커밋

**SonarQube 실패**:

- SONAR_TOKEN이 올바른지 확인
- SonarQube 서버가 접근 가능한지 확인

**Docker 빌드 실패**:

- DOCKERHUB_USERNAME, DOCKERHUB_TOKEN 확인
- Docker Hub 저장소가 존재하는지 확인

#### 6-2. EC2 배포 실패 시

**SSH 연결 실패**:

- EC2_HOST, EC2_USER, EC2_SSH_KEY 확인
- EC2 보안 그룹에서 SSH(22) 포트 허용 확인

**Docker 이미지 Pull 실패**:

- EC2에서 Docker Hub 로그인 가능한지 확인
- 네트워크 연결 확인

**앱 실행 실패**:

- DB_URL, DB_USERNAME, DB_PASSWORD 확인
- RDS 보안 그룹에서 EC2 접근 허용 확인
- `docker logs assignment-app`로 에러 확인

---

### 7. 보안 권장사항

1. **GitHub Secrets**: 민감한 정보는 절대 코드에 커밋하지 않기
2. **RDS 비밀번호**: 강력한 비밀번호 사용 (대소문자, 숫자, 특수문자 포함)
3. **EC2 SSH Key**: 안전한 곳에 보관, 공유하지 않기
4. **보안 그룹**: 최소 권한 원칙 (필요한 포트만 오픈)
5. **SonarQube 토큰**: 정기적으로 갱신

---

## 📝 설정 요약

### 필수 설정 (운영 배포 시)

- [ ] GitHub Secrets: DOCKERHUB_USERNAME, DOCKERHUB_TOKEN, DOCKER_IMAGE_NAME
- [ ] GitHub Secrets: EC2_HOST, EC2_USER, EC2_SSH_KEY, SERVER_PORT
- [ ] GitHub Secrets: DB_URL, DB_USERNAME, DB_PASSWORD
- [ ] EC2 인스턴스 생성 및 Docker 설치
- [ ] RDS MySQL 생성 및 보안 그룹 설정

### 선택 설정 (품질 분석용)

- [ ] GitHub Secrets: SONAR_HOST_URL, SONAR_TOKEN
- [ ] SonarQube 서버 설정 (로컬 또는 SonarCloud)

### 로컬 개발용

- [ ] SonarQube 토큰 환경변수 설정
- [ ] Docker Compose 실행
