# 🏠 HomeSweetHome

> 숙소 예약부터 일정·체크인 관리, 게스트와의 실시간 소통까지
> 숙박 운영에 필요한 모든 기능을 하나로 담은 통합 관리 플랫폼
> 
## 📌 프로젝트 개요  
**HomeSweetHome**은 숙소 호스트와 게스트 간의 예약 및 일정 관리 과정을 간소화하기 위해 제작된 웹 플랫폼이다.  
호스트는 예약 현황과 체크인 일정을 한눈에 관리하고, 게스트와 실시간 채팅을 통해 소통할 수 있다.  
달력 기반의 일정 관리와 메모 기능으로 효율적인 숙소 운영을 돕는 것을 목표로 한다.

---

## ⚙️ 주요 기능
- **회원 관리** : 회원가입 / 로그인 / 권한(호스트·게스트) 구분  
- **숙소 관리** : 숙소 등록, 수정, 삭제  
- **예약 관리** : 예약 일정 확인, 체크인·체크아웃 표시  
- **채팅 기능** : WebSocket(STOMP) 기반 실시간 1:1 채팅  
- **달력 관리** : FullCalendar 기반 예약 일정·메모 표시  
- **마이페이지** : 사용자 정보, 예약 내역, 일정 관리 통합  
- **알림 기능** : 새로운 채팅 메시지, 예약 변경 시 실시간 알림  

## 🛠 기술 스택

| 구분 | 사용 기술 |
|------|------------|
| **Frontend** | HTML, CSS, JavaScript, Thymeleaf, Bootstrap 5, FullCalendar.js |
| **Backend** | Spring Boot, Spring Security, WebSocket(STOMP), JPA / MyBatis |
| **DB** | MySQL |
| **Tools** | GitHub, Figma, Eclipse |
| **Design** | [Figma 디자인 바로가기](https://www.figma.com/design/YSjc45WehXFYvqh77Dk7ej/HomeSweetHome) |

---

## 🚀 향후 계획
- 예약 통계 대시보드 구현  
- 다국어(영·일·중) 지원  
