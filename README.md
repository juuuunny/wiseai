# 회의실 예약 시스템 RESTful API

**GitHub 저장소**: https://github.com/juuuunny/wiseai

---

## 📋 프로젝트 개요

사내 회의실 예약을 위한 RESTful API 서버입니다. Docker & Docker Compose 기반 컨테이너 환경에서 실행되며, Swagger UI를 통한 API 문서화 및 테스트를 지원합니다.

### 주요 기능

- 회의실 목록 조회
- 예약 생성/조회/취소 (CRUD)
- 결제 처리 (다중 결제사 통합: TOSS, KAKAO, CARD, VIRTUAL_ACCOUNT)
- 결제 상태 조회
- 결제사별 웹훅 수신

### 핵심 요구사항

- ✅ **예약 시간 중복 방지** (동일 회의실) - `ReservationEntity` UNIQUE 제약조건 + 비즈니스 로직 검증
- ✅ **시작 시간 < 종료 시간 검증** - `Reservation` 도메인 모델 검증
- ✅ **정시(00분) 또는 30분 단위** - `Reservation` 도메인 모델 검증
- ✅ **요청 파라미터 유효성 검사** - Jakarta Validation (`@NotNull`, `@Min`, `@Future` 등)
- ✅ **결제 시스템 추상화 설계** (Strategy Pattern) - `PaymentGateway` 인터페이스 + 4개 결제사 구현
- ✅ **동시성 제어** (Deadlock 방지, 분산 락) - Redisson `@DistributedLock` AOP

---

## 🛠 기술 스택

### 필수 기술 스택

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.3.11
- **빌드 도구**: Gradle 8.14.3
- **데이터베이스**: MySQL 8.0
- **ORM**: Spring Data JPA
- **API 문서화**: Swagger (OpenAPI 3.0) - springdoc-openapi 2.6.0
- **컨테이너**: Docker + Docker Compose
- **테스트**: JUnit 5

### 추가 기술 스택

- **분산 락/토큰 저장**: Redis 7, Redisson (동시성 제어 및 리프레시 토큰 관리)
- **메시지 큐**: Apache Kafka
- **아키텍처**: Hexagonal Architecture (Ports & Adapters), DDD, Event-Driven Architecture
- **코드 품질 도구**: JaCoCo (코드 커버리지), SonarQube (코드 품질 분석), Checkstyle (코딩 표준), Spotless (자동 포맷팅)
- **CI/CD**: GitHub Actions, Docker Hub

---

## 🚀 실행 방법

### Docker Compose로 전체 환경 실행 (권장)

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
- **Redis**: localhost:6380

**중지**:

```bash
docker-compose down -v
```

---

## 📖 Swagger UI 접속 방법

### Docker Compose 실행 시

1. `docker-compose up --build` 실행
2. 브라우저에서 **http://localhost:8081/docs** 접속
3. API 엔드포인트 목록 및 테스트 가능

### 주요 API 엔드포인트

- **회의실 API**: `GET /meeting-rooms`, `GET /meeting-rooms/{id}`
- **예약 API**: `POST /reservations`, `GET /reservations/{id}`, `DELETE /reservations/{id}/users/{userId}`
- **예약 결제 API**: `POST /reservations/{id}/payment` (예약별 결제 처리)
- **결제 API**: `POST /payments`, `GET /payments/{id}`, `GET /payments/{paymentId}/status` (결제 상태 조회)
- **웹훅 API**: `POST /webhooks/payments/{provider}` (결제사별 웹훅 수신)

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

**통합 테스트 구성**:

- `ReservationPaymentIntegrationTest`: 예약-결제 통합 플로우 테스트
- `DeadlockPreventionIntegrationTest`: Deadlock 방지 시나리오 테스트
- `MockPaymentApiIntegrationTest`: Mock 결제 게이트웨이 테스트

---

## 🏗️ 설계 결정 사항

> **과제 핵심 요구사항 충족**: 본 섹션은 과제에서 요구한 핵심 주의사항을 어떻게 해결했는지 설명합니다.

### 아키텍처

- **Hexagonal Architecture (Ports & Adapters)**: 비즈니스 로직과 인프라 계층 분리, 테스트 용이성 향상
- **Domain Model과 Entity 분리**: JPA 의존성 없는 순수 도메인 객체로 비즈니스 로직 독립성 확보

### 1. Strategy Pattern을 통한 결제 시스템 추상화 ⭐ (과제 핵심 요구사항)

**과제 요구사항**: "결제 시스템 추상화 설계" - 결제사별 상이한 API를 공통 인터페이스로 추상화

**구현**:

- `PaymentGateway` 인터페이스: 공통 결제 처리 메서드 정의 (`processPayment()`)
- `PaymentGatewayFactory`: 결제 수단별 게이트웨이 동적 선택 (TOSS, KAKAO, CARD, VIRTUAL_ACCOUNT)
- `PaymentResult`: 결제사별 응답을 공통 모델로 변환 (`PaymentStatus.SUCCESS/FAILURE`)
- **확장성**: 새로운 결제사 추가 시 `PaymentGateway` 구현체만 추가

**구현 위치**: `PaymentGateway`, `PaymentGatewayFactory`, `TossPaymentGateway`, `KakaoPaymentGateway`, `CardPaymentGateway`, `VirtualAccountPaymentGateway`

### 2. 분산 락을 통한 동시성 제어 ⭐ (과제 핵심 요구사항)

**과제 요구사항**: "동시성 제어 (Deadlock 방지, 분산 락)"

**문제 상황**: 동일 회의실에 동시 예약 요청 시 Race Condition, 다중 인스턴스 환경에서 DB 레벨 락만으로는 부족

**해결 방법**:

1. **Redisson 분산 락**: `@DistributedLock` AOP 어노테이션으로 선언적 락 관리, 예약 생성/취소 시 `reservationId` 기반 락 획득
2. **Deadlock 방지**: Lock 순서 일관성 유지, Timeout 설정, 재시도 로직
3. **DB 레벨 보완**: `UNIQUE` 제약조건으로 예약 시간 중복 방지 (최종 안전장치)

**구현 위치**: `ReservationCommandService.createReservation()`, `cancelReservation()`, `DistributedLockAspect`, `RedissonDistributedLockManager`

### 3. 예약 시간 중복 방지 (과제 핵심 요구사항)

**과제 요구사항**: "예약 시간 중복 방지 (동일 회의실)"

**다층 방어 전략**:

1. **비즈니스 로직 검증**: 예약 생성 전 동일 회의실의 시간대 중복 체크 (`ReservationQueryPort.findOverlappingReservations()`)
2. **DB 제약조건**: `@Table(uniqueConstraints = @UniqueConstraint(...))` - 동일한 (회의실, 시작시간, 종료시간) 조합의 중복 방지
3. **분산 락과의 조합**: 분산 락으로 동시성 제어 + DB 제약조건으로 데이터 무결성 보장

**구현 위치**: `ReservationEntity`, `ReservationCommandService.createReservation()`

### 4. 결제 처리 중 예약 상태 관리 ⭐ (과제 핵심 요구사항)

**과제 요구사항**: "결제 처리 중 예약 상태 관리 (결제 대기 → 결제 완료 → 예약 확정)"

**예약 상태 플로우**:

1. **결제 대기 (PENDING)**: 예약 생성 시 초기 상태 `ReservationStatus.PENDING`
2. **결제 처리 (비동기)**: `POST /reservations/{id}/payment` 호출 시 Kafka 이벤트 발행, `PaymentProcessListener`가 비동기 처리
3. **예약 확정 (CONFIRMED)**: 결제 완료(`PaymentStatus.SUCCESS`) 시 자동으로 `CONFIRMED`로 변경

**구현 위치**: `PaymentProcessListener.handleMessage()`, `Reservation.confirm()`

**실무 고려사항**: 비동기 처리로 응답 시간 단축, Idempotency 로그로 중복 처리 방지, DLQ를 통한 실패 이벤트 재처리

### 5. 요청 파라미터 유효성 검사 (과제 핵심 요구사항)

**구현**: Jakarta Validation (`@NotNull`, `@Min`, `@Future` 등), 비즈니스 규칙 검증 (시작 시간 < 종료 시간, 30분 단위), `@ExceptionHandler`로 일관된 에러 응답

**구현 위치**: DTO 클래스, `Reservation` 도메인 모델, `GlobalExceptionHandler`

### 6. Event-Driven Architecture

**구현**: Kafka 비동기 이벤트 처리, Outbox Pattern (트랜잭션 일관성), Idempotency (중복 처리 방지), DLQ 및 Exponential Backoff

**구현 위치**: `PaymentProcessListener`, `PaymentOutboxRelay`, `PaymentProcessLogEntity`

### 7. 테스트 전략

**구현**: 단위 테스트 (Service 레이어, 도메인 모델), 통합 테스트 (예약-결제 플로우, Deadlock 방지, 결제 게이트웨이), H2 In-memory DB, 외부 의존성 Mock 처리

**구현 위치**: `*Test.java`, `*IntegrationTest.java`

---

## ☁️ 클라우드 아키텍처 & 실서비스 배포 설계

### 인프라 구성

**AWS EC2 + RDS** 기반, **Blue-Green 무중단 배포** 전략

- **컴퓨팅**: AWS EC2 (Docker 컨테이너), Nginx 로드밸런서
- **데이터베이스**: AWS RDS MySQL 8.0, Redis (ElastiCache) - Redisson 분산 락, JWT 토큰 저장
- **메시지 큐**: Apache Kafka (EC2 또는 MSK)
- **모니터링**: Spring Actuator + Prometheus, CloudWatch Logs
- **CI/CD**: GitHub Actions (자동 빌드/테스트, Docker Hub 푸시, 코드 품질 검사)

### 배포 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                    Internet                              │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
         ┌───────────────────────┐
         │   Nginx (Load Balancer)│
         │   Port: 80/443 (HTTPS) │
         └───────────┬────────────┘
                     │
         ┌───────────┴────────────┐
         │                        │
         ▼                        ▼
┌─────────────────┐      ┌─────────────────┐
│  Blue (Active)  │      │ Green (Standby) │
│  EC2 Instance   │      │  EC2 Instance    │
│  Docker Container│      │  Docker Container│
└────────┬────────┘      └────────┬────────┘
         │                        │
         └───────────┬────────────┘
                     │
         ┌───────────┴────────────┐
         ▼                        ▼
┌─────────────────┐      ┌─────────────────┐
│   RDS MySQL     │      │   Redis         │
│   (Primary DB)  │      │   (Distributed Lock) │
└─────────────────┘      └─────────────────┘
                     │
                     ▼
         ┌───────────────────────┐
         │   Apache Kafka        │
         │   (Event Broker)      │
         └───────────────────────┘
```

### 배포 전략

**Blue-Green 무중단 배포**: 두 개의 동일한 프로덕션 환경 유지, Nginx 로드밸런서로 트래픽 전환, RDS/Redis/Kafka는 공유 인프라

**배포 프로세스**: Green 환경에 새 버전 배포 및 검증 → Nginx 설정 변경으로 트래픽 전환 → Blue 환경 Standby (롤백 대비)

### 설계 고려사항

- **확장성/가용성**: EC2 Auto Scaling Group, Multi-AZ 배포 (RDS, ElastiCache)
- **보안**: HTTPS (SSL/TLS), JWT 인증, RDS 암호화, 보안 그룹, Secrets 관리
- **PCI DSS 준수**: 결제 데이터 분리/암호화, 네트워크 분리, 접근 제어, 감사 로그

---

## 📁 프로젝트 구조

```
assignment/
├── src/
│   ├── main/
│   │   ├── java/com/wiseai/assignment/
│   │   │   ├── modules/
│   │   │   │   ├── reservation/    # 예약 관리
│   │   │   │   ├── payment/        # 결제 처리
│   │   │   │   ├── meetingroom/    # 회의실 관리
│   │   │   │   ├── user/           # 사용자 관리
│   │   │   │   ├── auth/           # 인증/인가
│   │   │   │   └── common/         # 공통 모듈
│   │   │   └── AssignmentApplication.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
├── docker-compose.yml
├── Dockerfile
├── build.gradle
└── README.md
```

---
