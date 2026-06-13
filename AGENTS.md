# Ask AI Mod — Agent Guide

## Project

Minecraft Fabric mod (client-side, **1.21.11**) that adds an AI chat assistant and command block helper.

## Current state

Greenfield — only docs exist. No build files, no source.

## Toolchain

| Item | Value |
|---|---|
| Build system | Fabric Loom `1.14-SNAPSHOT` (`net.fabricmc.fabric-loom-remap`) |
| Loader | `0.18.2` |
| Fabric API | `0.139.4+1.21.11` |
| Mappings | Mojang official (`loom.officialMojangMappings()`) |
| Java | 21, `options.release = 21` |
| Gradle | 9.2.1 |
| Cloth Config | `21.11.153` |
| Mod Menu | `17.0.0-beta.1` |
| HTTP | JDK `java.net.http.HttpClient` (依存追加不要) |
| JSON | Gson (Fabric API に同梱) |

- Fabric API は依存に含めるが **コード内で直接参照しない** (Mixin・Screen・HTTP・JSONは全てバニラ範囲)

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
