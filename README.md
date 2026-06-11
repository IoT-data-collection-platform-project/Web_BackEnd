# IoT Web Backend

IoT 기기에서 전송되는 센서 데이터를 MQTT로 수집하고, 기기 승인/제어, 알림, 기상 데이터, AI 분석 및 리포트 기능을 제공하는 Spring Boot 기반 백엔드 서버입니다.


## 주요 기능

| 영역 | 설명 |
| --- | --- |
| 인증/보안 | Spring Security 세션 인증, BCrypt 비밀번호 해싱, CSRF 토큰, 세션 쿠키 보안, CORS 제한 |
| 기기 등록 | MQTT 등록 요청을 수신하고 관리자가 기기를 승인 또는 거절 |
| 기기 상태 관리 | 온라인, 오프라인, 승인 대기, 거절 상태 관리 |
| 센서 데이터 수집 | MQTT 텔레메트리 메시지를 수신해 MariaDB에 저장 |
| 알림 처리 | 화재, 온도, 습도, TVOC, eCO2 기준으로 알림 생성 및 해제 |
| 환경 제어 | 사용자 설정값과 현재 제어 상태 저장, MQTT 제어 명령 발행 |
| 기상 데이터 | 기상청 API Hub에서 외부 날씨 및 특보 데이터를 수집 |
| 히스토리 | 일간, 주간, 월간 센서/날씨 데이터를 시간 단위로 집계 |
| AI 분석 | LLM 서버에 환경 분석 요청, 결과 저장 및 MQTT 발행 |
| AI 리포트 | 매일 01:00 전일 데이터 기반 AI 리포트 생성 |

## 기술 스택

| 구분 | 기술 | 용도 |
| --- | --- | --- |
| Language | Java 17 | 백엔드 애플리케이션 개발 |
| Framework | Spring Boot 3.5.13 | 애플리케이션 실행 및 자동 설정 |
| Web | Spring Web MVC | REST API 제공 |
| Security | Spring Security | 세션 인증, CSRF, API 접근 제어 |
| Password Hashing | BCryptPasswordEncoder | 비밀번호 단방향 해싱 |
| ORM | Spring Data JPA, Hibernate | 엔티티 매핑 및 데이터 접근 |
| Database | MariaDB | 운영 데이터 저장소 |
| Migration | Flyway | DB 스키마 버전 관리 |
| Messaging | Spring Integration MQTT, Eclipse Paho | MQTT 구독/발행 |
| HTTP Client | Spring WebFlux WebClient | LLM 서버 연동 |
| Validation | Spring Validation | 요청 DTO 검증 |
| Scheduling | Spring Scheduling | 날씨 수집, AI 리포트, 파티션 관리 작업 |
| Boilerplate | Lombok | Getter, Builder, 생성자 코드 축약 |
| Test | JUnit 5, Spring Boot Test, H2 | 테스트 및 인메모리 DB |
| Build | Gradle Wrapper | 빌드 및 의존성 관리 |
| Deployment | GitHub Actions, SSH, Docker Compose | 클라우드 서버 자동 배포 |

## 전체 구조

```mermaid
flowchart LR
    Device["IoT Device"] -->|provisioning/request| MQTT["MQTT Broker"]
    Device -->|gateway/{mac}/telemetry| MQTT
    Device -->|devices/status| MQTT

    MQTT --> Backend["Spring Boot Backend"]
    Backend --> DB["MariaDB"]
    Backend -->|KMA API| KMA["KMA API Hub"]
    Backend -->|/api/v1/analyze| LLM["LLM Server"]
    Backend -->|/api/v1/report| LLM

    Backend -->|provisioning/response/{mac}| MQTT
    Backend -->|webbackend/control/{mac}| MQTT
    Backend -->|webbackend/alarm/{mac}| MQTT
    Backend -->|webbackend/analysis/{mac}| MQTT
```

## 디렉토리 구조

```text
src/main/java/com/iot_sw/iot_web_backend/
  Auth/          인증, 회원가입, 로그인, 로그아웃, 비밀번호 재설정, 보안 설정
  AiService/     AI 분석 요청, AI 분석 결과 저장, AI 일간 리포트 생성/조회
  dashboard/     기상청 API 연동, 날씨 데이터 저장, 대시보드 응답 생성
  device/        기기 등록/승인/거절, MQTT 센서 수집, 알림 처리
  history/       센서/날씨 히스토리 집계 API
  mqtt/          MQTT 브로커 연결, 구독 토픽, 발행 채널 구성
  setting/       환경 설정값, 현재 제어 상태, 제어 명령 발행
  IoTWebBackendApplication.java
  ServletInitializer.java

src/main/resources/
  application.yml
  db/migration/  Flyway SQL 마이그레이션

.github/workflows/
  deploy.yml      GitHub Actions 기반 클라우드 배포 워크플로
```

## 패키지 설명

| 패키지 | 설명 |
| --- | --- |
| `Auth` | 사용자 계정, 세션 로그인, CSRF 토큰 발급, 로그아웃, 로그인 기록, 비밀번호 재설정 |
| `device` | 기기 등록 요청 처리, 승인/거절, 온라인 상태 관리, 센서 데이터 수집, 알림 생성 |
| `mqtt` | MQTT 브로커 연결 설정, inbound/outbound 채널, 메시지 발행 게이트웨이 |
| `dashboard` | 기상청 API 호출, 날씨 데이터 저장, 대시보드용 데이터 응답 |
| `setting` | 환경 설정값과 실제 제어 상태 관리, 제어 명령 MQTT 발행 |
| `history` | 센서 데이터와 날씨 데이터를 기간별로 집계 |
| `AiService` | LLM 서버 분석 요청, AI 결과 저장, 일간 AI 리포트 생성 및 조회 |

## 보안 구조

| 항목 | 적용 내용 |
| --- | --- |
| 인증 방식 | Spring Security 기반 세션 인증 |
| 비밀번호 보호 | BCrypt 기반 단방향 해싱 |
| CSRF 보호 | `CookieCsrfTokenRepository` 기반 CSRF 토큰 |
| 접근 제어 | 인증 필요 API에 대해 로그인 여부 기반 접근 제어 |
| 세션 쿠키 | `HttpOnly`, `SameSite=Lax`, `Secure` 환경변수 제어 |
| CORS | 허용 Origin 패턴 제한 |

현재 인가는 로그인 여부 기반입니다. 관리자/일반 사용자 역할을 분리하는 RBAC는 별도로 구현되어 있지 않습니다.

인증 없이 접근 가능한 API:

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/api/auth/csrf` | CSRF 토큰 발급 |
| `POST` | `/api/auth/signup` | 회원가입 |
| `POST` | `/api/auth/login` | 로그인 |
| `POST` | `/api/auth/password-reset/request` | 비밀번호 재설정 코드 발급 |
| `POST` | `/api/auth/password-reset/confirm` | 비밀번호 재설정 확정 |

그 외 주요 서비스 API는 `SecurityConfig`에서 인증 필요 경로로 보호합니다.

## API 요약

### Auth

| Method | Path | 설명 | 인증 |
| --- | --- | --- | --- |
| `GET` | `/api/auth/csrf` | CSRF 토큰 발급 | 공개 |
| `POST` | `/api/auth/signup` | 회원가입 | 공개 |
| `POST` | `/api/auth/login` | 로그인 및 세션 생성 | 공개 |
| `GET` | `/api/auth/me` | 현재 사용자 및 연결 기기 정보 | 필요 |
| `POST` | `/api/auth/logout` | 로그아웃 | 필요 |
| `POST` | `/api/auth/password-reset/request` | 비밀번호 재설정 코드 발급 | 공개 |
| `POST` | `/api/auth/password-reset/confirm` | 비밀번호 재설정 확정 | 공개 |

### Device

| Method | Path | 설명 | 인증 |
| --- | --- | --- | --- |
| `GET` | `/api/devices/pending` | 승인 대기 기기 목록 조회 | 필요 |
| `GET` | `/api/devices/online` | 온라인 기기 목록 조회 | 필요 |
| `POST` | `/api/devices/approve` | 기기 승인 | 필요 |
| `POST` | `/api/devices/reject` | 기기 거절 | 필요 |

### Dashboard / History

| Method | Path | 설명 | 인증 |
| --- | --- | --- | --- |
| `GET` | `/api/dashboard` | 대시보드용 날씨/센서 요약 조회 | 필요 |
| `GET` | `/api/history` | 일간/주간/월간 센서/날씨 집계 조회 | 필요 |

### Control / Alert

| Method | Path | 설명 | 인증 |
| --- | --- | --- | --- |
| `GET` | `/api/control/environment` | 환경 설정값 조회 | 필요 |
| `PUT` | `/api/control/environment` | 환경 설정값 저장 | 필요 |
| `GET` | `/api/control/status` | 현재 제어 상태 조회 | 필요 |
| `PUT` | `/api/control/status` | 현재 제어 상태 저장 및 MQTT 제어 명령 발행 | 필요 |
| `GET` | `/api/alerts/active/{mac}` | 활성 알림 조회 | 필요 |
| `PATCH` | `/api/alerts/read/{mac}` | 특정 기기의 알림 전체 읽음 처리 | 필요 |
| `PATCH` | `/api/alerts/read/{mac}/category` | 특정 기기/카테고리 알림 읽음 처리 | 필요 |

### AI

| Method | Path | 설명 | 인증 |
| --- | --- | --- | --- |
| `GET` | `/api/ai/latest` | 최신 AI 분석 결과 조회 | 필요 |
| `POST` | `/api/ai/reanalyze` | 지정 MAC 기준 AI 재분석 요청 | 필요 |
| `GET` | `/api/ai/report/latest` | 최신 AI 리포트 조회 | 필요 |
| `GET` | `/api/ai/report/daily` | 날짜별 AI 리포트 조회 | 필요 |

## MQTT 토픽

### Subscribe

| Topic | 설명 |
| --- | --- |
| `provisioning/request` | 신규 기기 등록 요청 |
| `gateway/+/telemetry` | 기기 센서 텔레메트리 |
| `devices/status` | 기기 온라인/오프라인 상태 |

### Publish

| Topic | 설명 |
| --- | --- |
| `provisioning/response/{mac}` | 기기 승인/거절 결과 |
| `webbackend/control/{mac}` | 제어 명령 |
| `webbackend/alarm/{mac}` | 알림 발생/해제 이벤트 |
| `webbackend/analysis/{mac}` | AI 분석 결과 이벤트 |

## DB 마이그레이션

Flyway로 스키마를 관리합니다. 애플리케이션 시작 시 `src/main/resources/db/migration` 아래의 SQL 파일이 버전 순서대로 적용됩니다.

| Version | 설명 |
| --- | --- |
| `V1` | 초기 사용자, 날씨, 기기 테이블 |
| `V2` | 센서 텔레메트리 테이블 |
| `V3` | 알림 로그 테이블 |
| `V4` | 기기 승인 로그 테이블 |
| `V5` | 기기 상태 로그 테이블 |
| `V6` | AI 분석 및 제어 로그 테이블 |
| `V7` | 환경 설정, 제어 상태 테이블 |
| `V8` | AI 리포트 테이블 |
| `V9` | 사용자 생성/수정 시각, 사용자 로그인 로그 |
| `V10` | 사용자 생성/수정 시각 백필 |
| `V11` | 비밀번호 재설정 토큰 테이블 |
| `V12` | 비밀번호 재설정 코드 해시 컬럼 |
| `V13` | 기존 기기의 제어 상태 기본값 백필 |

주의사항:

- 이미 DB에 적용된 마이그레이션 파일은 수정하지 않습니다.
- 스키마 변경은 새 `V{number}__description.sql` 파일로 추가합니다.
- `spring.jpa.hibernate.ddl-auto=validate`이므로 엔티티와 DB 스키마가 다르면 애플리케이션 시작이 실패합니다.

## 환경변수

클라우드 배포 환경에서는 Docker Compose 또는 서버 환경변수로 다음 값을 주입합니다.

| 변수 | 설명 | 예시 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | 실행 프로파일 | `prod` |
| `SPRING_DATASOURCE_URL` | DB URL 오버라이드 | `jdbc:mariadb://mariadb:3306/iot_db` |
| `MYSQL_USER` | DB 사용자 | `project_user` |
| `MYSQL_PASSWORD` | DB 비밀번호 | `change-me` |
| `SESSION_COOKIE_SECURE` | HTTPS 환경 Secure 쿠키 사용 여부 | `true` |
| `SESSION_TIMEOUT` | 세션 타임아웃 | `30m` |
| `KMA_AUTH_KEY` | 기상청 API 인증키 |  |
| `KMA_WARNING_AUTH_KEY` | 기상특보 API 인증키, 없으면 `KMA_AUTH_KEY` 사용 |  |
| `MQTT_BROKER_URL` | MQTT 브로커 URL | `tcp://mosquitto:1883` |
| `LLM_SERVER_URL` | `llm.server.url` 오버라이드 | `http://llm-server:8000` |
| `WEATHER_FETCH_ENABLED` | 날씨 수집 활성화 여부 | `true` |
| `WEATHER_FETCH_CRON` | 날씨 수집 스케줄 | `0 0 * * * *` |

`application.yml`에는 `dev`, `prod` 프로파일이 정의되어 있습니다. 클라우드 서버에서는 `SPRING_PROFILES_ACTIVE=prod`를 명시하는 것을 권장합니다.

## 클라우드 배포

`.github/workflows/deploy.yml`은 `main` 브랜치 push 시 GitHub Actions에서 SSH로 서버에 접속해 백엔드 컨테이너를 다시 빌드하고 실행합니다.

배포 흐름:

```text
~/iot-platform/backend 에서 git pull
~/iot-platform 에서 docker-compose rm -fs backend
~/iot-platform 에서 docker-compose up -d --build backend
```

이 저장소에는 운영용 `docker-compose.yml`이 포함되어 있지 않습니다. 실제 Compose 파일과 네트워크 구성은 클라우드 서버의 `~/iot-platform` 환경에 존재한다고 가정합니다.

운영 배포 전 확인할 항목:

| 항목 | 확인 내용 |
| --- | --- |
| Profile | `SPRING_PROFILES_ACTIVE=prod` |
| DB | MariaDB 컨테이너/서비스 이름과 `SPRING_DATASOURCE_URL` 일치 |
| MQTT | `MQTT_BROKER_URL`이 백엔드 컨테이너에서 접근 가능한 주소인지 확인 |
| LLM | `LLM_SERVER_URL`이 백엔드 컨테이너에서 접근 가능한 주소인지 확인 |
| Weather API | `KMA_AUTH_KEY`, `KMA_WARNING_AUTH_KEY` 설정 |
| Cookie | HTTPS 뒤에서 실행 시 `SESSION_COOKIE_SECURE=true` |
| CORS | 실제 서비스 Origin이 허용 목록에 포함되어 있는지 확인 |

## 빌드 및 검증

클라우드 배포 전 최소한 다음 명령으로 컴파일과 테스트를 확인합니다.

```powershell
.\gradlew.bat classes
.\gradlew.bat test
```

서버나 CI 환경에서는 Linux shell 기준으로 다음과 같이 실행할 수 있습니다.

```bash
./gradlew classes
./gradlew test
```

## 개발 규칙

| 규칙 | 설명 |
| --- | --- |
| 마이그레이션 추가 | 기존 Flyway 파일 수정 대신 새 버전 파일 추가 |
| API 보안 | 새 인증 필요 API를 추가하면 `SecurityConfig` 보호 목록 확인 |
| 세션 요청 | 브라우저 클라이언트는 쿠키 포함 요청을 사용해야 함 |
| DB 검증 | 엔티티 변경 시 Flyway 스키마와 Hibernate validate 정합성 확인 |
| MQTT 토픽 | 새 토픽 추가 시 구독/발행 방향과 payload 형식 문서화 |

## Troubleshooting

### Gradle Worker Daemon 실행 실패

로컬 또는 개발 PC에서 다음 오류가 발생하면 코드 컴파일 오류가 아니라 Gradle 캐시 또는 JDK 설정 문제일 가능성이 큽니다.

```text
ClassNotFoundException: worker.org.gradle.process.internal.worker.GradleWorkerMain
Failed to run Gradle Worker Daemon
```

확인할 항목:

- Java 17 JDK가 설치되어 있는지 확인합니다.
- `gradle.properties`의 `org.gradle.java.home`을 실제 JDK 17 경로로 설정합니다.
- Gradle wrapper distribution 또는 Gradle cache가 깨졌다면 캐시를 정리한 뒤 다시 실행합니다.
- 실행 중인 Gradle Daemon을 중지한 뒤 재시도합니다.

```powershell
.\gradlew.bat --stop
.\gradlew.bat classes --no-daemon
```
