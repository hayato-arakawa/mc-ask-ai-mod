# Research Results

## 1. Fabric Loom + Gradle setup (1.21.11)

| Item | Value |
|---|---|
| Minecraft | 1.21.11 |
| Loom plugin ID | `net.fabricmc.fabric-loom-remap` (難読化版のためremap必要) |
| Loom version | `1.14-SNAPSHOT` |
| Loader version | `0.18.2` |
| Fabric API | `0.139.4+1.21.11` |
| Mappings | `loom.officialMojangMappings()` |
| Java | 21, `options.release = 21` |
| Gradle | 9.2.1 |
| 公式example | https://github.com/FabricMC/fabric-example-mod/commit/86e800d48332f8d1adc67965d029ac816bc97626 |

## 2. Cloth Config + Mod Menu

| Dependency | Maven coordinate | Version (1.21.11) |
|---|---|---|
| Cloth Config | `me.shedaniel.cloth:cloth-config-fabric` | `21.11.153` |
| Mod Menu | `com.terraformersmc:modmenu` | `17.0.0-beta.1` |

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

**Cloth Config builder pattern (Mojang mappings — `Component`を使う):**
```java
ConfigBuilder builder = ConfigBuilder.create()
    .setParentScreen(parent)
    .setTitle(Component.literal("Ask AI Mod Config"))
    .setDefaultBackgroundTexture(ConfigBuilder.getDefaultBackgroundTexture());
ConfigEntryBuilder entryBuilder = builder.entryBuilder();

ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
general.addEntry(entryBuilder.startStrField(
    Component.literal("API Endpoint"),
    config.endpoint
).setDefaultValue("https://api.openai.com/v1")
 .setSaveConsumer(v -> config.endpoint = v)
 .build());
```

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

- クラス (Mojang): `net.minecraft.client.gui.screens.inventory.CommandBlockScreen`
- 親クラス (Mojang): `net.minecraft.client.gui.screens.inventory.AbstractCommandBlockScreen`

```json
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
@Mixin(CommandBlockScreen.class)
public class CommandBlockScreenMixin {
    @Inject(method = "addAdditionalButtons", at = @At("TAIL"))
    private void addAiButton(CallbackInfo ci) {
        CommandBlockScreen self = (CommandBlockScreen) (Object) this;
        // this.addRenderableWidget(Button.builder(...).build());
    }
}
```

Key observations:
- `addAdditionalButtons()` — 最適な注入ポイント、`@At("TAIL")` で既存ボタンの後ろに追加
- `updateCommandBlock()` — public, コマンドフィールド反映時に呼べる
- 内部に `commandTextField` (EditBox) または `commandSuggestor` がある想定
- Mixin設定ファイルは `resources/ask-ai-mod.mixins.json`

## 5. Screen API (1.21.x) — Mojang mappings

**重要: Yarn名とMojang名の差異**

| Yarn名 | Mojang名 | インポート先 |
|---|---|---|
| `Text` | `Component` | `net.minecraft.network.chat.Component` |
| `Text.literal()` | `Component.literal()` | |
| `MutableText` | `MutableComponent` | `net.minecraft.network.chat.MutableComponent` |
| `TextFieldWidget` | **`EditBox`** | `net.minecraft.client.gui.components.EditBox` |
| `TextRenderer` | **`Font`** | `net.minecraft.client.gui.Font` |
| `GuiGraphics` | `GuiGraphics`（同名） | `net.minecraft.client.gui.GuiGraphics` |
| `Screen` | `Screen`（同名） | `net.minecraft.client.gui.screens.Screen` |
| `Button` | `Button`（同名） | `net.minecraft.client.gui.components.Button` |
| `ClickEvent` | `ClickEvent`（同名） | `net.minecraft.network.chat.ClickEvent` |
| `Style` | `Style`（同名） | `net.minecraft.network.chat.Style` |

**Clickable text pattern (Mojang):**
```java
Style style = Style.EMPTY
    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to use")));

MutableComponent text = Component.literal("/command").setStyle(style);
```

**重要:** `RUN_COMMAND` は即時実行、`SUGGEST_COMMAND` は入力欄に挿入。
REQUIREMENTS.md の "ユーザーがEnterで実行" 要件には `SUGGEST_COMMAND` が合致。

**Custom Screen (Mojang):**
```java
public class AiChatScreen extends Screen {
    private EditBox inputField;

    @Override
    protected void init() {
        inputField = new EditBox(this.font, 40, 40, 200, 20, Component.literal("Ask AI..."));
        addRenderableWidget(inputField);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
    }
}
```

開き方: `Minecraft.getInstance().setScreen(new AiChatScreen(Component.literal("AI Chat")))`
チャット入力欄に展開: `client.setScreen(new ChatScreen(command))` または `SUGGEST_COMMAND` を使う

**Confirm from Fabric docs source (`reference/1.21.11`):**
- `Component.literal()` / `Component.empty()` / `Component.nullToEmpty()`
- `Button.builder(text, callback).bounds(x, y, w, h).build()`
- `this.addRenderableWidget(widget)`
- `this.minecraft.getToastManager()` など
- `graphics.drawString(this.font, string, x, y, color, shadow)`

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
|---|---|---|---|
| Mod loader | Fabric 専用 (マルチローダー非対応) | Sinytra Connectorは1.21.11非対応のため |
| HTTP client | Java 21 `HttpClient` | 依存ゼロ、CompletableFuture対応 |
| JSON | Gson (Fabric API同梱) | 追加依存不要 |
| ClickEvent | `SUGGEST_COMMAND` | ユーザーEnterまで実行しない要件 |
| Mixin inject target | `addAdditionalButtons()` @TAIL | ボタン追加に最適 |
| Mappings | Mojang公式 | Yarn終了予定のため将来性 |
| Mod separation | シングルモジュール | クライアント専用mod、split不要 |
