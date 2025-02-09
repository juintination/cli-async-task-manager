### 2주차 과제 내용

CLI 프로그램 제작 (비동기 프로그램)

1. [1주차 과제로 만든 CLI 프로그램](https://github.com/juintination/cli-task-manager)을 비동기 프로그램으로 변경
    1. 간단한 스레드 구현(예시: 시간 흐름, 날씨 변화, 음악 플레이 등)
    2. 스레드간 상호작용할 수 있는 기능 구현(예시: 사람 스레드와 몹 스레드가 싸우는 게임)

### 프로그램 설명
- CLI 기반의 간단한 작업 관리 프로그램
    - 사용자는 해야 할 작업을 관리할 수 있다.
    - 사용자의 모든 입력은 시간과 함께 로그로 남는다.
    - 동시에 여러 사용자가 사용할 수 있다.

### 실행 영상

- 로그 작성 기능

[![Video Label](http://img.youtube.com/vi/BpECtHcu3rc/0.jpg)](https://youtu.be/BpECtHcu3rc)

- 같은 사용자가 동시에 실행할 경우

[![Video Label](http://img.youtube.com/vi/bdchk1bOlSM/0.jpg)](https://youtu.be/bdchk1bOlSM)

### 구현할 기능 목록
- 프로그램 비동기 방식 적용
  - AsyncTaskManager 및 AsyncFileManager 구현 
- 로그 작성 기능
  - 로그 작성 스레드 구현 및 적용
- 동시에 여러 사용자가 사용할 수 있는 기능
  - Application에서 AsyncTaskManager를 스레드로 실행

### 개발 환경
- IDE: IntelliJ
- 언어: JAVA21
