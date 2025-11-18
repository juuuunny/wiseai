# 회의실 예약 시스템 RESTful API

**GitHub 저장소**: https://github.com/juuuunny/wiseai

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

1. SonarQube 실행:

```bash
docker-compose up -d sonarqube
```

2. SonarQube 준비 대기 (약 1-2분):

```bash
# SonarQube가 준비될 때까지 대기
curl -f http://localhost:9000/api/system/status
```

3. SonarQube 토큰 발급:

   **방법 1: 웹 UI 사용**
   - http://localhost:9000 접속
   - 첫 실행 시: 초기 비밀번호 확인 (로그에서 확인)
   - 로그인 후: My Account → Security → Generate Token

   **방법 2: API 사용 (자동화)**

```bash
# SonarQube 10.x: 초기 비밀번호 확인 필요 (로그에서 확인)
# SonarQube 9.x: 기본 admin/admin 사용 가능
export SONAR_TOKEN=$(curl -u admin:admin -X POST "http://localhost:9000/api/user_tokens/generate?name=local-token&type=PROJECT_ANALYSIS_TOKEN" 2>/dev/null | jq -r '.token // empty')

# 토큰이 생성되었는지 확인
echo "SONAR_TOKEN=$SONAR_TOKEN"
```

4. 환경변수 설정:

```bash
export SONAR_HOST_URL=http://localhost:9000
export SONAR_TOKEN=<발급받은_토큰>  # 위에서 생성한 토큰
```

5. 빌드 (자동으로 SonarQube 분석까지 실행):

```bash
./gradlew clean build
# 또는 SonarQube만 실행
./gradlew sonarqube
```

**참고**: `SONAR_TOKEN`이 설정되지 않으면 SonarQube 분석은 스킵되지만 빌드는 정상적으로 완료됩니다.

**SonarQube 대시보드**: http://localhost:9000/projects?query=assignment

---

## 🏗️ 아키텍처

### 설계 원칙

본 프로젝트는 **DDD (Domain-Driven Design)**, **Hexagonal Architecture (Ports & Adapters)**, **Event-Driven Architecture**를 적용하여 설계되었습니다.

### 핵심 특징

#### 1. Domain Model과 Entity 분리

- **Domain Model**: 비즈니스 로직을 담은 순수한 도메인 객체 (JPA 의존성 없음)
- **Entity**: 데이터베이스 영속성을 위한 JPA 엔티티
- **Mapper**: Domain Model ↔ Entity 변환 담당

```java
// Domain Model (비즈니스 로직)
User user = User.create(email, password, name);

// Entity (영속성 계층)
UserEntity entity = userEntityMapper.toEntity(user);
User domain = userEntityMapper.toDomain(entity);
```

**장점**:
- Domain Model이 데이터베이스 기술에 독립적
- 비즈니스 로직과 영속성 계층의 명확한 분리
- 테스트 용이성 향상

#### 2. Hexagonal Architecture (Ports & Adapters)

**모듈 구조**:
```
modules/
├── adapter/          # 외부와의 연결 (Web, JPA, Redis, 다른 모듈)
├── application/      # 애플리케이션 로직
│   ├── port/
│   │   ├── in/      # Port In (UseCase) - 외부에서 호출받는 용도
│   │   └── out/     # Port Out - 외부를 호출하는 용도
│   └── service/     # Application Service (UseCase 구현)
└── domain/          # 도메인 로직
    ├── model/       # Domain Model
    ├── service/     # Domain Service
    └── exception/   # Domain Exception
```

**모듈 간 호출 원칙**:
- Application Service는 **자신의 모듈의 Port Out**만 의존
- Adapter Out이 실제로 **다른 모듈의 Port In(UseCase)**를 호출
- Port Out과 Adapter Out은 **호출하는 모듈**에 위치

**예시**: `auth` 모듈이 `user` 모듈을 호출하는 경우
```java
// auth 모듈의 Service
AuthCommandService {
    UserQueryPort userQueryPort;  // auth 모듈의 Port Out
}

// auth 모듈의 Adapter
UserQueryAdapter implements UserQueryPort {
    IsLoginPossibleUseCase isLoginPossibleUseCase;  // user 모듈의 Port In 호출
}
```

**장점**:
- 모듈 간 결합도 최소화
- 의존성 역전 원칙(DIP) 준수
- 테스트 용이성 (Mock 객체 활용)

#### 3. Event-Driven Architecture

- Domain Event를 통한 모듈 간 비동기 통신
- 향후 Kafka로 확장 가능한 구조

### 모듈 구성

- **user**: 사용자 관리 (회원가입, 로그인 인증)
- **auth**: 인증/인가 (JWT 토큰 관리, 리프레시 토큰)
- **security**: Spring Security 설정 및 JWT 필터
- **common**: 공통 응답, 예외 처리, 유틸리티

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
