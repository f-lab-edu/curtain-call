# 로그인(Login) 기능 설계

- 작성일: 2026-06-05
- 기준 문서: [아키텍처 설계 초안](./2026-05-31-curtain-call-architecture-design.md), [ERD 수정본](./2026-05-31-curtain-call-erd-revised.md)
- 선행 기능: 회원가입 (commit 9419887, `com.curtaincall.user` 패키지)

## 1. 목표 및 범위

이메일/비밀번호 기반 로그인을 구현한다. 아키텍처 문서가 정한 **세션 기반 인증**을 따르되, 세션 저장소의 Redis 외부화는 본 단계 범위에서 제외하고 분산 인프라 단계(아키텍처 문서 §9-4)로 연기한다.

### 포함

- 로그인 (세션 생성)
- 로그아웃 (세션 무효화)
- 현재 로그인 사용자 조회 (세션 동작 검증용)
- 로그인 실패 / 미인증 접근에 대한 일관된 에러 응답

### 제외 (후순위)

- 세션의 Redis 외부화 (분산 인프라 단계에서 Spring Session Data Redis 도입)
- 비밀번호 재설정, 이메일 인증, 소셜 로그인
- Remember-me / 자동 로그인

### 결정 사항 요약 (브레인스토밍 합의)

| 항목 | 결정 |
|---|---|
| 인증 방식 | 세션 기반 (현재는 서버 메모리 세션, 추후 Redis 외부화) |
| 로그인 구현 방식 | 커스텀 REST 엔드포인트 + `AuthenticationManager` |
| 패키지 | `com.curtaincall.auth` 신설 (signup은 `user`에 유지) |

## 2. 아키텍처

기존 `com.curtaincall.user` 패키지(회원가입)는 변경하지 않고, 인증 도메인을 `com.curtaincall.auth`로 신설한다. 로그인은 Spring Security의 `AuthenticationManager`에 위임하며, 인증 성공 시 `SecurityContext`를 HTTP 세션에 영속화한다.

```
com.curtaincall.auth/
├── controller/AuthController.java          REST 엔드포인트 (login/logout/me)
├── service/AuthService.java                인증 로직 (AuthenticationManager 위임)
├── security/CustomUserDetailsService.java  email로 User 로드 → UserDetails
├── security/CustomUserDetails.java         User 래핑 + ROLE_ 권한 매핑
├── dto/LoginRequest.java                   record(email, password)
└── dto/LoginResponse.java                  record(userId, name, email, role)
```

- 데이터 접근은 기존 `UserMapper.findByEmail(email)`을 재사용한다 (이미 구현됨, password 포함 전체 User 반환).
- **DB 스키마 변경 없음.**
- 트랜잭션 경계가 필요한 쓰기 작업은 없다 (로그인은 읽기 + 세션 생성).

## 3. 엔드포인트

> 컨텍스트 패스 `/api` 적용 (application.yml). `SecurityConfig`의 matcher는 `/api`를 제외한 경로 사용.

| 메서드 | 경로 | 인증 필요 | 설명 | 성공 응답 |
|---|---|---|---|---|
| POST | `/api/auth/login` | 아니오 | 로그인, 세션 생성 | 200 + `LoginResponse` |
| POST | `/api/auth/logout` | 예 | 로그아웃, 세션 무효화 | 204 No Content |
| GET | `/api/auth/me` | 예 | 현재 로그인 사용자 조회 | 200 + `LoginResponse` |

### DTO

```java
// LoginRequest
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}

// LoginResponse
public record LoginResponse(
    Long userId,
    String name,
    String email,
    Role role
) {}
```

## 4. 데이터 흐름 (로그인)

1. `AuthController.login(@Valid @RequestBody LoginRequest, HttpServletRequest, HttpServletResponse)`
2. `AuthService.authenticate(email, password)` 호출 →
   `AuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password))`
3. 내부적으로 `CustomUserDetailsService.loadUserByUsername(email)` →
   `UserMapper.findByEmail(email)` → 없으면 `UsernameNotFoundException`, 있으면 `CustomUserDetails` 반환
4. `DaoAuthenticationProvider`가 `BCryptPasswordEncoder.matches(raw, hash)`로 비밀번호 검증
5. 성공 시:
   - `SecurityContext`에 `Authentication` 저장
   - `SecurityContextRepository`(`HttpSessionSecurityContextRepository`)로 세션에 영속화
   - 세션 고정 보호: `request.changeSessionId()` (인증 직전 세션 ID 재발급)
6. 인증된 `CustomUserDetails`에서 사용자 정보를 꺼내 `LoginResponse` 반환

### 로그아웃 흐름

- `SecurityContextLogoutHandler` 또는 동등 로직으로 `SecurityContext` 비우고 `HttpSession.invalidate()` 수행 → 204.

### `/me` 흐름

- Spring Security 필터가 세션의 `SecurityContext`를 복원 → `@AuthenticationPrincipal CustomUserDetails`에서 사용자 정보 추출 → `LoginResponse` 반환.

## 5. 보안 설정 (`SecurityConfig`) & 권한

- `authorizeHttpRequests`에 `POST /auth/login` `permitAll()` 추가. `/auth/me`, `/auth/logout`은 `authenticated()` (기존 `anyRequest().authenticated()`로 커버).
- `AuthenticationManager`를 빈으로 노출 (`AuthenticationConfiguration.getAuthenticationManager()`).
- `CustomUserDetailsService`(`UserDetailsService` 구현) + 기존 `PasswordEncoder` 빈으로 `DaoAuthenticationProvider`가 구성되도록 한다.
- **권한 매핑**: `Role` enum은 변경하지 않고, `CustomUserDetails.getAuthorities()`에서 `new SimpleGrantedAuthority("ROLE_" + role.name())`로 매핑한다 (예: `ROLE_MEMBER`).
- **CSRF**: 본 단계에서는 비활성 유지 (JSON REST 클라이언트 가정). 아키텍처 문서의 "세션 도입 시 재검토" 메모는 인지하되, CSRF 토큰 적용은 별도 범위로 미루고 주석으로 명시한다.

## 6. 에러 처리 (`GlobalExceptionHandler` 확장)

기존 응답 포맷 `{"message": "..."}` (Map<String,String>)을 유지한다.

| 상황 | 예외 | HTTP | message |
|---|---|---|---|
| 이메일 없음 / 비밀번호 불일치 | `BadCredentialsException` (Spring Security) | 401 | `이메일 또는 비밀번호가 일치하지 않습니다.` |
| 미인증 상태로 보호 자원 접근 | (인증 진입점) | 401 | `인증이 필요합니다.` |
| 요청 검증 실패 | `MethodArgumentNotValidException` (기존 핸들러) | 400 | `<field>: <message>` |

- **계정 열거 방지**: 이메일 부재와 비밀번호 불일치를 구분하지 않고 동일한 401 메시지로 응답한다. 이를 위해 `CustomUserDetailsService`의 `UsernameNotFoundException`은 `DaoAuthenticationProvider`에 의해 `BadCredentialsException`으로 변환되도록 한다(`hideUserNotFoundExceptions` 기본 동작 유지).
- **미인증 접근**: `AuthenticationEntryPoint`를 커스터마이징하여 기본 로그인 페이지 리다이렉트 대신 JSON `{"message": "인증이 필요합니다."}` + 401을 반환한다.

## 7. 테스트 전략

기존 3계층 테스트 패턴(JUnit 5 + Mockito + AssertJ)을 따른다. TDD로 진행한다.

| 테스트 | 종류 | 검증 내용 |
|---|---|---|
| `CustomUserDetailsServiceTest` | 단위 (UserMapper 모킹) | email 로드 시 권한(`ROLE_*`)·필드 매핑; 없는 email → `UsernameNotFoundException` |
| `AuthServiceTest` | 단위 (AuthenticationManager 모킹) | 성공 시 인증 반환; 실패 시 예외 전파 |
| `AuthControllerTest` | `@WebMvcTest` + MockMvc | 로그인 200 + body; 잘못된 자격증명 401; 검증 실패(빈/형식) 400; 로그아웃 204 |
| `AuthIntegrationTest` | 통합 (전체 컨텍스트) | 로그인 → 세션 쿠키 → `/me` 사용자 유지 → 로그아웃 후 `/me` 401 |

- 컨트롤러 테스트는 `@WebMvcTest(AuthController.class) + @Import(SecurityConfig.class)`로 구성하고 `AuthService`를 `@MockitoBean`으로 대체한다.
- 통합 테스트는 H2 + 실제 세션 흐름을 사용한다.

## 8. 구현 순서 (개요)

1. `auth` 패키지 골격 + DTO
2. `CustomUserDetails` / `CustomUserDetailsService` (테스트 우선)
3. `SecurityConfig` 확장 (AuthenticationManager, permitAll, EntryPoint)
4. `AuthService` (테스트 우선)
5. `AuthController` (login/logout/me, 테스트 우선)
6. `GlobalExceptionHandler` 확장 (BadCredentialsException)
7. 통합 테스트로 세션 흐름 검증

> 상세 단계는 후속 implementation plan에서 작성한다.
