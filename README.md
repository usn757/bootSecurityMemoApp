# 모던 메모 - 간단한 Spring Boot CRUD 애플리케이션

Spring Boot (v3.4.4)와 Java 17을 기반으로 구축된 간단한 텍스트 기반 메모 관리 웹 애플리케이션입니다. Spring MVC, 템플릿 엔진으로 Thymeleaf, 데이터베이스 연동을 위해 MyBatis, 데이터베이스로 PostgreSQL을 사용하여 기본적인 CRUD(생성, 읽기, 수정, 삭제) 기능을 구현합니다.

## 주요 기능

* **메모 목록 조회:** 메인 페이지에 모든 기존 메모를 표시합니다.
* **메모 추가:** 인라인 폼을 통해 사용자가 새 메모를 추가할 수 있습니다.
* **메모 수정:** 인라인 편집 기능을 제공하여 기존 메모를 수정할 수 있습니다.
* **메모 삭제:** 확인 메시지와 함께 사용자가 개별 메모를 삭제할 수 있습니다.
* **전체 메모 삭제:** 확인 메시지와 함께 모든 메모를 한 번에 삭제하는 옵션을 제공합니다.
* **플래시 메시지:** `RedirectAttributes`를 사용하여 작업(추가, 수정, 삭제) 수행 후 성공 또는 오류 메시지를 표시합니다.
* **입력값 검증:** 메모 추가 시 메모 텍스트에 대한 기본 검증을 수행합니다.
* **동적 UI:** 간단한 JavaScript를 사용하여 편집 폼 표시/숨김 및 플래시 메시지 페이드 아웃 효과를 구현합니다.

## 기술 스택

* **백엔드:**
    * Java 17
    * Spring Boot 3.4.4 (Web, Validation)
    * MyBatis 3.0.4 (데이터 퍼시스턴스)
* **프론트엔드:**
    * Thymeleaf (서버 사이드 템플릿 엔진)
    * HTML5
    * CSS3 (스타일링은 `<link>` 태그를 통해 적용되는 것으로 가정)
    * JavaScript (기본 DOM 조작)
* **데이터베이스:**
    * PostgreSQL (런타임 의존성 및 설정 확인)
* **빌드 도구:**
    * Gradle
* **개발 도구:**
    * Spring Boot DevTools

## 사전 요구 사항

* **JDK:** Java Development Kit 17 이상
* **빌드 도구:** Gradle 설치 및 환경변수 설정
* **데이터베이스:** PostgreSQL 인스턴스

## 데이터베이스 설정

이 애플리케이션은 `memo`라는 이름의 PostgreSQL 테이블이 필요합니다.

1.  **테이블 생성:** PostgreSQL 데이터베이스에 접속하여 아래 SQL DDL 문을 실행합니다.

    ```sql
    -- PostgreSQL용 memo 테이블 생성 DDL
    CREATE TABLE memo (
        id BIGSERIAL PRIMARY KEY,       -- 자동 증가 기본 키
        text TEXT NOT NULL,             -- 메모 내용
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP -- 생성 타임스탬프 자동 설정 (DB에서 관리)
    );
    ```
    * **참고:** `Memo` 자바 레코드의 `createdAt` 필드는 `String` 타입입니다. 데이터베이스에서 `TIMESTAMP`로 저장된 값을 읽어와 문자열로 다룹니다. 새 메모 추가 시(`Memo.fromText`) 빈 문자열로 초기화되지만, DB의 `DEFAULT CURRENT_TIMESTAMP` 설정에 의해 실제 저장 시 현재 시각이 기록됩니다.

2.  **연결 정보 설정:**
    * **기본 설정 (`src/main/resources/application.yml`):**
        * MyBatis 설정을 확인합니다 (`type-aliases-package`, `map-underscore-to-camel-case`).
        * HikariCP 커넥션 풀 설정을 확인합니다.
        * 기본 활성 프로파일(`spring.profiles.active`)이 `prod`로 설정되어 있습니다.

        ```yaml
        # application.yml 예시 발췌
        spring:
          # ... (application name 등)
          profiles:
            active: prod # 기본 프로파일
          datasource:
            type: com.zaxxer.hikari.HikariDataSource
            hikari:
              maximum-pool-size: 10
              minimum-idle: 5
          mybatis:
            type-aliases-package: org.example.bootsecurity.model.domain
            configuration:
              map-underscore-to-camel-case: true
        logging:
          level:
            org.example.bootsecurity.mapper: DEBUG
        ```

    * **프로덕션 설정 (`src/main/resources/application-prod.yml`):**
        * 실제 운영 환경에서는 이 프로파일이 활성화됩니다 (`prod`가 기본값).
        * 데이터베이스 연결 정보는 **환경 변수**를 통해 주입받습니다. 애플리케이션 실행 환경에서 아래 환경 변수를 설정해야 합니다.
            * `POSTGRES_URL`: PostgreSQL 데이터베이스 접속 URL (예: `jdbc:postgresql://your_host:5432/your_db`)
            * `POSTGRES_USER`: 데이터베이스 사용자명
            * `POSTGRES_PASSWORD`: 데이터베이스 비밀번호

        ```yaml
        # application-prod.yml 예시 발췌
        spring:
          datasource:
            url: ${POSTGRES_URL}
            username: ${POSTGRES_USER}
            password: ${POSTGRES_PASSWORD}
            driver-class-name: org.postgresql.Driver
          # security 설정도 환경 변수 사용 (현재 코드에서는 미적용)
          # security:
          #  user:
          #    name: ${SECURITY_NAME}
          #    password: ${SECURITY_PASSWORD}
        ```

    * **개발 환경 설정 (선택 사항):** 로컬 개발 시에는 `application-dev.yml` 파일을 생성하고 `spring.profiles.active=dev`로 설정하여 별도의 개발용 DB 설정을 사용할 수 있습니다.

## 설치 및 실행

1.  **리포지토리 클론:**
    ```bash
    git clone <your-repository-url>
    cd <repository-directory>
    ```

2.  **프로젝트 빌드:**
    ```bash
    ./gradlew clean build
    ```

3.  **애플리케이션 실행:**
    * **Gradle 사용 (개발 시):** (프로덕션 환경 변수가 설정되지 않은 경우 오류 발생 가능)
        ```bash
        ./gradlew bootRun
        ```
    * **패키징된 JAR 실행 (배포 시):**
        * 먼저 환경 변수(`POSTGRES_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`)를 설정합니다.
        * JAR 파일을 실행합니다.
        ```bash
        # 환경 변수 설정 (Linux/macOS 예시)
        export POSTGRES_URL=jdbc:postgresql://localhost:5432/mydatabase
        export POSTGRES_USER=myuser
        export POSTGRES_PASSWORD=mypassword

        # JAR 실행
        java -jar build/libs/<your-app-name>-<version>.jar
        ```

4.  **애플리케이션 접속:** 웹 브라우저를 열고 `http://localhost:8080` (또는 설정된 포트)으로 이동합니다.

## 사용법

* 메인 페이지 (`/`)에는 현재 메모 목록과 새 메모를 추가하는 폼이 표시됩니다.
* "새 메모 작성" 입력 필드에 텍스트를 입력하고 "등록하기" 버튼을 클릭하여 메모를 추가합니다.
* 각 메모 카드에는 내용, ID, 생성 날짜(DB에서 가져온 문자열)가 표시됩니다.
* 메모 카드에서 "수정" 버튼을 클릭하면 인라인 편집 폼이 나타납니다. 텍스트를 변경하고 "저장"을 클릭합니다. 변경을 취소하려면 "취소"를 클릭합니다.
* 메모 카드에서 "삭제" 버튼을 클릭하면 해당 메모가 삭제됩니다 (확인 창이 나타납니다).
* 상단의 "전체 삭제" 버튼을 클릭하면 모든 메모가 삭제됩니다 (확인 창이 나타납니다).
* 작업 수행 후 성공 또는 오류 메시지가 상단에 잠시 표시됩니다.

## API 엔드포인트

`MainController`는 다음 웹 요청을 처리합니다:

* `GET /`: 메모 목록과 추가 폼이 있는 메인 페이지를 표시합니다.
* `POST /add`: 새 메모 폼 제출을 처리합니다.
* `POST /delete/{id}`: 지정된 `id`의 메모를 삭제합니다.
* `POST /delete-all`: 모든 메모를 삭제합니다.
* `POST /update/{id}`: 지정된 `id`의 메모를 제출된 텍스트로 업데이트합니다.

## 추가 정보

* **보안:** `build.gradle.kts` 및 `application.yml`에 Spring Security 관련 설정 및 의존성 흔적이 있으나, 제공된 `MainController` 코드에는 현재 보안 기능(인증/인가)이 적용되어 있지 않습니다. 실제 운영을 위해서는 보안 강화가 필요합니다.
* **Memo 레코드:** 메모 데이터는 `org.example.bootsecurity.model.domain.Memo` 레코드로 관리됩니다. (`id`, `text`, `createdAt` 필드 포함)

## 라이선스

(선택 사항: 여기에 프로젝트 라이선스를 명시하세요. 예: MIT 라이선스, Apache 2.0 또는 해당 없는 경우 비워두세요)