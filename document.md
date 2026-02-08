# Miterundesu Android 再現タスクドキュメント

> iOS版 Miterundesu（ミテルンデス）を Kotlin / Jetpack Compose で完全に再現するためのタスク一覧

---

## 目次

1. [プロジェクト概要](#1-プロジェクト概要)
2. [プロジェクト構成・セットアップ](#2-プロジェクト構成セットアップ)
3. [データモデル](#3-データモデル)
4. [マネージャー・サービス層](#4-マネージャーサービス層)
5. [UI画面](#5-ui画面)
6. [カメラ機能](#6-カメラ機能)
7. [画像管理](#7-画像管理)
8. [セキュリティ機能](#8-セキュリティ機能)
9. [シアターモード](#9-シアターモード)
10. [プレスモード](#10-プレスモード)
11. [ローカライゼーション](#11-ローカライゼーション)
12. [チュートリアル・オンボーディング](#12-チュートリアルオンボーディング)
13. [アクセシビリティ](#13-アクセシビリティ)
14. [ネットワーク監視](#14-ネットワーク監視)
15. [透かし（ウォーターマーク）](#15-透かしウォーターマーク)
16. [アセット・リソース](#16-アセットリソース)
17. [iOS → Android 技術マッピング](#17-ios--android-技術マッピング)

---

## 1. プロジェクト概要

### アプリの目的
- 視覚障害者や高齢者向けのプライバシー重視カメラ拡大鏡アプリ
- 美術館・博物館・劇場などの文化施設での利用を想定
- 撮影画像は10分後に自動削除
- スクリーンショット・画面録画を検知・防止

### 主要機能
- カメラプレビュー（3:4アスペクト比）
- ピンチ・ボタンによるズーム（最大200倍）
- 画像キャプチャと10分自動削除
- シアターモード（美術館・劇場向け静音モード）
- プレスモード（報道関係者向け認証モード）
- 透かし（ウォーターマーク）付き画像保存
- スクリーンショット・画面録画防止
- 日本語/英語二言語対応
- インタラクティブチュートリアル
- VoiceOver（TalkBack）完全対応

---

## 2. プロジェクト構成・セットアップ

### タスク 2.1: Android プロジェクト初期化
- [ ] Android Studio で新規 Kotlin プロジェクト作成
- [ ] パッケージ名: `com.miterundesu.app`（仮）
- [ ] minimum SDK: API 26 (Android 8.0)
- [ ] target SDK: 最新安定版
- [ ] Jetpack Compose を有効化

### タスク 2.2: 依存関係の設定（build.gradle.kts）
- [ ] **Jetpack Compose**: UI フレームワーク
- [ ] **CameraX**: カメラ制御（`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`）
- [ ] **Room Database**: ローカルDB（CoreData 代替）
- [ ] **Supabase-kt**: Supabase クライアント（`postgrest-kt`, `gotrue-kt`）
- [ ] **Ktor**: HTTP クライアント（Supabase-kt の依存）
- [ ] **Kotlinx Serialization**: JSON シリアライゼーション
- [ ] **DataStore Preferences**: 設定値永続化（UserDefaults 代替）
- [ ] **AndroidX Security Crypto**: EncryptedSharedPreferences（Keychain 代替）
- [ ] **Coil**: 画像読み込み・キャッシュ
- [ ] **Navigation Compose**: 画面遷移
- [ ] **Accompanist**: 権限リクエスト等
- [ ] **Lifecycle Runtime Compose**: ライフサイクル管理

### タスク 2.3: プロジェクト構造
```
com.miterundesu.app/
├── MainActivity.kt              // エントリーポイント
├── MiterundesuApplication.kt    // Application クラス
├── data/
│   ├── model/
│   │   ├── CapturedImage.kt     // 撮影画像モデル
│   │   └── PressAccount.kt      // プレスアカウントモデル
│   ├── local/
│   │   ├── AppDatabase.kt       // Room Database
│   │   ├── CapturedImageDao.kt  // DAO
│   │   └── CapturedImageEntity.kt // Entity
│   └── remote/
│       └── SupabaseClient.kt    // Supabase 設定
├── manager/
│   ├── CameraManager.kt         // カメラ制御
│   ├── ImageManager.kt          // 画像管理
│   ├── SecurityManager.kt       // セキュリティ
│   ├── SettingsManager.kt       // 設定管理
│   ├── LocalizationManager.kt   // ローカライズ
│   ├── PressModeManager.kt      // プレスモード
│   ├── OnboardingManager.kt     // オンボーディング
│   ├── WhatsNewManager.kt       // 新機能通知
│   └── NetworkMonitor.kt        // ネットワーク監視
├── ui/
│   ├── theme/
│   │   ├── Color.kt             // カラー定義
│   │   ├── Theme.kt             // テーマ
│   │   └── Type.kt              // タイポグラフィ
│   ├── screen/
│   │   ├── MainScreen.kt        // メイン画面
│   │   ├── SettingsScreen.kt    // 設定画面
│   │   ├── ImageGalleryScreen.kt // 画像ギャラリー
│   │   ├── CapturedImagePreviewScreen.kt // 撮影後プレビュー
│   │   ├── ExplanationScreen.kt  // 説明画面
│   │   ├── PressModeLoginScreen.kt  // プレスログイン
│   │   ├── PressModeInfoScreen.kt   // プレス情報
│   │   ├── PressModeAccountStatusScreen.kt // アカウント状態
│   │   └── ImageDeletedScreen.kt // 画像削除通知
│   ├── tutorial/
│   │   ├── TutorialWelcomeScreen.kt   // チュートリアル開始
│   │   ├── SpotlightTutorialScreen.kt // スポットライト
│   │   ├── TutorialCompletionScreen.kt // チュートリアル完了
│   │   └── WhatsNewScreen.kt         // 新機能通知
│   └── component/
│       ├── CameraPreview.kt      // カメラプレビュー
│       ├── ZoomControls.kt       // ズームコントロール
│       ├── ShutterButton.kt      // シャッターボタン
│       ├── HeaderView.kt         // ヘッダー
│       ├── FooterView.kt         // フッター
│       ├── InfiniteScrollingText.kt // スクロールテキスト
│       ├── WatermarkView.kt      // 透かし
│       ├── TimeRemainingBadge.kt  // 残り時間バッジ
│       └── ZoomableImage.kt      // ズーム可能画像
└── util/
    ├── WatermarkHelper.kt        // 透かし生成
    └── Extensions.kt             // 拡張関数
```

---

## 3. データモデル

### タスク 3.1: CapturedImage モデル
**iOS元ファイル**: `ImageManager.swift` 内の `CapturedImage`

- [ ] データクラス作成
```kotlin
data class CapturedImage(
    val id: UUID = UUID.randomUUID(),
    val capturedAt: Instant,
    val expiresAt: Instant, // capturedAt + 600秒（10分）
    val imageData: ByteArray // JPEG圧縮 quality 0.6
)
```
- [ ] `isExpired` 計算プロパティ実装（`expiresAt <= now`）
- [ ] `remainingTime` 計算プロパティ実装（`max(0, expiresAt - now)`）
- [ ] `remainingMinutes` / `remainingSeconds` フォーマット用プロパティ
- [ ] JPEG圧縮品質: 0.6（60%）

### タスク 3.2: PressAccount モデル
**iOS元ファイル**: `PressAccount.swift`

- [ ] `@Serializable` データクラス作成
- [ ] フィールド:
  - `id: String` (UUID)
  - `userId: String` (`user_id`)
  - `organizationName: String` (`organization_name`)
  - `organizationType: String?` (`organization_type`)
  - `contactPerson: String?` (`contact_person`)
  - `email: String?`
  - `phone: String?`
  - `approvedBy: String?` (`approved_by`)
  - `approvedAt: String?` (`approved_at`)
  - `expiresAt: String?` (`expires_at`)
  - `isActive: Boolean` (`is_active`)
  - `lastLoginAt: String?` (`last_login_at`)
  - `createdAt: String` (`created_at`)
  - `updatedAt: String` (`updated_at`)
- [ ] `@SerialName` アノテーションで snake_case マッピング
- [ ] `PressAccountStatus` enum: `active`, `expired`, `deactivated`
- [ ] 計算プロパティ:
  - `status`: isActive=false→deactivated、期限切れ→expired、それ以外→active
  - `isValid`: status == active
  - `daysUntilExpiration`: expiresAt までの残り日数
  - `expirationDisplayString`: 有効期限の表示文字列（expiresAt が null なら「無期限」）
  - `summary`: 「組織名 (userId)」
  - `statusMessage`: ステータスに応じたローカライズ済みメッセージ

### タスク 3.3: Room Entity
**iOS元ファイル**: `CoreDataManager.swift`

- [ ] `CapturedImageEntity` Entity 作成
```kotlin
@Entity(tableName = "captured_images")
data class CapturedImageEntity(
    @PrimaryKey val id: String, // UUID文字列
    @ColumnInfo(name = "image_data") val imageData: ByteArray,
    @ColumnInfo(name = "captured_at") val capturedAt: Long, // epoch millis
    @ColumnInfo(name = "expiration_date") val expirationDate: Long // epoch millis
)
```
- [ ] `CapturedImageDao` 作成（insert, delete, getAll, deleteExpired）
- [ ] `AppDatabase` 作成（Room database クラス）
- [ ] Entity ↔ モデル変換関数

---

## 4. マネージャー・サービス層

### タスク 4.1: SettingsManager
**iOS元ファイル**: `SettingsManager.swift`

- [ ] DataStore Preferences で永続化
- [ ] 管理する設定値:
  - `maxZoomFactor: Float`（範囲 10〜200、デフォルト 100）
  - `language: String`（"ja" または "en"、デフォルト "ja"）
  - `isTheaterMode: Boolean`（デフォルト false）
  - `isPressMode: Boolean`（デフォルト false）
  - `scrollingMessageNormal: String`（デフォルト "すみません、目が不自由なのでこのアプリで拡大して見ています"）
  - `scrollingMessageTheater: String`（デフォルト "すみません、拡大して見ています"）
- [ ] `StateFlow` で各値を公開
- [ ] `resetToDefaults()` メソッド
- [ ] 値変更時の自動永続化

### タスク 4.2: LocalizationManager
**iOS元ファイル**: `LocalizationManager.swift`

- [ ] シングルトンクラス
- [ ] 250以上の文字列ペア（日本語/英語）
- [ ] `localizedString(key: String): String` メソッド
- [ ] フォーマット置換対応: `{zoom}`, `{minutes}`, `{seconds}`, `{days}`, `{count}`, `{current}`, `{total}`, `{time}`, `{number}`, `{period}`
- [ ] カテゴリ別文字列:
  - **カメラ操作**: zoom_in, zoom_out, reset_zoom, capture, zoom_level, zoom_slider, max_zoom_factor
  - **セキュリティ警告**: screenshot_detected, recording_detected, screenshot_warning_title/message, recording_warning_title/message, security_overlay_message
  - **シアターモード**: theater_mode, theater_mode_on/off, theater_mode_description
  - **プレスモード**: press_mode, press_login_title, press_login_user_id/password, press_login_button, press_login_error_* (invalid_credentials, expired, deactivated, invalid, failed), press_logout, press_account_status, press_account_* (organization, type, contact, email, phone, approved_by, approved_date, expires, status, last_login, days_remaining), press_status_* (active, expired, deactivated), press_info_*, press_renewal_*
  - **ギャラリー**: gallery_title, no_images, image_count, delete_image, delete_all, time_remaining, image_expired, page_indicator
  - **チュートリアル**: tutorial_welcome_title/message, tutorial_start/skip/next/previous/done, tutorial_step_* (zoom, capture, theater, message, settings) の title/description
  - **設定**: settings_title, camera_settings, zoom_range, language_setting, scrolling_message, reset_settings, reset_confirm, app_info, version, tutorial_show
  - **アクセシビリティ**: accessibility_* (shutter_button, zoom_in/out/reset, theater_toggle, settings_button, explanation_button, thumbnail, close, gallery, time_remaining, page_indicator, image_viewer, capture_preview)
  - **時間フォーマット**: time_minutes_seconds, time_remaining_format, time_expired
  - **画像削除**: image_deleted_title, image_deleted_reason
  - **一般**: close, cancel, confirm, ok, error, loading, app_name (常に "ミテルンデス")

### タスク 4.3: OnboardingManager
**iOS元ファイル**: `OnboardingManager.swift`

- [ ] シングルトン
- [ ] `SharedPreferences` で `hasCompletedOnboarding` を永続化
- [ ] フロー管理:
  1. `checkOnboardingStatus()`: 初回起動判定
  2. `showWelcomeScreen`
  3. `completeWelcomeScreen()` → `showFeatureHighlights`
  4. `showFeatureHighlights`
  5. `completeFeatureHighlights()` → `showCompletionScreen`
  6. `showCompletionScreen`
  7. `completeOnboarding()`
- [ ] `showTutorial()`: 設定画面からの再表示（`hasCompletedOnboarding` はリセットしない）
- [ ] 各状態を `StateFlow<Boolean>` で公開

### タスク 4.4: WhatsNewManager
**iOS元ファイル**: `WhatsNewManager.swift`

- [ ] `SharedPreferences` で `lastSeenAppVersion` を永続化
- [ ] `shouldShowWhatsNew`: 既存ユーザーが v1.1.0 にアップデートした場合のみ true
  - 初回インストール（`lastSeenAppVersion` が null）では表示しない
  - `lastSeenAppVersion` が "1.1.0" 未満の場合のみ表示
- [ ] `markAsSeen()`: `lastSeenAppVersion` を現在のバージョンに更新

### タスク 4.5: NetworkMonitor
**iOS元ファイル**: `NetworkMonitor.swift`

- [ ] `ConnectivityManager` を使用したネットワーク監視
- [ ] `StateFlow<Boolean>` で `isConnected` を公開
- [ ] `NetworkCallback` でリアルタイム監視
- [ ] アプリ起動時に初期状態を取得

---

## 5. UI画面

### タスク 5.1: MainActivity / メインエントリーポイント
**iOS元ファイル**: `miterundesuApp.swift`

- [ ] 画面回転ロック: 縦向き（Portrait）のみ
  - `AndroidManifest.xml` で `android:screenOrientation="portrait"`
- [ ] `PressModeManager` をアプリ全体で共有（Hilt / manual DI）
- [ ] アプリのライフサイクル監視（`ProcessLifecycleOwner`）
  - バックグラウンド遷移時: セキュリティ処理
  - フォアグラウンド復帰時: 期限切れ画像チェック
- [ ] ステータスバー・ナビゲーションバーのスタイル設定

### タスク 5.2: MainScreen（メイン画面）
**iOS元ファイル**: `ContentView.swift`（約1005行）

#### 5.2.1: 全体レイアウト
- [ ] 縦方向レイアウト: ヘッダー → カメラプレビュー → フッター
- [ ] 背景色: `MainGreen` (#369B45)
- [ ] シアターモード時の背景色: `TheaterOrange` (#D87A39)
- [ ] 全画面表示（エッジ to エッジ）

#### 5.2.2: ヘッダー部分 (HeaderView)
- [ ] 3段構成:
  1. **トップコントロール**: シアターモードトグル（左）、説明ボタン（中央）、設定ボタン（右）
  2. **ロゴ**: "Logo" 画像（高さ14dp、白色テンプレート）
  3. **スクロールテキスト**: 無限ループスクロール

#### 5.2.3: InfiniteScrollingText（無限スクロールテキスト）
- [ ] テキストを左から右へ無限ループスクロール
- [ ] アニメーション速度: テキスト長に基づく（1文字あたり約0.15秒）
- [ ] 2つのテキストインスタンスで継ぎ目なくループ
- [ ] シアターモード時はシアター用メッセージを使用
- [ ] 設定画面で編集可能

#### 5.2.4: シアターモードトグル (TheaterModeToggle)
- [ ] カスタムアイコン: 通常時は「月」アイコン、シアター時は「太陽」アイコン
- [ ] トグル時のアニメーション: 回転 + フェード
- [ ] アクセシビリティラベル: 現在の状態を説明
- [ ] iOS版のアイコン:
  - 通常時 (TheaterModeIcon): 半月アイコン（右半分が塗り、左半分が三日月カット、星2つ）
  - シアター時: `sun.max.fill` に相当する太陽アイコン

#### 5.2.5: フッター部分 (FooterView)
- [ ] 3列レイアウト:
  - **左**: サムネイル（最新撮影画像、タップでギャラリーへ）+ 残り時間バッジ
  - **中央**: シャッターボタン
  - **右**: ズームレベル表示

#### 5.2.6: ShutterButton（シャッターボタン）
- [ ] 外側リング（白、3dp幅）+ 内側円（白塗り）
- [ ] 撮影中は内側がグレーに変化
- [ ] タップで写真撮影
- [ ] 二重撮影防止（`isCapturing` フラグ）
- [ ] アクセシビリティ: 「撮影」ラベル

#### 5.2.7: ThumbnailView（サムネイル）
- [ ] 最新の撮影画像を丸角四角形で表示（48x48dp）
- [ ] 角丸: 8dp
- [ ] ボーダー: 白2dp
- [ ] タップでギャラリー画面へ遷移
- [ ] 画像なし時: カメラアイコン表示
- [ ] 複数画像時: 右上に画像数バッジ

#### 5.2.8: TimeRemainingBadge（残り時間バッジ）
- [ ] サムネイルの上に表示
- [ ] 「Xm Ys」形式で残り時間表示
- [ ] 1秒ごとに更新（Timer）
- [ ] 残り2分以下で赤色に変化
- [ ] 白背景 + MainGreen テキスト

#### 5.2.9: ZoomLevelView（ズームレベル表示）
- [ ] 現在のズーム倍率を「X.Xx」形式で表示
- [ ] 半透明白背景のカプセル形状
- [ ] フッター右側に配置

#### 5.2.10: シアターモード時のUI自動非表示
- [ ] シアターモード中、15秒後にヘッダーとフッターを非表示
- [ ] 画面タップで一時的に表示（再び15秒後に非表示）
- [ ] ズーム操作・撮影操作で表示をリセット
- [ ] アニメーション付きで表示/非表示切り替え

#### 5.2.11: スクリーンショット検知時の処理
- [ ] `hideContent = true` でカメラプレビューを黒画面に
- [ ] 画像プレビュー・ギャラリーを自動で閉じる
- [ ] 警告表示は SecurityManager が管理

#### 5.2.12: 画面遷移（フルスクリーンカバー）
- [ ] 設定画面（SettingsScreen）
- [ ] 説明画面（ExplanationScreen）
- [ ] 画像ギャラリー（ImageGalleryScreen）
- [ ] 撮影後プレビュー（CapturedImagePreviewScreen）
- [ ] チュートリアル（Welcome → Spotlight → Completion）
- [ ] WhatsNew 画面

### タスク 5.3: SettingsScreen（設定画面）
**iOS元ファイル**: `SettingsView.swift`（約582行）

- [ ] NavigationBar にタイトル「設定」+ シアターモードトグル
- [ ] **カメラ設定セクション**:
  - ズーム倍率スライダー（10〜200）
  - 現在値ラベル「Xx」
  - ステップ: 10刻み
- [ ] **言語設定セクション**:
  - セグメント切り替え: 日本語 / English
  - 切り替え時に即座に全UIを更新
- [ ] **スクロールメッセージセクション**:
  - 通常モード用メッセージ TextEditor
  - シアターモード用メッセージ TextEditor
  - 改行を自動除去
- [ ] **プレスモードセクション**:
  - ログイン済み: アカウント情報表示 + ログアウトボタン
  - 未ログイン: ログインリンク + 申請方法リンク
  - PressModeAccountStatusView をインライン表示
- [ ] **アプリ情報セクション**:
  - バージョン番号表示
  - 利用規約リンク（miterundesu.jp へ）
  - プライバシーポリシーリンク
  - チュートリアル再表示ボタン
- [ ] **リセットセクション**:
  - 「設定をリセット」ボタン（赤色）
  - 確認ダイアログ付き
- [ ] オフライン時の警告表示（プレスモード関連機能が使えない旨）
- [ ] ログアウト確認ダイアログ

### タスク 5.4: ExplanationScreen（説明画面）
**iOS元ファイル**: `ExplanationView.swift`（約329行）

- [ ] **通常モード時の内容**:
  - アプリの目的説明（視覚障害者・高齢者向け拡大鏡）
  - イラスト画像: icon-white-cane, icon-elderly, icon-wheelchair
  - 「このアプリの使い方」「なぜこのアプリが必要か」
- [ ] **シアターモード時の内容**:
  - 文化施設での利用説明
  - イラスト画像: building-theater, building-museum
  - 「劇場・美術館での利用について」
- [ ] **フッター**:
  - ウェブサイトリンク: miterundesu.jp
  - X（Twitter）リンク: @miterundesu_jp
  - Instagram リンク: @miterundesu_jp
- [ ] カスタムアイコン: X ロゴ、Instagram ロゴ（SVGパスから描画）
- [ ] 閉じるボタン

### タスク 5.5: ImageGalleryScreen（画像ギャラリー）
**iOS元ファイル**: `ImageGalleryView.swift`（約1061行）

#### 5.5.1: 全体構成
- [ ] 横スクロール（ページング）で画像を切り替え
- [ ] LazyRow + ページングスナップ
- [ ] 背景色: 黒

#### 5.5.2: トップコントロール
- [ ] 閉じるボタン（左上）
- [ ] 残り時間バッジ（中央上）
- [ ] 説明ボタン（右上、通常モード時のみ）

#### 5.5.3: ZoomableImageView（ズーム可能画像ビュー）
- [ ] ピンチでズーム（アンカーポイント対応）
- [ ] ドラッグで移動
- [ ] ダブルタップでリセット
- [ ] ズーム中のオフセット境界制限
- [ ] ズームコントロールボタン: +, -, 1x
- [ ] **連続ズーム**: ボタン長押しで加速（0.03秒間隔、時間に応じて指数的に加速、ズーム値に比例した速度）
- [ ] **1xボタン長押し**: 一時的に1xにリセット、離すと元のズームに復帰
- [ ] ズーム範囲: 1x〜10x

#### 5.5.4: ページインジケーター
- [ ] ドット形式のページインジケーター
- [ ] 現在ページ: 白、その他: 半透明白

#### 5.5.5: 画像期限切れ処理
- [ ] 表示中の画像が期限切れ → ImageDeletedScreen を表示
- [ ] 全画像期限切れ → ギャラリーを自動で閉じる
- [ ] バックグラウンド/フォアグラウンド遷移時の期限チェック

#### 5.5.6: セキュリティ保護
- [ ] スクリーンショット検知 → 画像をぼかし + 警告
- [ ] 画面録画中 → 画像をぼかし（半径30dp相当）+ 持続警告
- [ ] プレスモード時はセキュリティ制限なし

#### 5.5.7: ウォーターマーク
- [ ] 画像の左下にウォーターマークオーバーレイ表示

#### 5.5.8: VoiceOverアクセシビリティ
- [ ] 3本指スワイプで写真ナビゲーション
- [ ] 「X枚中Y枚目」の読み上げ
- [ ] 残り時間の読み上げ

### タスク 5.6: CapturedImagePreviewScreen（撮影後プレビュー）
**iOS元ファイル**: `CapturedImagePreview.swift`（約706行）

- [ ] 撮影直後に表示されるプレビュー画面
- [ ] **閉じるボタン**: シャッターボタンと同じスタイル（外リング + X アイコン）
- [ ] **ズーム機能**: ギャラリーと同じ ZoomableImageView を使用
- [ ] **トップコントロール**: 残り時間バッジ、説明ボタン、設定ボタン
- [ ] **セキュリティ保護**: ギャラリーと同じ保護
- [ ] **バックグラウンド処理**: バックグラウンド中に期限切れ → 自動閉じ
- [ ] **ウォーターマーク**: 左下に表示

### タスク 5.7: ImageDeletedScreen（画像削除通知）
**iOS元ファイル**: `ImageDeletedView.swift`

- [ ] 背景色: MainGreen
- [ ] タイマーアイコン（大サイズ、120sp相当）
- [ ] タイトル: 「自動削除されました」
- [ ] 理由テキスト: 「10分が経過したため〜」
- [ ] **自動消去**: 2.5秒後に自動で閉じる
- [ ] **TalkBack有効時**: 自動消去しない、閉じるボタンを表示
- [ ] ダークテーマ

### タスク 5.8: PressModeLoginScreen（プレスログイン）
**iOS元ファイル**: `PressModeLoginView.swift`（約226行）

- [ ] ユーザーID入力フィールド
- [ ] パスワード入力フィールド（表示/非表示トグル付き）
- [ ] ログインボタン
- [ ] エラーメッセージ表示（赤色テキスト）
- [ ] ローディング状態: ProgressIndicator 表示
- [ ] 「プレスアカウントについて」情報セクション
- [ ] キーボード種別: ユーザーID は ASCII capable

### タスク 5.9: PressModeAccountStatusScreen（アカウント状態）
**iOS元ファイル**: `PressModeAccountStatusView.swift`（約185行）

- [ ] ステータスアイコン: active→checkmark(緑)、expired→clock(オレンジ)、deactivated→xmark(赤)
- [ ] 情報行:
  - 組織名
  - 組織種別
  - 担当者
  - メール
  - 電話
  - 承認者
  - 承認日
  - 有効期限
  - 残り日数
  - ステータス
  - 最終ログイン
- [ ] 期限切れ時: 更新申請リンク（miterundesu.jp/press）

### タスク 5.10: PressModeInfoScreen（プレス情報）
**iOS元ファイル**: `PressModeInfoView.swift`（約242行）

- [ ] プレスモードの説明
- [ ] 対象者リスト（報道関係者、教育者など）
- [ ] 3ステップの申請手順
- [ ] 申請リンク: miterundesu.jp/press
- [ ] 閉じるボタン

---

## 6. カメラ機能

### タスク 6.1: CameraManager
**iOS元ファイル**: `CameraManager.swift`（約499行）

#### 6.1.1: カメラ初期化
- [ ] CameraX を使用
- [ ] バックカメラ（広角）を選択
- [ ] プレビュー解像度: 4:3 アスペクト比
- [ ] キャプチャモード: 写真（品質優先）
- [ ] カメラ権限リクエスト処理

#### 6.1.2: ズーム機能
- [ ] `zoom(factor: Float)`: 即座にズーム変更
- [ ] `smoothZoom(to: Float, duration: Float)`: アニメーション付きズーム
- [ ] ズーム範囲: 1x 〜 設定の maxZoomFactor
- [ ] ピンチジェスチャーでのズーム
- [ ] ズームボタン: +, -, 1x

#### 6.1.3: 連続ズーム
- [ ] ボタン長押しで連続的にズーム変更
- [ ] 間隔: 0.03秒
- [ ] 加速: 時間経過で指数的に速度増加
- [ ] ズーム値が大きいほど速度も速い（比例）
- [ ] 最大速度制限あり

#### 6.1.4: 1xボタン長押し機能
- [ ] 長押し開始: 現在のズーム値を保存、1x にリセット
- [ ] 長押し終了: 保存したズーム値に復帰
- [ ] 通常タップ: 1x にリセット（保存なし）

#### 6.1.5: 写真撮影
- [ ] CameraX ImageCapture を使用
- [ ] 二重撮影防止（`isCapturing` フラグ）
- [ ] 撮影時にウォーターマークを画像に焼き込み
- [ ] 画像のダウンサンプリング: 最大 2048px（BitmapFactory.Options.inSampleSize）
- [ ] JPEG品質: 60%
- [ ] 撮影完了後、ImageManager に追加

#### 6.1.6: カメラプレビュー（CameraPreview Composable）
**iOS元ファイル**: `CameraPreview.swift`（約301行）

- [ ] CameraX PreviewView を Composable にラップ（AndroidView）
- [ ] ピンチジェスチャー検出（`detectTransformGestures`）
- [ ] ズームコントロールオーバーレイ
- [ ] ウォーターマークオーバーレイ（左下）

---

## 7. 画像管理

### タスク 7.1: ImageManager
**iOS元ファイル**: `ImageManager.swift`（約283行）

#### 7.1.1: 画像保存・読み込み
- [ ] 撮影画像を Room Database に保存
- [ ] アプリ起動時に保存済み画像を自動読み込み
- [ ] 期限切れ画像の自動除去
- [ ] `StateFlow<List<CapturedImage>>` で画像リストを公開

#### 7.1.2: 10分自動削除
- [ ] 各画像に対して個別のタイマーを設定
- [ ] `capturedAt + 600秒` で期限切れ
- [ ] 期限切れ時: Room から削除 + リストから除去
- [ ] `CoroutineScope` で `delay()` を使用してタイマー実装
- [ ] タイマーIDの管理とキャンセル処理

#### 7.1.3: 画像キャッシュ（LRU）
- [ ] 最大2エントリーのLRUキャッシュ
- [ ] `Bitmap` をメモリにキャッシュ
- [ ] `ByteArray → Bitmap` 変換のキャッシュ
- [ ] メモリ警告時にキャッシュクリア（`ComponentCallbacks2`）
- [ ] スレッドセーフ実装

#### 7.1.4: メソッド
- [ ] `addImage(imageData: ByteArray)`: 新画像追加 → DB保存 → タイマー開始
- [ ] `removeImage(id: UUID)`: 画像削除 → DB削除 → タイマーキャンセル
- [ ] `removeExpiredImages()`: 期限切れ画像の一括削除
- [ ] `clearAllImages()`: 全画像削除
- [ ] `loadImages()`: DB から全画像を読み込み

---

## 8. セキュリティ機能

### タスク 8.1: SecurityManager
**iOS元ファイル**: `SecurityManager.swift`（約402行）

#### 8.1.1: スクリーンショット検知
- [ ] **Android実装**: `FLAG_SECURE` を `Window` に設定
- [ ] `FLAG_SECURE` はスクリーンショットと画面録画の両方を防止
- [ ] Android 14+ (API 34): `Activity.ScreenCaptureCallback` でスクリーンショット検知
- [ ] 検知時の処理:
  - `hideContent = true`（カメラプレビューを黒に）
  - 画像プレビュー/ギャラリーを閉じる
  - 警告ダイアログを3秒間表示
  - 3秒後に自動消去

#### 8.1.2: 画面録画検知
- [ ] `FLAG_SECURE` で画面録画時にプレビューが黒くなる
- [ ] Android 11+ (API 30): `MediaProjection` の状態監視
- [ ] 録画検知時:
  - カメラプレビューにぼかし効果（半径30dp）
  - 持続的な警告バナー表示
  - 録画停止まで維持

#### 8.1.3: セキュアサーフェス
- [ ] iOS の `ScreenshotPreventView`（secure UITextField）に相当
- [ ] Android: `FLAG_SECURE` が標準的な方法
- [ ] `SurfaceView` に `setSecure(true)` を設定
- [ ] 画像表示部分にも適用

#### 8.1.4: プレスモード連携
- [ ] プレスモード有効時: 全セキュリティ制限を無効化
- [ ] `FLAG_SECURE` を解除
- [ ] 警告表示を抑制

#### 8.1.5: 警告UI
- [ ] **スクリーンショット警告**: モーダルダイアログ（タイトル + メッセージ + 3秒自動消去）
- [ ] **録画中警告**: 画面上部に赤いバナー（persistent）
- [ ] 警告テキストはローカライズ対応

---

## 9. シアターモード

### タスク 9.1: シアターモード実装
**iOS元ファイル**: `ContentView.swift` のシアターモード関連部分

- [ ] トグル切り替えで即座にモード変更
- [ ] **テーマ色変更**: `MainGreen` → `TheaterOrange` (#D87A39)
- [ ] **スクロールメッセージ変更**: 通常メッセージ → シアターメッセージ
- [ ] **UI自動非表示**: 15秒後にヘッダーとフッターを自動非表示
- [ ] **タップで一時表示**: 画面タップで再表示、15秒後に再び非表示
- [ ] **操作でリセット**: ズーム操作・撮影でタイマーリセット
- [ ] **表示/非表示アニメーション**: フェードイン/アウト
- [ ] **説明画面の内容切り替え**: 通常→アクセシビリティ説明、シアター→文化施設説明
- [ ] **設定で永続化**: `SettingsManager.isTheaterMode`

---

## 10. プレスモード

### タスク 10.1: PressModeManager
**iOS元ファイル**: `PressModeManager.swift`（約291行）

#### 10.1.1: 認証フロー
- [ ] Supabase RPC関数 `verify_press_account_password` を呼び出し
- [ ] パラメータ: `p_user_id`, `p_password`
- [ ] レスポンス: `List<PressAccount>`（空なら認証失敗）
- [ ] アカウント状態チェック: `isValid`（active かつ有効期限内）

#### 10.1.2: 認証情報の安全な保存
- [ ] `EncryptedSharedPreferences` を使用（Keychain 代替）
  - キー: `userId`, `password`
- [ ] AndroidKeyStore でマスターキーを管理
- [ ] 自動ログイン: アプリ起動時に保存認証情報で自動ログイン試行
- [ ] 自動ログイン失敗時: 認証情報クリア + プレスモードオフ

#### 10.1.3: ログイン成功時の処理
- [ ] `PressAccount` を保持
- [ ] `isPressModeEnabled = true`
- [ ] `isLoggedIn = true`
- [ ] 認証情報を EncryptedSharedPreferences に保存
- [ ] ログイン日時を UserDefaults に記録
- [ ] Supabase の `press_accounts` テーブルの `last_login_at` を更新

#### 10.1.4: ログアウト処理
- [ ] 全状態をクリア
- [ ] EncryptedSharedPreferences から認証情報を削除
- [ ] ログイン記録を削除

#### 10.1.5: Supabase クライアント設定
**iOS元ファイル**: `SupabaseClient.swift`

- [ ] Supabase URL: `https://gtxoniuzwhmdwnhegwnz.supabase.co`
- [ ] 公開 Anon Key: iOS版と同じキーを使用
- [ ] `supabase-kt` ライブラリで初期化
- [ ] Lazy初期化（シングルトン）

---

## 11. ローカライゼーション

### タスク 11.1: 全文字列の実装
**iOS元ファイル**: `LocalizationManager.swift`（約569行）

- [ ] 250以上の文字列ペアを全て移植
- [ ] 日本語をデフォルトとし、英語を代替として対応
- [ ] **重要な文字列カテゴリ**:

#### 11.1.1: カメラ関連
| キー | 日本語 | English |
|------|--------|---------|
| zoom_in | 拡大 | Zoom In |
| zoom_out | 縮小 | Zoom Out |
| reset_zoom | リセット | Reset Zoom |
| capture | 撮影 | Capture |
| zoom_level | 倍率 | Zoom Level |

#### 11.1.2: セキュリティ
| キー | 日本語 | English |
|------|--------|---------|
| screenshot_detected | スクリーンショット検知 | Screenshot Detected |
| recording_detected | 画面録画検知 | Screen Recording Detected |

#### 11.1.3: 時間フォーマット
| キー | 日本語 | English |
|------|--------|---------|
| time_minutes_seconds | {minutes}分{seconds}秒 | {minutes}m {seconds}s |
| time_remaining_format | 残り{time} | {time} remaining |

- [ ] フォーマット置換メソッド: `{placeholder}` → 実際の値
- [ ] `app_name` は言語によらず常に「ミテルンデス」

---

## 12. チュートリアル・オンボーディング

### タスク 12.1: TutorialWelcomeScreen
**iOS元ファイル**: `TutorialWelcomeView.swift`（約99行）

- [ ] LogoSquare 画像（200x200dp）
- [ ] ウェルカムタイトルとメッセージ
- [ ] 「はじめる」ボタン → SpotlightTutorial へ
- [ ] 「スキップ」ボタン → チュートリアル完了

### タスク 12.2: SpotlightTutorialScreen
**iOS元ファイル**: `SpotlightTutorialView.swift`（約591行）

#### 12.2.1: スポットライト効果
- [ ] Canvas でオーバーレイ描画（半透明黒）
- [ ] 対象UI要素の位置をくり抜き（`BlendMode.DstOut` 相当）
- [ ] ターゲット要素の周囲にハイライト効果

#### 12.2.2: 5ステップのチュートリアル
| ステップ | ターゲット | 内容 |
|----------|-----------|------|
| 1. Zoom | zoom_buttons + zoom_controls | ズーム操作の説明 |
| 2. Capture | shutter_button + photo_button | 撮影の説明 |
| 3. Theater | theater_toggle | シアターモードの説明 |
| 4. Message | scrolling_message + explanation_button | スクロールメッセージの説明 |
| 5. Settings | settings_button | 設定の説明 |

#### 12.2.3: UI要素
- [ ] 説明カード（TutorialDescriptionCard）: 動的に位置決定（画面境界でクランプ）
- [ ] 矢印線（TutorialArrowView）: カードからターゲットへの破線
- [ ] 次へ/前へ/完了ボタン
- [ ] ステップインジケーター（ドット）

#### 12.2.4: SpotlightPreferenceKey 相当の仕組み
- [ ] Compose の `onGloballyPositioned` で各UI要素の位置・サイズを取得
- [ ] ViewModel/State で位置情報を管理
- [ ] チュートリアル中にリアルタイムで位置を参照

### タスク 12.3: TutorialCompletionScreen
**iOS元ファイル**: `TutorialCompletionView.swift`（約93行）

- [ ] チェックマークサークルアイコン（大サイズ）
- [ ] 完了メッセージ
- [ ] 「使い始める」ボタン

### タスク 12.4: WhatsNewScreen
**iOS元ファイル**: `WhatsNewView.swift`（約110行）

- [ ] v1.1.0 新機能紹介:
  - 1xボタン長押し機能の説明
- [ ] FeatureRow コンポーネント: アイコン + タイトル + 説明
- [ ] 「了解」ボタン

---

## 13. アクセシビリティ

### タスク 13.1: TalkBack 対応
**iOS全ファイルのアクセシビリティ関連部分**

- [ ] 全インタラクティブ要素に `contentDescription` を設定
- [ ] `semantics` ブロックで適切なロール・アクション・状態を定義
- [ ] カスタムアクション:
  - シャッターボタン: 「撮影」
  - ズームイン/アウト/リセット: 「拡大」「縮小」「リセット」
  - シアターモードトグル: 現在のモードを含む説明
  - サムネイル: 「撮影した写真を表示」
- [ ] 画像ギャラリーで「X枚中Y枚目」を読み上げ
- [ ] 残り時間の読み上げ
- [ ] 状態変更時の `announceForAccessibility` 呼び出し

### タスク 13.2: TalkBack 固有の挙動
- [ ] ImageDeletedScreen: TalkBack 有効時は自動消去しない + 閉じるボタンを表示
- [ ] ギャラリー: 3本指スワイプで写真ナビゲーション（TalkBackジェスチャー）
- [ ] フォーカス管理: 画面遷移時に適切な要素にフォーカスを移動
- [ ] `accessibilityHidden(true)` 相当: 装飾アイコンを TalkBack から隠す

---

## 14. ネットワーク監視

### タスク 14.1: NetworkMonitor
**iOS元ファイル**: `NetworkMonitor.swift`

- [ ] `ConnectivityManager.NetworkCallback` でネットワーク状態監視
- [ ] `StateFlow<Boolean>` で `isConnected` を公開
- [ ] `CAPABILITY_INTERNET` + `CAPABILITY_VALIDATED` で接続判定
- [ ] Application クラスで初期化、ライフサイクルに合わせて登録/解除
- [ ] 設定画面でオフライン時にプレスモード関連の制限表示に使用

---

## 15. 透かし（ウォーターマーク）

### タスク 15.1: WatermarkHelper
**iOS元ファイル**: `WatermarkView.swift`（約251行）

#### 15.1.1: ウォーターマークテキスト生成
- [ ] フォーマット: `"YYYY/MM/DD HH:mm | ID: XXXXXX"`
- [ ] 日付: 現在日時
- [ ] ID: デバイスID の先頭6文字
  - Android: `Settings.Secure.ANDROID_ID` の先頭6文字
- [ ] 60秒ごとに更新（日付部分）

#### 15.1.2: 画像への焼き込み
- [ ] `Bitmap` にテキストを描画する拡張関数
- [ ] 描画位置: 画像の左下
- [ ] テキストスタイル: モノスペースフォント、半透明白
- [ ] `Canvas` と `Paint` で直接描画
- [ ] 撮影時にカメラ画像へ焼き込み（保存前）

#### 15.1.3: WatermarkView（Composable オーバーレイ）
- [ ] カメラプレビューの左下に表示
- [ ] Logo 画像（高さ10dp、opacity 0.4）
- [ ] 情報テキスト（8sp モノスペースフォント、opacity 0.35）
- [ ] `WatermarkOverlay` Modifier で簡単に適用
- [ ] WatermarkViewModel: 60秒ごとにテキスト更新

---

## 16. アセット・リソース

### タスク 16.1: カラーリソース
- [ ] `MainGreen`: #369B45（通常モードのテーマ色）
- [ ] `TheaterOrange`: #D87A39（シアターモードのテーマ色）
- [ ] `colors.xml` または Compose の `Color` で定義

### タスク 16.2: 画像アセット
- [ ] **Logo**: アプリロゴ（横長、ヘッダー用）
- [ ] **LogoSquare**: 正方形ロゴ（チュートリアル用）
- [ ] **icon-white-cane**: 白杖のイラスト（説明画面用）
- [ ] **icon-elderly**: 高齢者のイラスト（説明画面用）
- [ ] **icon-wheelchair**: 車椅子のイラスト（説明画面用）
- [ ] **building-theater**: 劇場のイラスト（シアターモード説明用）
- [ ] **building-museum**: 博物館のイラスト（シアターモード説明用）
- [ ] 各画像を `drawable` / `drawable-*dpi` に配置

### タスク 16.3: アプリアイコン
- [ ] iOS版と同じデザインを `mipmap` に配置
- [ ] Adaptive Icon 対応

---

## 17. iOS → Android 技術マッピング

| iOS (Swift/SwiftUI) | Android (Kotlin/Compose) |
|---|---|
| SwiftUI `View` | `@Composable` 関数 |
| `@State` | `remember { mutableStateOf() }` |
| `@Published` (ObservableObject) | `StateFlow` / `MutableStateFlow` |
| `@ObservedObject` | `collectAsState()` |
| `@EnvironmentObject` | CompositionLocal / Hilt |
| `NavigationLink` / `.sheet` / `.fullScreenCover` | Navigation Compose |
| `AVCaptureSession` | CameraX `ProcessCameraProvider` |
| `AVCapturePhotoOutput` | CameraX `ImageCapture` |
| `UIViewRepresentable` | `AndroidView` Composable |
| `CoreData` (NSManagedObjectModel) | Room Database |
| `UserDefaults` | DataStore Preferences |
| `Keychain` (SecItemAdd等) | EncryptedSharedPreferences |
| `NWPathMonitor` | ConnectivityManager + NetworkCallback |
| `UIScreen.isCaptured` | `FLAG_SECURE` on Window |
| `UIApplication.userDidTakeScreenshotNotification` | `ScreenCaptureCallback` (API 34+) |
| `UITextField` secure subview trick | `FLAG_SECURE` / `SurfaceView.setSecure(true)` |
| `Timer.publish` | `CoroutineScope` + `delay()` / `Flow` |
| `DispatchQueue.main.asyncAfter` | `Handler.postDelayed` / `delay()` |
| `UIAccessibility.isVoiceOverRunning` | `AccessibilityManager.isTouchExplorationEnabled` |
| `UIAccessibility.post(.announcement)` | `AccessibilityEvent.TYPE_ANNOUNCEMENT` |
| `GeometryReader` | `Modifier.onGloballyPositioned` |
| `PreferenceKey` | `LayoutCoordinates` コールバック |
| `Canvas` (SwiftUI) | `Canvas` (Compose) |
| `.blendMode(.destinationOut)` | `BlendMode.DstOut` |
| `MagnificationGesture` | `detectTransformGestures` |
| `DragGesture` | `detectDragGestures` |
| `.animation()` / `withAnimation` | `animate*AsState()` / `AnimatedVisibility` |
| `Color("MainGreen")` (Asset Catalog) | `Color(0xFF369B45)` / `colorResource()` |
| `Image(systemName:)` (SF Symbols) | Material Icons / Custom icons |
| `Bundle.main.infoDictionary["CFBundleShortVersionString"]` | `BuildConfig.VERSION_NAME` |
| Supabase Swift SDK | supabase-kt |
| `identifierForVendor` | `Settings.Secure.ANDROID_ID` |
| `UIApplication.shared.open(url)` | `Intent(Intent.ACTION_VIEW, uri)` |
| `.preferredColorScheme(.dark)` | `MaterialTheme` with dark colors |
| `Haptics` / `UIImpactFeedbackGenerator` | `HapticFeedbackType` Compose |

---

## 補足: 実装優先順位（推奨）

### Phase 1: コア基盤
1. プロジェクトセットアップ・依存関係
2. SettingsManager (DataStore)
3. LocalizationManager (全文字列)
4. NetworkMonitor
5. カラー・テーマ定義

### Phase 2: カメラ・画像
6. CameraManager (CameraX)
7. CameraPreview (Composable)
8. ImageManager (Room + 10分削除)
9. WatermarkHelper
10. SecurityManager (FLAG_SECURE)

### Phase 3: メイン画面
11. MainScreen (全体レイアウト)
12. HeaderView + InfiniteScrollingText
13. FooterView + ShutterButton + Thumbnail
14. ZoomControls (連続ズーム + 1x長押し)

### Phase 4: ギャラリー・プレビュー
15. ImageGalleryScreen (ページング + ズーム)
16. CapturedImagePreviewScreen
17. ImageDeletedScreen
18. ZoomableImageView

### Phase 5: 追加機能
19. シアターモード (テーマ切り替え + UI自動非表示)
20. PressModeManager (Supabase + EncryptedSharedPrefs)
21. PressModeLoginScreen
22. PressModeAccountStatusScreen / InfoScreen
23. ExplanationScreen

### Phase 6: チュートリアル・仕上げ
24. OnboardingManager
25. TutorialWelcomeScreen
26. SpotlightTutorialScreen (スポットライト効果)
27. TutorialCompletionScreen
28. WhatsNewManager + WhatsNewScreen
29. TalkBack 完全対応
30. 最終テスト・デバッグ

---

> このドキュメントは iOS版 Miterundesu の全ソースファイル（37ファイル）を解析して作成されました。
> 各タスクは iOS版の機能を完全に再現することを目的としています。
