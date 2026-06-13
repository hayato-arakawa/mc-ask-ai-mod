# Ask AI Mod — Implementation Plan

## 1. Gradle project skeleton

- Fabric Loom plugin, Java 21, 1.21.x Fabric mappings
- `build.gradle` with Fabric API, Mod Menu, Cloth Config, OkHttp dependencies
- Gradle wrapper

## 2. Mod metadata

- `fabric.mod.json` — client-side only, mod ID `ask-ai-mod`
- Main entrypoint class

## 3. Config (Cloth Config + Mod Menu)

- Fields: endpoint, API key, model
- Mod Menu integration screen
- `ConfigManager` for load/save

## 4. API client

- Async HTTP call to `POST /v1/chat/completions`
- JSON request/response (no streaming)
- OpenAI-compatible (works with any compatible backend)

## 5. System prompt builder

- Dynamic context: gamemode, singleplayer/multiplayer, op status
- Sourced from `MinecraftClient.getInstance()`

## 6. AI Chat Screen

- Dedicated screen (not vanilla chat)
- Text input field + scrollable response area
- Send button triggers async API call

## 7. Clickable code blocks

- Regex extract code blocks (`` ``` ``, `` ` ``, `/`-prefixed lines) from response
- Render as clickable components
- Click → `openChatScreen(command)` (user presses Enter to run)

## 8. CommandBlockScreen Mixin

- Inject AI button into `CommandBlockScreen`
- Button sends current command block content as context to AI
- Response fills the command field after user confirmation

## 9. Build verification

- `./gradlew build` compiles
- No server-side classes loaded
