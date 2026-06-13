# Research Results

## 1. Fabric Loom + Gradle setup (1.21.x)

| Item | Value |
|---|---|
| Minecraft | 1.21.1 (推奨) |
| Loom plugin ID | `net.fabricmc.fabric-loom-remap` (1.21.x は難読化版なのでremap必要) |
| Loom version | `1.16-SNAPSHOT` (最新の example mod 1.21 ブランチ) |
| Loader version | `0.19.2` |
| Fabric API | `0.102.0+1.21` |
| Mappings | `loom.officialMojangMappings()` (Mojang公式推奨。Yarnは1.21.11で終了) |
| Java | 21, `options.release = 21` |
| Gradle | 9.2.1 (`gradle-wrapper.properties`) |
| 公式テンプレート | https://fabricmc.net/develop/template/ |
| 公式example | https://github.com/FabricMC/fabric-example-mod/tree/1.21 |

## 2. Cloth Config + Mod Menu

| Dependency | Maven coordinate | Version (1.21.1) |
|---|---|---|
| Cloth Config | `me.shedaniel.cloth:cloth-config-fabric` | `15.0.130+fabric` |
| Mod Menu | `com.terraformersmc:modmenu` | `11.0.3` (推定, 要確認) |

**セットアップ:**

```groovy
repositories {
    maven { url "https://maven.shedaniel.me/" }
    maven { url "https://maven.terraformersmc.com/releases/" }
}

dependencies {
    modApi("me.shedaniel.cloth:cloth-config-fabric:${cloth_config_version}") {
        exclude(group: "net.fabricmc.fabric-api")
    }
    modCompileOnly("com.terraformersmc:modmenu:${modmenu_version}") {
        transitive(false)
    }
}
```

**ModMenu API:**
- インターフェース: `com.terraformersmc.modmenu.api.ModMenuApi`
- メソッド: `getModConfigScreenFactory()` → `ConfigScreenFactory<?>`
- entrypoint名: `"modmenu"` in `fabric.mod.json`
- ConfigScreenFactory → `parent -> new ConfigScreen(parent)` でCloth Configのスクリーンを返す

**Cloth Config バージョン対応表:**
| MC | Cloth Config |
|---|---|
| 1.21(.1) | v15.0.130 |
| 1.21.6-8 | v19.0.147 |
| 1.21.9-10 | v20.0.149 |
| 1.21.11 | v21.11.151 |

## 3. HTTP client (OkHttp vs Java HttpClient)

**OkHttp:**
- `com.squareup.okhttp3:okhttp:5.4.0`
- 非同期: `enqueue(Callback)` または `CompletableFuture` ラップ
- Maven Central から取得

**Java 21 HttpClient (推奨):**
- 標準ライブラリ、追加依存不要
- `java.net.http.HttpClient`
- 非同期: `sendAsync(request, BodyHandlers.ofString())` → `CompletableFuture<String>`
- Fabric環境では依存を減らせるのでこちらを推奨

## 4. Fabric Mixin

**Inject対象: `CommandBlockScreen.addAdditionalButtons()`**

```yaml
# mixins/ask-ai-mod.mixins.json
{
  "required": true,
  "package": "com.example.askaimod.mixin",
  "compatibilityLevel": "JAVA_21",
  "client": [
    "CommandBlockScreenMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

```java
// CommandBlockScreen (extends AbstractCommandBlockScreen)
// addAdditionalButtons() — ボタン追加に最適なフック
@Mixin(CommandBlockScreen.class)
public class CommandBlockScreenMixin {
    @Inject(method = "addAdditionalButtons", at = @At("TAIL"))
    private void addAiButton(CallbackInfo ci) {
        // this は CommandBlockScreen
        // ButtonWidget を追加
    }
}
```

Key observations from Yarn docs:
- `CommandBlockScreen` has `modeButton`, `conditionalModeButton`, `redstoneTriggerButton` (private `CyclingButtonWidget`)
- `addAdditionalButtons()` — ✅ 最適な注入ポイント
- `updateCommandBlock()` — public, コマンド反映時に呼べる
- `AbstractCommandBlockScreen` has `doneButton`, `commandSuggestor`

## 5. Screen API (1.21.x)

**Clickable text pattern:**
```java
// SUGGEST_COMMAND: クリックでチャット入力欄に挿入 (実行はユーザーEnter)
Style style = Style.EMPTY
    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to use")));

MutableText text = Text.literal("/command").setStyle(style);
```

**重要:** `RUN_COMMAND` は即時実行、`SUGGEST_COMMAND` は入力欄に挿入。
REQUIREMENTS.md の "ユーザーがEnterで実行" 要件には `SUGGEST_COMMAND` が合致。

**Custom Screen:**
```java
public class AiChatScreen extends Screen {
    private TextFieldWidget inputField;

    @Override
    public void init() {
        inputField = new TextFieldWidget(textRenderer, x, y, width, height, Text.literal("Ask AI..."));
        addDrawableChild(inputField);
    }
}
```

開き方: `client.setScreen(new AiChatScreen(Text.literal("AI Chat")))`
チャット入力欄に展開: `client.setScreen(new ChatScreen(command))` または `SUGGEST_COMMAND` を使う

## 6. OpenAI API contract

```
POST /v1/chat/completions
Content-Type: application/json
Authorization: Bearer <API_KEY>

{
  "model": "gpt-4o-mini",
  "messages": [
    {"role": "system", "content": "You are a Minecraft command assistant..."},
    {"role": "user", "content": "...ユーザー入力..."}
  ],
  "n": 1,
  "temperature": 0.7
}
```

**Response:**
```json
{
  "choices": [
    {
      "message": {
        "role": "assistant",
        "content": "...レスポンス..."
      }
    }
  ]
}
```

## 7. JSON parsing

**Gson (Fabric API に同梱):**
- Fabric API は `com.google.gson` を transitive dependency として含む
- 追加設定不要。`Gson` / `JsonObject` を直接使える

```java
Gson gson = new Gson();
JsonObject body = new JsonObject();
body.addProperty("model", model);
// ...
```

## 決定事項

| 選択肢 | 決定 | 理由 |
|---|---|---|
| HTTP client | Java 21 `HttpClient` | 依存ゼロ、CompletableFuture対応 |
| JSON | Gson (Fabric API同梱) | 追加依存不要 |
| ClickEvent | `SUGGEST_COMMAND` | ユーザーEnterまで実行しない要件 |
| Mixin inject target | `addAdditionalButtons()` @TAIL | ボタン追加に最適 |
| Mappings | Mojang公式 | Yarn終了予定のため将来性 |
| Mod separation | シングルモジュール (`splitEnvironmentSourceSets`不要) | クライアント専用modのため |
