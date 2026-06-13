# Ask AI Mod — 要件定義書

## 基本情報

| 項目 | 値 |
|------|-----|
| プラットフォーム | Fabric (Java Edition) |
| 対象バージョン | 1.21.x |
| 実行環境 | クライアントサイド |

## 機能要件

### 1. 専用AIチャットScreen

- バニラチャットとは独立した専用GUIウィンドウ
- テキスト入力欄 + AIレスポンス表示エリアで構成
- AIレスポンス内のコマンド（コードブロック `` ``` `` or `` ` ``, もしくは `/` 始まり文字列）をクリッカブルに描画
- クリック → バニラチャット入力欄にコマンドを展開 (`openChatScreen(command)`)
- ユーザーが Enter で実行（mod側は実行しない）

### 2. コマンドブロック補助

- `CommandBlockScreen` に Mixin で AI ボタンを注入
- ボタン押下 → 現在の入力内容をコンテキストとして AI 呼び出し
- レスポンスのコマンドを抽出 → 確認後にフィールドへ反映

### 3. OpenAI 互換 API

- `POST /v1/chat/completions` を使用
- 設定画面（Mod Menu 連携）でエンドポイント・APIキー・モデル名を管理
- APIキーはクライアントローカルの config に保存

## システムプロンプト（自動付与）

実行時に以下を動的に埋め込む:

```
You are a Minecraft command assistant.
Current context:
- Version: 1.21.x (Java Edition)
- Gamemode: {gamemode}
- World type: {singleplayer|multiplayer}
- Operator: {true|false}

When providing commands, always wrap them in a code block starting with /.
Explain what each command does briefly.
```

`MinecraftClient.getInstance()` から取得できる情報を動的に埋め込む。

## コマンド検出・貼り付けフロー

```
AIレスポンス
  → コードブロック or /始まり文字列を抽出
  → チャット画面にクリッカブルなコンポーネントとして描画
  → クリック → openChatScreen("/detected_command") で入力欄に展開
  → ユーザーが Enter で実行
```

## 非機能要件

| 項目 | 方針 |
|------|------|
| コマンド実行 | 必ずユーザー操作を介する（mod側からの直接実行なし） |
| API通信 | 非同期（UIブロッキングなし）。OkHttp または Java HttpClient |
| 設定 | Mod Menu + Cloth Config（またはシンプルなJSON） |
| ストリーミング | 今フェーズはなし（将来対応として保留） |

## 実装スコープ外（明示的除外）

- サーバーサイド対応
- コマンドの自動実行
- ストリーミングレスポンス（v1では）
