# Health_mr

**EMR(전자의무기록) 시스템** — Java 기반 멀티모듈 백엔드 프로젝트입니다.  
Nest 버전 대비 Java/Spring Boot 기반으로 재구성된 의료 정보 시스템입니다.

---

## 프로젝트 소개

본 프로젝트는 병원·의료기관의 **전자의무기록(EMR)** 업무를 지원하는 백엔드 서비스입니다.  
도메인별로 분리된 모듈 구조로 **임상(Clinical)**, **재무(Finance)**, **지원(Support)** 등 업무 영역을 나누고,  
공통 인프라(**emr-core**)와 공유 도메인(**emr-domain**)을 두어 유지보수성과 확장성을 높였습니다.

### 주요 기능 영역

| 영역 | 설명 |
|------|------|
| **임상 (Clinical)** | 예약, 접수(체크인), 진료, 처방, 입원, AI 기반 진료 분석/추천 |
| **재무 (Finance)** | 수가/진료비, 결제, 계약, 비급여 항목, 의료급여·기초생활수급자 자격 연동 |
| **지원 (Support)** | 게시판, 근태, 휴가/휴직, 휴일, 장비 연동, 건강검진·장애인 시설 정보, 검사 등 |
| **공통 도메인 (Domain)** | 사용자·환자·부서·기관, 인증, 메시지 등 |

---

## 기술 스택

- **언어**: Java 21  
- **빌드**: Gradle (Kotlin DSL)  
- **프레임워크**: Spring Boot 4.0.x  
- **주요 라이브러리**  
  - Spring Web, WebFlux, Security, Data JPA, Validation, AOP, Mail, Data Redis  
  - JWT (jjwt), Jasypt(암호화), QueryDSL, Apache POI, ModelMapper  
  - Lombok, RestAssured, Mockito, JUnit 5  

---

## 아키텍처 구조

### 모듈 의존성

```
                    ┌─────────────────┐
                    │   emr-clinical  │  ← 실행 가능 (Boot JAR)
                    │   emr-finance   │  ← 실행 가능 (Boot JAR)
                    │   emr-support   │  ← 실행 가능 (Boot JAR)
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
              ▼              ▼              ▼
    ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
    │ emr-domain  │  │  emr-core   │  │ emr-clinical │
    │ (공유 도메인) │  │ (공통 인프라) │  │ (emr-finance, │
    └──────┬──────┘  └──────┬──────┘  │  support에서  │
           │                │         │  사용)        │
           │                │         └──────────────┘
           └────────────────┘
                    │
                    ▼
             ┌─────────────┐
             │  emr-core   │  ← 라이브러리 (bootJar 비활성)
             └─────────────┘
```

- **emr-core**: 모든 하위 모듈이 의존하는 **공통 인프라** (보안, JWT, 락, 감사, 파일, 알림 등).  
- **emr-domain**: **공유 도메인** (사용자, 환자, 부서, 기관, 인증, 메시지). emr-core에 의존.  
- **emr-clinical**: **임상** 전용. emr-core, emr-domain 의존. 단독 실행 가능.  
- **emr-finance**: **재무** 전용. emr-core, emr-domain, emr-clinical 의존. 단독 실행 가능.  
- **emr-support**: **지원** 전용. emr-core, emr-domain, emr-clinical 의존. 단독 실행 가능.

### 모듈별 역할

| 모듈 | 타입 | 역할 |
|------|------|------|
| **emr-core** | 라이브러리 | 공통 설정·보안(JWT, 암호화, 마스킹), 분산 락/멱등성, 감사(Audit), 파일 업로드/엑셀, 알림, 이벤트, 예외 처리 |
| **emr-domain** | 라이브러리 | 사용자/환자/부서/기관 엔티티·서비스, 인증(Auth), 메시지 등 공유 도메인 로직 |
| **emr-clinical** | 애플리케이션 | 예약·접수·진료·처방·입원, 의약품 정보, 진료/입원 통계, AI 분석·추천·리포트 |
| **emr-finance** | 애플리케이션 | 진료비/수가, 수가 코드 통계, 비급여 항목, 결제, 계약, 의료급여·기초생활수급 자격 연동 |
| **emr-support** | 애플리케이션 | 게시판·댓글, 근태·휴가·휴직·휴일, 장비 연동, 건강검진 기관, 장애인 시설, 검사, 의사 진료 등 |

---

## 프로젝트 구조

```
Health_mr/
├── README.md
└── java_backend/
    ├── build.gradle.kts          # 루트 빌드 설정
    ├── settings.gradle.kts       # 모듈 include
    ├── emr-core/                 # 공통 인프라
    ├── emr-domain/               # 공유 도메인
    ├── emr-clinical/             # 임상 서비스 (실행 가능)
    ├── emr-finance/              # 재무 서비스 (실행 가능)
    └── emr-support/              # 지원 서비스 (실행 가능)
```

각 모듈 내부는 `src/main/java` 아래에 패키지별로 Controller, Service, Repository, Entity, DTO 등이 구성됩니다.

---

## 빌드 및 실행

### 요구 사항

- JDK 21  
- Gradle (또는 wrapper 사용)

### 빌드

```bash
cd java_backend
./gradlew build
# Windows: gradlew.bat build
```

### 애플리케이션 실행

실행 가능한 모듈은 **emr-clinical**, **emr-finance**, **emr-support** 세 가지입니다.

```bash
# 임상 서비스
./gradlew :emr-clinical:bootRun

# 재무 서비스
./gradlew :emr-finance:bootRun

# 지원 서비스
./gradlew :emr-support:bootRun
```

각 서비스는 별도 포트/설정으로 기동하며, 필요 시 `application.yml`(또는 `application.properties`)에서 데이터소스·Redis·JWT 등 환경을 설정합니다.

---

## 라이선스 및 버전

- **Group**: `com.sleekydz86`  
- **Version**: `0.0.1-SNAPSHOT`  
- **Root project name**: `my-java-multimodule` (Gradle settings)

---

이 문서는 프로젝트 루트의 `README.md`이며, Java 백엔드(emr-*) 모듈의 소개와 아키텍처를 설명합니다.
