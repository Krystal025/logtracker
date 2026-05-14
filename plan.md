# Project Plan: LogTracker

## 1. Project Overview

LogTracker analyzes logs, helps identify root causes of errors, and stores resolved **issues** as reusable knowledge.

The goal is NOT just log analysis, but:
→ Converting logs into structured **issue** records  
→ Accumulating resolution history  
→ Reusing past solutions for similar issues  

*(Documentation uses **Issue**. Older drafts may say “incident”; the codebase uses Issue.)*

---

## 2. Core Concept

```
Log → (optional) AI Analysis → Issue → Resolution → Storage → Reuse
```

* Logs are raw input  
* AI analysis is **optional** and **not** tied to creating an issue (see API flow below)  
* User resolves the issue and stores resolution  
* Finalized issue becomes reusable data  

---

## 3. Tech Stack

* Backend: Spring Boot 3.x (Java 17)  
* Persistence: Spring Data JPA, MySQL  
* View: Thymeleaf, Bootstrap 5, minimal vanilla JS (`fetch`)  
* AI: OpenAI API (`OPENAI_API_KEY`, model configurable in `application.yml`)  
* (Future) Vector DB: Pinecone / RAG  

---

## 4. Core Features (MVP)

### 4.1 Project Management (Minimal)

* Create project  
* List projects and open project detail  

### 4.2 Log Analysis (AI)

* Input: raw log text  
* Output (structured): summary, estimated root cause, recommendation, **severity** (LOW / MEDIUM / HIGH / CRITICAL), confidence  
* **Temporary analysis** (`POST /api/analysis`): not persisted  
* **Persist analysis** on an existing issue: `POST /api/analysis/issues/{issueId}` (re-runs analysis from stored raw log, upserts `IssueAnalysis`)  

### 4.3 Issue Creation

* User saves an issue after (optionally) running analysis on the client  
* Required: project, **title**, **raw log**  
* Optional: attach last analysis result in the same request → creates `IssueAnalysis` row  
* If no analysis payload: issue exists **without** analysis (1 : 0..1)  

### 4.4 Issue Storage

* **Issue** row: title, raw log, status, timestamps  
* **IssueAnalysis** (optional): AI fields + severity + confidence  
* **IssueResolution** (optional until user saves): actual cause, action taken  

### 4.5 Similar Issue Search (Phase 1)

* Simple keyword search (LIKE / contains) — not implemented yet  

---

## 5. Future Features

### 5.1 RAG (Phase 2)

* Embeddings, Pinecone, similarity retrieval  

### 5.2 Code Context Integration (Optional)

* GitHub or local files + logs  

---

## 6. System Architecture

```
REST View (Thymeleaf)     REST API (/api/*)
        \                       /
         \                     /
          Controller layer
                 ↓
          Service layer
          ├── AnalysisService (OpenAI)
          ├── IssueService
          └── ProjectService
                 ↓
          Repository (JPA)
                 ↓
               MySQL
```

*(SearchService / LogParseService may be added later.)*

---

## 7. API Design (implemented baseline)

Base path for JSON APIs: **`/api/...`**

### Project

* `POST /api/projects` — create  
* `GET /api/projects` — list  

### Analysis

* `POST /api/analysis` — body: `{ "rawLog": "..." }`  
  * Returns structured result **without** persisting any issue or analysis row  
* `POST /api/analysis/issues/{issueId}` — loads issue’s stored raw log, runs AI, **upserts** `IssueAnalysis`  

### Issue

* `POST /api/issues` — create issue; optional `analysis` in body → persist analysis if present  
* `GET /api/issues` — query: `projectId` (optional), `unresolvedOnly` (optional, default false)  
* `GET /api/issues/{id}` — detail (includes nested analysis/resolution when present)  
* `PATCH /api/issues/{id}/status` — body: `{ "status": "UNRESOLVED" | "RESOLVED" }`  
* `PATCH /api/issues/{id}` — update title / raw log  
* `POST /api/issues/{id}/resolution` — body: actual cause + action taken; creates or updates resolution; sets status **RESOLVED**  

---

## 8. Data Model (as implemented)

Naming: **UPPER_SNAKE** table and column names in DB; JPA maps explicitly.

### Project (`PROJECT`)

* `ID`  
* `NAME`  
* `CREATED_AT`  

### Issue (`ISSUE`)

* `ID`  
* `PROJECT_ID` (FK → Project)  
* `RAW_LOG` (TEXT) — stored on the issue row (**no separate IssueLog table**)  
* `TITLE` (string; plan’s “one-line summary” for the issue)  
* `STATUS` — `UNRESOLVED` | `RESOLVED`  
* `CREATED_AT`  

### IssueAnalysis (`ISSUE_ANALYSIS`) — optional 1 : 1

* `ID`  
* `ISSUE_ID` (unique FK)  
* `SUMMARY`, `ROOT_CAUSE`, `RECOMMENDATION`  
* `SEVERITY` — enum: LOW, MEDIUM, HIGH, CRITICAL  
* `CONFIDENCE`  

### IssueResolution (`ISSUE_RESOLUTION`) — optional 1 : 1

* `ID`  
* `ISSUE_ID` (unique FK)  
* `ACTUAL_CAUSE`  
* `ACTION_TAKEN`  
* `CREATED_AT`  

---

## 9. Analysis vs Issue Save (product flow)

1. User submits raw log to **`POST /api/analysis`** → backend returns JSON only (no DB write for analysis).  
2. Frontend keeps that result in memory (optional).  
3. User clicks **save issue** → **`POST /api/issues`** with title, rawLog, optional `analysis` object.  
4. Backend creates **Issue** always; creates **IssueAnalysis** only if `analysis` is present.  

Reason: issue can exist without AI data; analysis is auxiliary.

---

## 10. UI Flow

```
[Project list / create]
    ↓
[Project detail: log input → Analyze → optional save issue]
    ↓
[Issue detail: log, analysis, resolution, status]
```

---

## 11. UI Principle

* Not chat-based  
* Structured, admin-style report UI  
* Focus on history and records  
* Primary copy: **Korean** for end-user labels; technical/API identifiers stay English where useful  

---

## 12. AI Responsibility

### AI does

* Analyze logs  
* Suggest causes and structured fields (including severity)  

### AI does not

* Auto-learn or change runtime behavior without explicit user actions  

---

## 13. Key Differentiation

* Issue lifecycle, not one-off log paste  
* Optional AI layer on top of durable records  
* Resolution history as knowledge  

---

## 14. Development Priority (updated)

1. Database connection and schema aligned with JPA (`validate`)  
2. Entities: Project, Issue, IssueAnalysis, IssueResolution  
3. Thymeleaf pages (dashboard, project detail, issue detail)  
4. Project + Issue REST + basic SSR  
5. `POST /api/analysis` (non-persisted) + OpenAI wiring  
6. Issue create with optional persisted analysis  
7. Resolution API + issue detail UX  
8. Similar issue search (LIKE)  
9. RAG / Pinecone (future)  

---

## 15. Definition of Done (MVP)

* User can paste logs and get structured AI output  
* User can save an issue with or without analysis  
* Issue list per project with optional “unresolved only” filter  
* User can mark resolved and store resolution text  
* (Stretch) basic keyword search across stored issues  

---

# UI / View Architecture

## Frontend strategy

* SSR with **Thymeleaf**  
* **Bootstrap 5**, minimal **vanilla JS** (`fetch` for analysis, issue save, status, resolution)  
* No full SPA  

---

## Page structure

### 1. Dashboard

* URLs: `/`, `/projects`  
* Template: `dashboard.html`  
* Project table, create project (fetch → `POST /api/projects`)  

### 2. Project detail

* URL: `/projects/{projectId}`  
* Template: `project-detail.html`  
* Left: raw log, title, analyze button, save issue  
* Right: recent issues (newest first), optional “unresolved only” filter  
* Analyze: `POST /api/analysis`; save issue: `POST /api/issues` including optional `analysis` from memory  

### 3. AI analysis result (on project detail)

* Card block: summary, root cause, recommendation, severity, similar issues (placeholder until search exists)  

### 4. Save issue (on project detail)

* Fields aligned with API: **title**, **raw log**; analysis object sent only if user ran analysis  

### 5. Issue detail

* URL: `/issues/{issueId}`  
* Template: **`issue-detail.html`** *(not `incident-detail`)*  
* Sections: original log, AI analysis (if any), resolution form, status controls  

---

## UI design rules

* Bootstrap grid, simple admin look  
* Prefer readability over decoration  
* Tables + cards; responsive when easy  

---

## Rendering strategy

Templates under `src/main/resources/templates/`:

* `layout.html`  
* `dashboard.html`  
* `project-detail.html`  
* `issue-detail.html`  

`ViewController` returns view names; model attributes populated server-side.

---

## API usage from the browser

* SSR for initial HTML  
* `fetch` for: analysis, issue create, status patch, resolution post, content updates as needed  

---

## Appendix: naming cheat sheet

| Concept in prose | API / field name |
|------------------|------------------|
| One-line issue headline | `title` |
| Raw log text | `rawLog` |
| Not yet fixed | `UNRESOLVED` |
| Fixed | `RESOLVED` |
| Old doc “Incident” | **Issue** |
