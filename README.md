# 회의실 예약 시스템 RESTful API

## 📋 프로젝트 개요

사내 회의실 예약을 위한 RESTful API 서버입니다. Docker & Docker Compose 기반 컨테이너 환경에서 실행되며, Swagger UI를 통한 API 문서화 및 테스트를 지원합니다.

### 주요 기능

- 회의실 목록 조회
- 예약 생성/조회/취소 (CRUD)
- 결제 처리 (다중 결제사 통합)
- 결제 상태 조회
- 결제사별 웹훅 수신

### 핵심 요구사항

- ✅ 예약 시간 중복 방지 (동일 회의실)
- ✅ 시작 시간 < 종료 시간
- ✅ 정시(00분) 또는 30분 단위로만 예약 가능
- ✅ 요청 파라미터 유효성 검사(Validation) 필수
- ✅ 결제 시스템 추상화 설계 (Strategy Pattern)
- ✅ 동시성 제어 (Deadlock 방지)

---

## 🛠 기술 스택

### 필수 기술 스택

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.3.11
- **빌드 도구**: Gradle 8.14.3
- **데이터베이스**: MySQL 8.0 (Docker 컨테이너)
- **ORM**: Spring Data JPA
- **API 문서화**: Swagger (OpenAPI 3.0) - springdoc-openapi 2.6.0
- **컨테이너**: Docker + Docker Compose
- **테스트**: JUnit 5

### 품질 도구

- **Checkstyle**: 코드 스타일 검사
- **Spotless**: 자동 코드 포맷팅
- **JaCoCo**: 테스트 커버리지 측정
- **SonarQube**: 종합 코드 품질 분석

---

## 🚀 실행 방법

### 1. Docker Compose로 전체 환경 실행 (권장)

**한 번의 명령으로 전체 실행**:

```bash
docker-compose up --build
```

**서비스 접속 URL**:

- **애플리케이션**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/docs
- **Health Check**: http://localhost:8081/actuator/health
- **MySQL**: localhost:3307 (db: `assignment` / user: `assignment` / pass: `assignment`)
- **WireMock** (모의 결제 서버): http://localhost:8089/\_\_admin/health
- **SonarQube**: http://localhost:9000 (초기 계정: `admin` / `WiseAi1234@@`)

**중지**:

```bash
docker-compose down -v
```

### 2. 로컬 실행 (Docker 없이)

**빌드**:

```bash
./gradlew clean build
```

**실행**:

```bash
SPRING_PROFILES_ACTIVE=default ./gradlew bootRun
```

**접속 URL**:

- Swagger UI: http://localhost:8080/docs
- Health Check: http://localhost:8080/actuator/health

> **참고**: 로컬 실행 시 MySQL을 별도로 설치하거나 Docker로 MySQL만 실행해야 합니다.

---

## 📖 Swagger UI 접속 방법

### Docker Compose 실행 시

1. `docker-compose up --build` 실행
2. 브라우저에서 http://localhost:8081/docs 접속
3. API 엔드포인트 목록 및 테스트 가능

### 로컬 실행 시

1. `SPRING_PROFILES_ACTIVE=default ./gradlew bootRun` 실행
2. 브라우저에서 http://localhost:8080/docs 접속

---

## 🧪 테스트 실행 방법

### 전체 테스트 실행

```bash
./gradlew clean test
```

### 테스트 + 커버리지 리포트 생성

```bash
./gradlew clean test jacocoTestReport
```

**커버리지 리포트 확인**:

```bash
open build/reports/jacoco/test/html/index.html
```

### 통합 테스트 포함 실행

```bash
./gradlew clean test --tests "*IntegrationTest"
```

---

## 🔍 품질 검사

### Checkstyle (코드 스타일 검사)

```bash
./gradlew checkstyleMain checkstyleTest
```

**리포트 확인**:

```bash
open build/reports/checkstyle/main.html
open build/reports/checkstyle/test.html
```

### Spotless (자동 코드 포맷팅)

```bash
# 포맷 적용
./gradlew spotlessApply

# 포맷 검사만
./gradlew spotlessCheck
```

### JaCoCo (테스트 커버리지)

```bash
./gradlew clean test jacocoTestReport
```

**리포트 확인**:

```bash
open build/reports/jacoco/test/html/index.html
```

### SonarQube (종합 품질 분석)

**로컬 SonarQube 사용 시**:

1. SonarQube 토큰 발급:

   - http://localhost:9000 접속
   - 로그인: `admin` / `WiseAi1234@@`
   - My Account → Security → Generate Token

2. 환경변수 설정:

```bash
export SONAR_HOST_URL=http://localhost:9000
export SONAR_TOKEN=<발급받은_토큰>
```

3. 빌드 (자동으로 SonarQube 분석까지 실행):

```bash
./gradlew clean build
```

**SonarQube 대시보드**: http://localhost:9000/projects?query=assignment

---

## 📁 프로젝트 구조

```
assignment/
├── src/
│   ├── main/
│   │   ├── java/com/wiseai/assignment/
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── application-prod.yaml
│   └── test/
├── .github/
│   └── workflows/
│       └── ci-cd.yml          # CI/CD 파이프라인
├── docker-compose.yml         # 로컬 개발 환경
├── docker-compose.aws.yml     # 운영 환경 (EC2)
├── Dockerfile                 # 애플리케이션 이미지
├── build.gradle               # 빌드 설정
├── checkstyle.xml             # 코드 스타일 규칙
├── quality-gate.yml           # 품질 게이트 설정
├── SETUP.md                   # 상세 설정 가이드
└── README.md                  # 프로젝트 문서
```

---

## 🔄 Git 브랜치 전략 및 CI/CD

### 브랜치 구조

```
main (프로덕션) ← EC2 자동 배포
  ↑
develop (개발 통합) ← 빌드/테스트만
  ↑
feature/* (기능 개발)
```

### 브랜치별 역할

- **feature/\***: 기능별 개발 브랜치
- **develop**: 개발 통합 브랜치 (CI: 빌드/테스트/소나 분석만)
- **main**: 프로덕션 브랜치 (CI/CD: 빌드/테스트/소나 → Docker 빌드/푸시 → EC2 배포)

### CI/CD 파이프라인

**develop 브랜치 push/PR**:

- ✅ Checkstyle 검사
- ✅ Spotless 검사
- ✅ 빌드/테스트
- ✅ JaCoCo 커버리지 리포트
- ✅ SonarQube 분석
- ❌ Docker 빌드/배포 없음

**main 브랜치 push/PR**:

- ✅ Checkstyle 검사
- ✅ Spotless 검사
- ✅ 빌드/테스트
- ✅ JaCoCo 커버리지 리포트
- ✅ SonarQube 분석
- ✅ Docker 이미지 빌드/푸시 (Docker Hub)
- ✅ EC2 자동 배포

---

## ☁️ 운영 환경 배포 (AWS EC2 + RDS)

**📌 상세 설정 가이드**: [SETUP.md](./SETUP.md) 참조

### 사전 준비

- EC2 (t3.micro) Ubuntu 22.04, 보안그룹 22/80/8080 오픈
- RDS MySQL (db.t3.micro), 보안그룹에서 EC2 인바운드 허용
- GitHub Secrets 설정 (Docker Hub, EC2, RDS 정보)

### 배포 절차

1. 로컬에서 `docker-compose up --build`로 개발 검증
2. develop 브랜치에 통합 (빌드/테스트만 실행)
3. main 브랜치로 PR 생성 → 머지 시 자동 배포

### 운영 환경 접속

- 앱: http://<EC2_PUBLIC_IP>:8080
- Swagger: http://<EC2_PUBLIC_IP>:8080/docs
- Health: http://<EC2_PUBLIC_IP>:8080/actuator/health

---

## 📝 개발 가이드

### 로컬 개발 환경 설정

1. **Docker Compose 실행**:

```bash
docker-compose up --build
```

2. **SonarQube 토큰 설정** (선택사항):

```bash
export SONAR_HOST_URL=http://localhost:9000
export SONAR_TOKEN=<발급받은_토큰>
```

3. **빌드 및 검증**:

```bash
./gradlew clean build
```

### 코드 품질 검사

모든 품질 검사는 빌드 시 자동 실행됩니다:

- Checkstyle: 코드 스타일 위반 시 빌드 실패
- Spotless: 포맷 위반 시 빌드 실패
- JaCoCo: 커버리지 30% 미만 시 빌드 실패
- SonarQube: 분석 결과를 SonarQube 서버에 저장

---

## 🔗 관련 문서

- [SETUP.md](./SETUP.md): 상세 설정 가이드 (GitHub Secrets, AWS 인프라 등)
- [quality-gate.yml](./quality-gate.yml): 품질 게이트 설정

---

## 📄 라이선스

이 프로젝트는 과제 전형용으로 작성되었습니다.
