# Project Plan: LogTracker

## 1. Project Overview

LogTracker is a system that analyzes logs, helps identify root causes of errors, and stores resolved issues as reusable knowledge.

The goal is NOT just log analysis, but:
→ Converting logs into structured issue records
→ Accumulating resolution history
→ Reusing past solutions for similar issues

---

## 2. Core Concept

```
Log → Analysis → Issue → Resolution → Storage → Reuse
```

* Logs are raw input
* AI provides initial analysis
* User resolves the issue
* Finalized issue becomes reusable data

---

## 3. Tech Stack

* Backend: Spring Boot (Java)
* Database: MySQL
* AI: OpenAI API (or similar)
* (Future) Vector DB: Pinecone

---

## 4. Core Features (MVP)

### 4.1 Project Management (Minimal)

* Create Project
* Select Project

### 4.2 Log Analysis

* Input: raw log text
* AI analyzes:

  * summary
  * suspected cause
  * key evidence
  * recommended actions

### 4.3 Issue Creation

* User clicks "Save Report"
* Inputs:

  * actual cause
  * action taken

### 4.4 Issue Storage

* Store:

  * raw log
  * AI analysis
  * user resolution

### 4.5 Similar Issue Search (Phase 1)

* Simple keyword search (LIKE / contains)

---

## 5. Future Features

### 5.1 RAG (Phase 2)

* Store embeddings
* Use Pinecone
* Retrieve similar issues by vector similarity

### 5.2 Code Context Integration (Optional)

* Read GitHub or local files
* Combine logs + code for better analysis

---

## 6. System Architecture

```
Controller
  ↓
Service Layer
  ├── LogParseService
  ├── AnalysisService (AI)
  ├── IssueService
  └── SearchService
  ↓
Repository (JPA)
  ↓
MySQL
```

---

## 7. API Design (Basic)

### Project

* POST /api/projects
* GET /api/projects

### Analysis

* POST /api/analysis

  * input: log text
  * output: AI analysis result

### Issue

* POST /issues
* GET /issues?projectId=
* GET /issues/{id}

---

## 8. Data Model

### Project

* id
* name
* createdAt

### Issue

* id
* projectId
* summary
* status (OPEN / RESOLVED)
* createdAt

### IssueLog

* id
* issueId
* rawLog

### IssueAnalysis

* id
* issueId
* summary
* rootCause
* recommendation
* confidence

### IssueResolution

* id
* issueId
* actualCause
* actionTaken
* createdAt

---

## 9. UI Flow

```
[Project Selection]
    ↓
[Log Input]
    ↓
[AI Analysis Result (Structured View)]
    ↓
[Save Report Button]
    ↓
[User Inputs Resolution]
    ↓
[Final issue Stored]
```

---

## 10. UI Principle

* NOT chat-based
* Structured report UI
* Focus on history and records

---

## 11. AI Responsibility

### AI does:

* Analyze logs
* Suggest possible causes
* Structure final report

### AI does NOT:

* Auto-learn
* Modify system behavior

---

## 12. Key Differentiation

* Not just log analysis
* Issue-based lifecycle system
* Knowledge accumulation
* Reusable debugging history

---

## 13. Development Priority

### Step 1 (Must)

* Project + Issue CRUD
* Log input
* AI analysis

### Step 2

* Resolution input
* Issue list UI

### Step 3

* Simple search (LIKE)

### Step 4 (Optional)

* RAG (Pinecone)

---

## 14. Definition of Done (MVP)

* User can input logs
* AI returns structured analysis
* User can save issue
* Issue list is viewable per project
* Past issues can be searched (basic)

---

# UI / View Architecture

## Frontend Strategy

This project uses Server-Side Rendering (SSR) with Thymeleaf.

Reason:
- Faster MVP development
- Simple admin-style UI
- Tight integration with Spring Boot
- Easier backend-focused portfolio development

Tech Stack:
- Thymeleaf
- Bootstrap 5
- Vanilla JavaScript (minimal usage)

---

# Page Structure

## 1. Dashboard Page

URL:
- `/`
- `/projects`

Purpose:
- Show project list
- Create new project
- Navigate to project detail page

UI Components:
- Project table
- Create project button
- Search input (optional)

---

## 2. Project Detail Page

URL:
- `/projects/{projectId}`

Purpose:
- Analyze logs for a selected project
- View issue history

Layout:
- Left: log input form
- Right: recent issue list

UI Components:
- Large textarea for log input
- Analyze button
- Issue summary cards

---

## 3. AI Analysis Result Section

Displayed after log analysis.

Fields:
- Summary
- Estimated Root Cause
- Recommended Action
- Similar Past issues

UI Style:
- Card-based layout
- Highlight severity level

---

## 4. Save issue Modal/Form

Purpose:
- Save resolved issue knowledge

Fields:
- Title
- Log content
- Root cause
- Resolution
- Memo

---

## 5. issue Detail Page

URL:
- `/issues/{issueId}`

Purpose:
- Show full issue report

Sections:
- Original log
- AI analysis result
- Final resolution
- Similar issues

---

# UI Design Rules

- Use Bootstrap grid layout
- Use simple admin dashboard style
- Avoid overly modern or animated UI
- Prioritize readability
- Use tables and cards
- Responsive layout preferred

---

# Rendering Strategy

Use Thymeleaf templates:
- layout.html
- dashboard.html
- project-detail.html
- issue-detail.html

Use Controller + Model pattern.

Example:
- Controller returns Thymeleaf view
- Backend directly injects model data into template

---

# API Usage

Most pages use SSR rendering.

Use fetch API only for:
- AI analyze request
- Issue save request
- Async updates

Do NOT build a full SPA frontend.

---

# Development Priority

1. Database connection
2. Entity structure
3. Basic Thymeleaf pages
4. Project CRUD
5. Issue CRUD
6. Mock AI analysis
7. OpenAI integration
8. Similar issue search
9. RAG/Pinecone upgrade (future)