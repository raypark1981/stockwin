# 프로젝트 디렉토리 구조

## 현재 구조 (2026-04-29 기준)

```
stock-win/
├── docs/                          # 기획·설계 문서
│   ├── 01_project_plan.md         # 전체 기획서
│   ├── 02_mvp_requirements.md     # MVP 요구사항 상세
│   ├── 03_agent_design.md         # 에이전트 설계
│   ├── 04_schedule.md             # 개발 일정
│   ├── 05_google_sheets_schema.md # Google Sheets 컬럼 설계
│   ├── 06_prompt_design.md        # AI 프롬프트 설계
│   └── 07_project_structure.md    # 프로젝트 구조 (이 파일)
├── .env.example                   # 환경변수 예시
├── CLAUDE.md                      # Claude Code 작업 지침
└── README.md                      # 프로젝트 소개
```

---

## 목표 구조 (MVP 완성 시)

```
stock-win/
├── docs/                          # 기획·설계 문서
│
├── agents/                        # 에이전트 모듈
│   ├── news_collector.py          # 에이전트 1: 경제 뉴스 수집
│   ├── stock_analyzer.py          # 에이전트 2: 종목 분석
│   └── notifier.py                # 에이전트 3: 알림 전송
│
├── core/                          # 공통 핵심 로직
│   ├── ai_client.py               # Claude / OpenAI API 클라이언트
│   ├── sheets_client.py           # Google Sheets 연동
│   └── scheduler.py               # 실행 스케줄러 (cron)
│
├── prompts/                       # AI 프롬프트 관리
│   ├── news_summary.txt           # 뉴스 요약 프롬프트
│   ├── sector_inference.txt       # 업종 추론 프롬프트
│   └── stock_extraction.txt       # 관련 종목 추출 프롬프트
│
├── data/                          # 정적 데이터
│   ├── sector_stock_map.json      # 업종 → 종목 매핑 테이블
│   └── watchlist.json             # 관심 종목 목록
│
├── logs/                          # 실행 로그 (gitignore)
│   └── .gitkeep
│
├── tests/                         # 테스트
│   ├── test_news_collector.py
│   ├── test_stock_analyzer.py
│   └── test_notifier.py
│
├── main.py                        # 전체 파이프라인 실행 진입점
├── config.py                      # 환경변수 로드 및 설정값
├── requirements.txt               # Python 패키지 목록
├── .env                           # 실제 환경변수 (gitignore)
├── .env.example                   # 환경변수 예시
├── .gitignore
├── CLAUDE.md
└── README.md
```

---

## 주요 환경변수 (.env)

| 변수명 | 용도 |
|---|---|
| `ANTHROPIC_API_KEY` | Claude API 키 |
| `OPENAI_API_KEY` | OpenAI API 키 (선택) |
| `GOOGLE_SHEETS_ID` | 저장용 스프레드시트 ID |
| `GOOGLE_SERVICE_ACCOUNT_JSON` | Google 서비스 계정 인증 파일 경로 |
| `TELEGRAM_BOT_TOKEN` | 텔레그램 봇 토큰 |
| `TELEGRAM_CHAT_ID` | 알림 수신 채팅 ID |
| `NEWS_API_KEY` | 뉴스 수집 API 키 (선택) |

---

## 실행 흐름

```
main.py
  └─ scheduler.py         # 시간대별 스케줄 실행
       ├─ news_collector.py   → 뉴스 수집
       ├─ stock_analyzer.py   → AI 분석 (ai_client.py + prompts/)
       ├─ sheets_client.py    → Google Sheets 저장
       └─ notifier.py         → 텔레그램 알림 전송
```

---

## 개발 단계별 구조 변화

| 단계 | 추가되는 구조 |
|---|---|
| 1단계 (현재) | docs/, .env.example |
| MVP | agents/, core/, prompts/, data/, main.py |
| 2단계 | frontend/ (Next.js), backend/ (API 서버), db/ |
| 3단계 | trading/ (증권사 API 연동, 자동매매 로직) |
