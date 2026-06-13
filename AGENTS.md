# Ask AI Mod — Agent Guide

## Project

Minecraft Fabric mod (client-side, 1.21.x) that adds an AI chat assistant and command block helper.

## Current state

Greenfield — only `REQUIREMENTS.md` exists. No build files, no source, no CI.

## Toolchain

- **Build system**: Fabric Loom (Gradle) — project needs `fabric-loom` gradle plugin and Fabric mappings
- **Required deps**: Fabric API, Mod Menu, Cloth Config, OkHttp or Java HttpClient
- **Java target**: Java 21 (Minecraft 1.21.x)

## Architecture (from REQUIREMENTS.md)

- Dedicated AI chat Screen (not vanilla chat) with clickable code blocks
- `CommandBlockScreen` Mixin injects an AI button
- OpenAI-compatible API (`/v1/chat/completions`), async, no streaming
- Settings via Mod Menu + Cloth Config
- Commands are **never auto-executed** — always user-initiated via `openChatScreen()`
- System prompt dynamically populated with player context (gamemode, world type, op status)

## Key constraints

| Rule | Why |
|------|-----|
| No server-side code | Client-side only mod |
| No streaming in v1 | Explicitly excluded |
| No auto-execute | All command runs go through user Enter |
| API calls async | Don't block the render thread |

## Commands (will need)

```bash
./gradlew build          # build mod jar
./gradlew runClient      # launch Minecraft with mod
./gradlew test           # run tests
```
