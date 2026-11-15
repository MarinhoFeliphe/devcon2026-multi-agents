# Agentic Systems: How Liferay AI Hub Coordinates Many Agents

Resources for the Liferay DEVCON 2026 London session **"Agentic Systems: How Liferay AI Hub Coordinates Many Agents"** by Feliphe Marinho.

- **Video:** [AGENTIC SYSTEMS: HOW LIFERAY AI HUB COORDINATES MANY AGENTS | Liferay Devcon 2026 London](https://www.youtube.com/watch?v=8mRK023vCZg) (Liferay on YouTube)
- **Slides:** [Agentic Systems: How Liferay AI Hub Coordinates Many Agents (PDF)](slides/agentic-systems-how-liferay-ai-hub-coordinates-many-agents.pdf)

This repository holds the resources behind the workflow pattern demos from the talk: **Sequential**, **Conditional**, **Loop**, and **Parallel**. Each one is a Liferay workflow made of AI Hub Agent nodes. An object entry starts it, and a Spring Boot client extension records each agent's output.

> [!IMPORTANT]
> **This is a reference repository, not a runnable project.** It is not meant to be compiled, built, deployed, or run as it is. The files show what the resources of the demos look like (agent definitions, workflow definitions, object definitions, and client extension code) so you can read them alongside the talk. They were extracted from the Liferay Portal source tree where the demos were built, and depend on that environment: there is no Gradle wrapper or settings, and the build files point at paths outside this repository.

The resources are split in two folders:

| Folder | Side | What It Holds |
| --- | --- | --- |
| [`ai-hub-side/`](ai-hub-side) | **AI Hub side** | The AI Hub agents: their agent definitions (agent cards) and the LLM workflow behind each one |
| [`client-side/`](client-side) | **Client side** | What a Liferay project builds on top of AI Hub: the objects, the pattern workflows that call the agents, and the client extensions that start and drive them |

## Contents

- [Talk Agenda](#talk-agenda)
- [Architecture](#architecture)
- [Repository Layout](#repository-layout)
- [Workflow Patterns](#workflow-patterns)
  - [Sequential: Novel Creator](#sequential-novel-creator)
  - [Conditional: Expert Router](#conditional-expert-router)
  - [Loop: Styled Writer](#loop-styled-writer)
  - [Parallel: Evening Planner](#parallel-evening-planner)
- [Pure Agentic AI: Supervisor](#pure-agentic-ai-supervisor)
- [AI Hub Agents](#ai-hub-agents)
- [How the Two Sides Fit Together](#how-the-two-sides-fit-together)
- [Commit History](#commit-history)

## Talk Agenda

| # | Section | What It Covers | Code in This Repository |
| --- | --- | --- | --- |
| 01 | Agentic Systems | Workflow patterns vs pure agentic AI | — |
| 02 | AI Hub Agent | Agent Builder: the agent card and how an agent runs | The 10 agent definitions (see [AI Hub Agents](#ai-hub-agents)) |
| 03 | Workflow Nodes | LLM, AI Decision, AI Hub Agent, and HTTP Request | The **AI Hub Agent** node (`ai-hub-agent`) is used in every pattern workflow, and each agent is a workflow with one **LLM** node (`llm`). AI Decision and HTTP Request are covered in the talk only. |
| 04 | Workflow Patterns | Sequential, Conditional, Loop, and Parallel | Yes, all four |
| 05 | Pure Agentic AI | Supervisor (planner) agent | Demonstrated in the talk only |

The talk compares two ways to combine agents:

- **Workflows**: the developer decides the order. Agents run in a fixed shape: one after another, by a rule, in a loop, or side by side. This is predictable, testable, and auditable.
- **Pure agentic AI**: an LLM decides the order. A supervisor reads the request, plans, calls agents, and stops when the goal is met. This handles open-ended requests.

## Architecture

Each pattern uses the same four steps:

```text
 1. A user adds an object entry (Novel, ExpertRequest, StyledStory, EveningPlan)
        │  onAfterAdd object action (objectAction client extension)
        ▼
 2. The Spring Boot client extension starts the matching workflow
    (POST /o/headless-admin-workflow/v1.0/workflow-instances/submit)
        │
        ▼
 3. The workflow runs: ai-hub-agent node ──► task node whose onAssignment
    action is a workflowAction client extension
        │
        ▼
 4. The client extension saves the agent output as a TaskResult entry,
    linked to the starting entry, then completes the task through a transition.
    The transition name is chosen by the code for Conditional and Loop.
```

The shared building blocks were added in the **BASE** commit:

- The [`TaskResult`](client-side/client-extensions/liferay-aihub-multi-agents-batch/batch/object-definition.batch-engine-data.json) object (`assignee` holds the agent ERC, `output` holds the agent's answer), inside the `AgentWorkflows` [object folder](client-side/client-extensions/liferay-aihub-multi-agents-batch/batch/00-object-folder.batch-engine-data.json).
- [`WorkflowService`](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/src/main/java/com/liferay/ai/hub/multi/agents/service/WorkflowService.java): looks up the latest version of a workflow definition by name, starts an instance with a context, and completes a workflow task through its `transitionURL`.
- [`TaskResultService`](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/src/main/java/com/liferay/ai/hub/multi/agents/service/TaskResultService.java): adds a `TaskResult`, reads the latest one for an agent, and counts them per agent (the Loop pattern uses the count).

## Repository Layout

Everything the client side contains is under `client-side/`, and everything the AI Hub side contains is under `ai-hub-side/`. The commit history follows the same split (see [Commit History](#commit-history)).

| Folder | Where It Lives in the Liferay Portal Source |
| --- | --- |
| `client-side/client-extensions/` | `workspaces/liferay-aihub-workspace/client-extensions/` |
| `ai-hub-side/site-initializer/` | `workspaces/liferay-aihub-workspace/modules/dxp/apps/ai-hub/ai-hub-site-initializer/src/main/resources/site-initializer/` |

```text
client-side/
└── client-extensions/
    ├── liferay-aihub-multi-agents-batch/                 # batch CET: objects + workflow definitions
    │   ├── client-extension.yaml
    │   └── batch/
    │       ├── 00-object-folder.batch-engine-data.json
    │       ├── object-definition.batch-engine-data.json   # TaskResult, Novel, ExpertRequest, StyledStory, EveningPlan
    │       └── workflow-definition.batch-engine-data.json # Novel Creator, Expert Router, Styled Writer, Evening Planner
    └── liferay-aihub-multi-agents-etc-spring-boot/       # microservice CET: object and workflow actions
        ├── client-extension.yaml
        ├── build.gradle, Dockerfile, LCP.json
        └── src/main/
            ├── java/com/liferay/ai/hub/multi/agents/
            │   ├── ObjectAction*RestController.java       # start a workflow from an object entry
            │   ├── WorkflowAction*RestController.java     # save agent output, pick the transition
            │   └── service/{WorkflowService,TaskResultService}.java
            └── resources/application*.properties

ai-hub-side/
└── site-initializer/
    ├── object-entries/
    │   └── agent-definition-object-entries.json       # the 10 agent definitions (agent cards)
    └── workflow-definitions/
        └── <agent>-workflow-definition/               # one per agent: an LLM node with its prompt
            ├── workflow-definition.json
            └── workflow-definition.xml
```

| Client Extension | Type | Purpose |
| --- | --- | --- |
| [`liferay-aihub-multi-agents-batch`](client-side/client-extensions/liferay-aihub-multi-agents-batch/client-extension.yaml) | `batch` + `oAuthApplicationHeadlessServer` | Imports the object folder, the object definitions (with their object actions and relationships), and the four workflow definitions |
| [`liferay-aihub-multi-agents-etc-spring-boot`](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/client-extension.yaml) | `objectAction`, `workflowAction`, OAuth (headless server and user agent) | Spring Boot service on port `58082` that starts workflows and handles the workflow actions |

## Workflow Patterns

Paths below are relative to `client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/src/main/java/com/liferay/ai/hub/multi/agents/`. All four workflow definitions are in [`workflow-definition.batch-engine-data.json`](client-side/client-extensions/liferay-aihub-multi-agents-batch/batch/workflow-definition.batch-engine-data.json), and all object definitions are in [`object-definition.batch-engine-data.json`](client-side/client-extensions/liferay-aihub-multi-agents-batch/batch/object-definition.batch-engine-data.json).

### Sequential: Novel Creator

> *One prompt can't do every job well.*

```text
start ─► Creative Writer ─► Audience Editor ─► Style Editor ─► end
```

| Agent | Inputs | Output |
| --- | --- | --- |
| Creative Writer (`L_CREATIVE_WRITER`) | topic | story |
| Audience Editor (`L_AUDIENCE_EDITOR`) | audience, story | story |
| Style Editor (`L_STYLE_EDITOR`) | story, style | story |

- **Object:** `Novel` (`topic`, `audience`, `style`, `content`). Its `startNovelCreator` action runs on `onAfterAdd`.
- **Starts the workflow:** [`ObjectActionNovelCreatorRestController`](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/src/main/java/com/liferay/ai/hub/multi/agents/ObjectActionNovelCreatorRestController.java) starts `Novel Creator` with `topic`, `audience`, `style`, and the novel ERC.
- **After each agent:** [`WorkflowActionTaskResultRestController`](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/src/main/java/com/liferay/ai/hub/multi/agents/WorkflowActionTaskResultRestController.java) saves the output as a `TaskResult` linked to the novel and takes the only transition.

### Conditional: Expert Router

> *A generalist can't be an expert in everything.*

```text
start ─► Request Analyst ─┬─► Medical Expert ─┐
                          ├─► Legal Expert   ─┼─► end
                          └─► Art Expert     ─┘
```

| Agent | Inputs | Output |
| --- | --- | --- |
| Request Analyst (`L_REQUEST_ANALYST`) | request | expertise |
| Medical Expert (`L_MEDICAL_EXPERT`) | request | answer |
| Legal Expert (`L_LEGAL_EXPERT`) | request | answer |
| Art Expert (`L_ART_EXPERT`) | request | answer |

- **Object:** `ExpertRequest` (`request`). Its `startExpertRouter` action runs on `onAfterAdd`.
- **Starts the workflow:** [`ObjectActionExpertRouterRestController`](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/src/main/java/com/liferay/ai/hub/multi/agents/ObjectActionExpertRouterRestController.java).
- **Routing:** [`WorkflowActionExpertRequestTaskResultRestController`](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/src/main/java/com/liferay/ai/hub/multi/agents/WorkflowActionExpertRequestTaskResultRestController.java). The Request Analyst answers with one word, `medical`, `legal`, or `art`, which is also the name of the transition to that expert. The controller matches that output against `nextTransitionNames`, ignoring case. An answer that matches no transition fails the task instead of guessing.

### Loop: Styled Writer

> *The first draft is rarely good enough.*

```text
start ─► Creative Writer ─► Style Scorer ─┬─► end          (score ≥ 0.8 or 5 iterations)
                              ▲           └─► Style Editor ─┐
                              └─────────────────────────────┘
```

| Agent | Inputs | Output |
| --- | --- | --- |
| Creative Writer (`L_CREATIVE_WRITER`) | topic | story |
| Style Scorer (`L_STYLE_SCORER`) | story, style | score |
| Style Editor (`L_STYLE_EDITOR`) | story, style | story |

- **Object:** `StyledStory` (`topic`, `style`). Its `startStyledWriter` action runs on `onAfterAdd`.
- **Starts the workflow:** [`ObjectActionStyledWriterRestController`](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/src/main/java/com/liferay/ai/hub/multi/agents/ObjectActionStyledWriterRestController.java).
- **Loop exit:** [`WorkflowActionStyleReviewLoopRestController`](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/src/main/java/com/liferay/ai/hub/multi/agents/WorkflowActionStyleReviewLoopRestController.java) reads the score as a number. It takes `end` when the score is at least `0.8`, or when the Style Scorer has run `5` times (counted from its `TaskResult` entries). Otherwise it takes `styleEditor`.
- **Writer and editor outputs:** [`WorkflowActionStyledStoryTaskResultRestController`](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/src/main/java/com/liferay/ai/hub/multi/agents/WorkflowActionStyledStoryTaskResultRestController.java).

### Parallel: Evening Planner

> *Independent work shouldn't wait in line.*

```text
start ─► fork ─┬─► Food Expert  ─┬─► join ─► Combine Evening Plans ─► end
               └─► Movie Expert ─┘
```

| Agent | Inputs | Output |
| --- | --- | --- |
| Food Expert (`L_FOOD_EXPERT`) | mood | meals |
| Movie Expert (`L_MOVIE_EXPERT`) | mood | movies |

- **Object:** `EveningPlan` (`mood`). Its `startEveningPlanner` action runs on `onAfterAdd`.
- **Starts the workflow:** [`ObjectActionEveningPlannerRestController`](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/src/main/java/com/liferay/ai/hub/multi/agents/ObjectActionEveningPlannerRestController.java).
- **Branch outputs:** [`WorkflowActionEveningPlanTaskResultRestController`](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/src/main/java/com/liferay/ai/hub/multi/agents/WorkflowActionEveningPlanTaskResultRestController.java).
- **Combining after the join:** [`WorkflowActionEveningPlanCombinerRestController`](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/src/main/java/com/liferay/ai/hub/multi/agents/WorkflowActionEveningPlanCombinerRestController.java). Both branches write the same output key of the workflow context, so the combiner reads each branch's latest `TaskResult` instead. It reads the `<p>` paragraphs of each stored output, pairs them one to one ("1. Movie: …, Meal: …") and stores the result as a `TaskResult` assigned to `EVENING_PLANNER`.

## Pure Agentic AI: Supervisor

> *Some requests don't follow a diagram.*

The last part of the talk swaps the fixed workflow for a **Supervisor**, also called a planner agent. It receives the agent cards of the same agents used above and plans which agents to call. Its output is the plan result. This part was a live demonstration in AI Hub and has no code in this repository.

## AI Hub Agents

The pattern workflows call AI Hub agents by external reference code, in the `agent-definition-external-reference-code` element of each `ai-hub-agent` node. The agents are defined on the AI Hub side, in its site initializer:

- **Agent definitions (agent cards):** [`agent-definition-object-entries.json`](ai-hub-side/site-initializer/object-entries/agent-definition-object-entries.json). Each entry holds the agent's ERC, title, description, input variables, output variable, and the name of the workflow that runs it. The AI Hub site initializer has more agents in this file, so this repository includes only the 10 used in the talk.
- **Agent workflows:** [`workflow-definitions/`](ai-hub-side/site-initializer/workflow-definitions). Each agent is a workflow with a single **LLM** node, which holds the agent's prompt, user message, and input and output variables.

| Agent | ERC | Inputs | Output | Used By |
| --- | --- | --- | --- | --- |
| [Art Expert](ai-hub-side/site-initializer/workflow-definitions/art-expert-workflow-definition/workflow-definition.xml) | `L_ART_EXPERT` | request | answer | Conditional |
| [Audience Editor](ai-hub-side/site-initializer/workflow-definitions/audience-editor-workflow-definition/workflow-definition.xml) | `L_AUDIENCE_EDITOR` | audience, story | story | Sequential |
| [Creative Writer](ai-hub-side/site-initializer/workflow-definitions/creative-writer-workflow-definition/workflow-definition.xml) | `L_CREATIVE_WRITER` | topic | story | Sequential, Loop |
| [Food Expert](ai-hub-side/site-initializer/workflow-definitions/food-expert-workflow-definition/workflow-definition.xml) | `L_FOOD_EXPERT` | mood | meals | Parallel |
| [Legal Expert](ai-hub-side/site-initializer/workflow-definitions/legal-expert-workflow-definition/workflow-definition.xml) | `L_LEGAL_EXPERT` | request | answer | Conditional |
| [Medical Expert](ai-hub-side/site-initializer/workflow-definitions/medical-expert-workflow-definition/workflow-definition.xml) | `L_MEDICAL_EXPERT` | request | answer | Conditional |
| [Movie Expert](ai-hub-side/site-initializer/workflow-definitions/movie-expert-workflow-definition/workflow-definition.xml) | `L_MOVIE_EXPERT` | mood | movies | Parallel |
| [Request Analyst](ai-hub-side/site-initializer/workflow-definitions/request-analyst-workflow-definition/workflow-definition.xml) | `L_REQUEST_ANALYST` | request | expertise | Conditional |
| [Style Editor](ai-hub-side/site-initializer/workflow-definitions/style-editor-workflow-definition/workflow-definition.xml) | `L_STYLE_EDITOR` | story, style | story | Sequential, Loop |
| [Style Scorer](ai-hub-side/site-initializer/workflow-definitions/style-scorer-workflow-definition/workflow-definition.xml) | `L_STYLE_SCORER` | story, style | score | Loop |

A few agents have to answer in an exact format, because the client extension parses their output:

- **Request Analyst** answers with only `medical`, `legal`, or `art`. That word is the transition the Conditional pattern takes.
- **Style Scorer** answers with only a score from `0.0` to `1.0`. The Loop pattern reads it as a number.
- **Food Expert** and **Movie Expert** answer with 3 items, one per line. The Parallel combiner pairs them one to one.

## How the Two Sides Fit Together

This section describes how the resources work together in the environment they come from. It is not a setup guide: the repository is not meant to be run (see the note at the top).

**AI Hub side.** The AI Hub site initializer creates the agents. Each [agent definition](ai-hub-side/site-initializer/object-entries/agent-definition-object-entries.json) names the workflow that runs it, and each [agent workflow](ai-hub-side/site-initializer/workflow-definitions) is a single LLM node holding the prompt.

**Client side.** Two client extensions build the demos on top of those agents:

- The [batch client extension](client-side/client-extensions/liferay-aihub-multi-agents-batch/client-extension.yaml) imports the objects, their object actions, and the four pattern workflows. Each `ai-hub-agent` node in those workflows calls an agent by its ERC, so the ERCs must match the AI Hub side.
- The [Spring Boot client extension](client-side/client-extensions/liferay-aihub-multi-agents-etc-spring-boot/client-extension.yaml) declares the object actions and workflow actions. In the demo environment it ran on port `58082` as `liferay-aihub-multi-agents-etc-spring-boot`, and called back into DXP with its own OAuth application.

**At runtime.** Adding an entry to `Novel`, `ExpertRequest`, `StyledStory`, or `EveningPlan` starts the matching workflow. The agents answer one after another, by a rule, in a loop, or side by side, and each answer is stored as a `TaskResult` related to that entry.

## Commit History

Each pattern has two commits: the AI Hub agents it needs (`ai-hub-side`, touching only [`ai-hub-side/`](ai-hub-side)), then the workflow and client extension code that calls them (`client-side`, touching only [`client-side/`](client-side)). Follow them in order to walk through the talk one step at a time. Each commit message explains what it adds and why.

| Commit | Adds |
| --- | --- |
| **BASE** - client-side | Batch and Spring Boot client extensions, the `TaskResult` object, `WorkflowService`, and `TaskResultService` |
| **SEQUENTIAL** - ai-hub-side | Creative Writer, Audience Editor, and Style Editor agents |
| **SEQUENTIAL** - client-side | Novel Creator workflow, `Novel` object, object action, and task result workflow action |
| **CONDITIONAL** - ai-hub-side | Request Analyst, Medical Expert, Legal Expert, and Art Expert agents |
| **CONDITIONAL** - client-side | Expert Router workflow, `ExpertRequest` object, and routing workflow action |
| **LOOP** - ai-hub-side | Style Scorer agent |
| **LOOP** - client-side | Styled Writer workflow, `StyledStory` object, and style review loop workflow action |
| **PARALLEL** - ai-hub-side | Food Expert and Movie Expert agents |
| **PARALLEL** - client-side | Evening Planner workflow, `EveningPlan` object, and combiner workflow action |
| **DOCS** | The slides and this README |

```bash
git log --reverse
```
