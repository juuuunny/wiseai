# 다음 단계 계획

## ✅ 완료된 작업

### Issue #45: 통합 테스트 작성
- ✅ 예약-결제 통합 플로우 테스트
- ✅ Deadlock 방지 통합 테스트
- ✅ Mock 결제 API 통합 테스트
- ✅ 통합 테스트 인프라 구성
- ✅ README 업데이트
- ✅ PR 설명 작성

**상태**: PR 준비 완료, GitHub에서 PR 생성 대기

---

## 🔍 발견된 TODO 항목

### 1. Security Context에서 userId 가져오기
**위치**: `ReservationController.java:40`
```java
// TODO: Security Context에서 userId 가져오기 (현재는 임시로 1L 사용)
Long userId = 1L;
```

**작업 내용**:
- Spring Security의 `SecurityContext`에서 인증된 사용자 정보 추출
- `@AuthenticationPrincipal` 또는 `SecurityContextHolder` 사용
- 모든 예약 관련 API에 적용

**예상 이슈 번호**: #46 (또는 새로운 이슈)

---

## 📋 다음 우선순위 작업

### 1. PR 생성 및 머지 (즉시)
- GitHub에서 `feat/integration-tests/#45` → `develop` PR 생성
- PR 제목: `[feat] 통합 테스트 작성 (#45)`
- PR 설명: `PR_DESCRIPTION.md` 내용 사용

### 2. Security Context 구현 (#46)
- JWT 인증 정보에서 userId 추출
- ReservationController 수정
- 관련 테스트 업데이트

### 3. 추가 개선 사항 (선택)
- 테스트 커버리지 향상
- 성능 테스트 추가
- API 문서 보완
- 모니터링 및 로깅 개선

---

## 🎯 권장 작업 순서

1. **PR 생성 및 머지** (현재 브랜치)
2. **develop 브랜치로 전환**
3. **Security Context 구현** (새 브랜치 생성)
4. **추가 테스트 작성**
