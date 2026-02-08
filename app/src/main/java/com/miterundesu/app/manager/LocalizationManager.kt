package com.miterundesu.app.manager

import kotlinx.coroutines.flow.StateFlow

object LocalizationManager {

    private lateinit var settingsManager: SettingsManager

    fun initialize(settingsManager: SettingsManager) {
        this.settingsManager = settingsManager
    }

    val currentLanguage: StateFlow<String>
        get() = settingsManager.language

    private val strings: Map<String, Pair<String, String>> = mapOf(
        // ===== General =====
        "app_name" to Pair("ミテルンデス", "ミテルンデス"),
        "close" to Pair("閉じる", "Close"),
        "cancel" to Pair("キャンセル", "Cancel"),
        "confirm" to Pair("確認", "Confirm"),
        "ok" to Pair("OK", "OK"),
        "error" to Pair("エラー", "Error"),
        "loading" to Pair("読み込み中...", "Loading..."),
        "done" to Pair("完了", "Done"),
        "save" to Pair("保存", "Save"),
        "delete" to Pair("削除", "Delete"),
        "edit" to Pair("編集", "Edit"),
        "back" to Pair("戻る", "Back"),
        "next" to Pair("次へ", "Next"),
        "previous" to Pair("前へ", "Previous"),
        "yes" to Pair("はい", "Yes"),
        "no" to Pair("いいえ", "No"),
        "retry" to Pair("再試行", "Retry"),
        "warning" to Pair("警告", "Warning"),
        "info" to Pair("情報", "Information"),
        "success" to Pair("成功", "Success"),

        // ===== Camera Operations =====
        "zoom_in" to Pair("拡大", "Zoom In"),
        "zoom_out" to Pair("縮小", "Zoom Out"),
        "reset_zoom" to Pair("リセット", "Reset Zoom"),
        "capture" to Pair("撮影", "Capture"),
        "zoom_level" to Pair("倍率", "Zoom Level"),
        "zoom_slider" to Pair("ズームスライダー", "Zoom Slider"),
        "max_zoom_factor" to Pair("最大ズーム倍率", "Max Zoom Factor"),
        "zoom_factor_format" to Pair("{zoom}x", "{zoom}x"),
        "zoom_range" to Pair("ズーム範囲", "Zoom Range"),
        "zoom_range_format" to Pair("10x 〜 {zoom}x", "10x - {zoom}x"),
        "current_zoom" to Pair("現在のズーム: {zoom}x", "Current Zoom: {zoom}x"),
        "camera_permission_required" to Pair("カメラの使用許可が必要です", "Camera permission is required"),
        "camera_permission_denied" to Pair("カメラのアクセスが拒否されました。設定から許可してください。", "Camera access denied. Please allow access in Settings."),
        "camera_error" to Pair("カメラの起動に失敗しました", "Failed to start camera"),
        "camera_not_available" to Pair("カメラが利用できません", "Camera not available"),
        "capturing_image" to Pair("撮影中...", "Capturing..."),
        "image_captured" to Pair("撮影しました", "Image captured"),
        "capture_failed" to Pair("撮影に失敗しました", "Capture failed"),

        // ===== Security =====
        "screenshot_detected" to Pair("スクリーンショット検知", "Screenshot Detected"),
        "recording_detected" to Pair("画面録画検知", "Screen Recording Detected"),
        "screenshot_warning_title" to Pair("スクリーンショットが検知されました", "Screenshot Detected"),
        "screenshot_warning_message" to Pair("プライバシー保護のため、画像は表示できません。", "Images cannot be displayed for privacy protection."),
        "recording_warning_title" to Pair("画面録画が検知されました", "Screen Recording Detected"),
        "recording_warning_message" to Pair("プライバシー保護のため、画面録画中は画像がぼかされます。", "Images are blurred during screen recording for privacy protection."),
        "security_overlay_message" to Pair("セキュリティ保護中", "Security Protected"),
        "security_warning" to Pair("セキュリティ警告", "Security Warning"),
        "screenshot_prevention" to Pair("スクリーンショット防止が有効です", "Screenshot prevention is active"),
        "recording_prevention" to Pair("画面録画防止が有効です", "Screen recording prevention is active"),

        // ===== Theater Mode =====
        "theater_mode" to Pair("シアターモード", "Theater Mode"),
        "theater_mode_on" to Pair("シアターモード: ON", "Theater Mode: ON"),
        "theater_mode_off" to Pair("シアターモード: OFF", "Theater Mode: OFF"),
        "theater_mode_description" to Pair(
            "美術館・博物館・劇場などの文化施設でのご利用に最適化されたモードです。",
            "A mode optimized for use in cultural facilities such as museums, galleries, and theaters."
        ),
        "theater_mode_toggle" to Pair("シアターモード切替", "Toggle Theater Mode"),
        "theater_mode_activated" to Pair("シアターモードが有効になりました", "Theater Mode activated"),
        "theater_mode_deactivated" to Pair("シアターモードが無効になりました", "Theater Mode deactivated"),

        // ===== Press Mode =====
        "press_mode" to Pair("プレスモード", "Press Mode"),
        "press_login_title" to Pair("プレスモード ログイン", "Press Mode Login"),
        "press_login_user_id" to Pair("ユーザーID", "User ID"),
        "press_login_password" to Pair("パスワード", "Password"),
        "press_login_button" to Pair("ログイン", "Log In"),
        "press_login_error_invalid_credentials" to Pair("ユーザーIDまたはパスワードが正しくありません", "Invalid user ID or password"),
        "press_login_error_expired" to Pair("アカウントの有効期限が切れています", "Account has expired"),
        "press_login_error_deactivated" to Pair("アカウントが無効化されています", "Account has been deactivated"),
        "press_login_error_invalid" to Pair("アカウントが無効です", "Account is invalid"),
        "press_login_error_failed" to Pair("ログインに失敗しました。もう一度お試しください。", "Login failed. Please try again."),
        "press_login_error_network" to Pair("ネットワークに接続できません", "Unable to connect to network"),
        "press_logout" to Pair("ログアウト", "Log Out"),
        "press_logout_confirm" to Pair("プレスモードからログアウトしますか？", "Log out from Press Mode?"),
        "press_logout_confirm_message" to Pair(
            "ログアウトすると、プレスモードの機能が利用できなくなります。",
            "Logging out will disable Press Mode features."
        ),
        "press_account_status" to Pair("アカウント状態", "Account Status"),
        "press_account_organization" to Pair("組織名", "Organization"),
        "press_account_type" to Pair("組織種別", "Organization Type"),
        "press_account_contact" to Pair("担当者", "Contact Person"),
        "press_account_email" to Pair("メールアドレス", "Email"),
        "press_account_phone" to Pair("電話番号", "Phone"),
        "press_account_approved_by" to Pair("承認者", "Approved By"),
        "press_account_approved_date" to Pair("承認日", "Approved Date"),
        "press_account_expires" to Pair("有効期限", "Expires"),
        "press_account_status_label" to Pair("ステータス", "Status"),
        "press_account_last_login" to Pair("最終ログイン", "Last Login"),
        "press_account_days_remaining" to Pair("残り日数", "Days Remaining"),
        "press_account_days_remaining_format" to Pair("残り{days}日", "{days} days remaining"),
        "press_account_no_expiration" to Pair("無期限", "No Expiration"),
        "press_status_active" to Pair("アクティブ", "Active"),
        "press_status_expired" to Pair("期限切れ", "Expired"),
        "press_status_deactivated" to Pair("無効化", "Deactivated"),
        "press_info_title" to Pair("プレスモードについて", "About Press Mode"),
        "press_info_description" to Pair(
            "プレスモードは、報道関係者や教育者など、承認されたユーザー向けの特別なモードです。",
            "Press Mode is a special mode for approved users such as press members and educators."
        ),
        "press_info_target_users" to Pair("対象ユーザー", "Target Users"),
        "press_info_target_press" to Pair("報道関係者", "Press Members"),
        "press_info_target_educators" to Pair("教育関係者", "Educators"),
        "press_info_target_researchers" to Pair("研究者", "Researchers"),
        "press_info_target_facility" to Pair("施設関係者", "Facility Staff"),
        "press_info_how_to_apply" to Pair("申請方法", "How to Apply"),
        "press_info_step1" to Pair("1. ウェブサイトで申請フォームに記入", "1. Fill out the application form on our website"),
        "press_info_step2" to Pair("2. 審査・承認（通常3営業日以内）", "2. Review and approval (usually within 3 business days)"),
        "press_info_step3" to Pair("3. ログイン情報の受け取り", "3. Receive login credentials"),
        "press_info_apply_link" to Pair("申請はこちら", "Apply Here"),
        "press_info_website" to Pair("miterundesu.jp/press", "miterundesu.jp/press"),
        "press_renewal_title" to Pair("アカウント更新", "Account Renewal"),
        "press_renewal_message" to Pair(
            "アカウントの有効期限が切れています。更新をお申し込みください。",
            "Your account has expired. Please apply for renewal."
        ),
        "press_renewal_link" to Pair("更新申請", "Apply for Renewal"),
        "press_mode_features" to Pair("プレスモードの機能", "Press Mode Features"),
        "press_mode_feature_no_security" to Pair("セキュリティ制限の解除", "Security restrictions removed"),
        "press_mode_feature_watermark" to Pair("透かし情報の記録", "Watermark information recorded"),
        "press_mode_feature_extended" to Pair("拡張機能の利用", "Extended features available"),
        "press_not_logged_in" to Pair("未ログイン", "Not logged in"),
        "press_login_link" to Pair("プレスモード ログイン", "Press Mode Login"),
        "press_apply_link" to Pair("プレスアカウントの申請方法", "How to apply for a Press Account"),

        // ===== Gallery =====
        "gallery_title" to Pair("撮影画像", "Captured Images"),
        "no_images" to Pair("撮影した画像はありません", "No captured images"),
        "image_count" to Pair("{count}枚", "{count} images"),
        "image_count_format" to Pair("{count}枚の画像", "{count} image(s)"),
        "delete_image" to Pair("画像を削除", "Delete Image"),
        "delete_image_confirm" to Pair("この画像を削除しますか？", "Delete this image?"),
        "delete_all" to Pair("すべて削除", "Delete All"),
        "delete_all_confirm" to Pair("すべての画像を削除しますか？", "Delete all images?"),
        "time_remaining" to Pair("残り時間", "Time Remaining"),
        "image_expired" to Pair("期限切れ", "Expired"),
        "page_indicator" to Pair("{current}/{total}", "{current}/{total}"),
        "page_indicator_format" to Pair("{current}枚目 / {total}枚中", "Image {current} of {total}"),
        "gallery_empty_message" to Pair(
            "撮影した画像がここに表示されます。\n画像は10分後に自動的に削除されます。",
            "Captured images will appear here.\nImages are automatically deleted after 10 minutes."
        ),

        // ===== Tutorial =====
        "tutorial_welcome_title" to Pair("ミテルンデスへようこそ", "Welcome to Miterundesu"),
        "tutorial_welcome_message" to Pair(
            "視覚障害者や高齢者の方が、文化施設などで快適にご利用いただけるカメラ拡大鏡アプリです。",
            "A camera magnifier app designed for visually impaired and elderly users at cultural facilities."
        ),
        "tutorial_start" to Pair("はじめる", "Get Started"),
        "tutorial_skip" to Pair("スキップ", "Skip"),
        "tutorial_next" to Pair("次へ", "Next"),
        "tutorial_previous" to Pair("前へ", "Previous"),
        "tutorial_done" to Pair("完了", "Done"),
        "tutorial_step_zoom_title" to Pair("ズーム操作", "Zoom Controls"),
        "tutorial_step_zoom_description" to Pair(
            "ピンチ操作またはボタンで画面を拡大・縮小できます。1xボタンを長押しすると一時的にリセットされ、離すと元に戻ります。",
            "Pinch or use buttons to zoom in/out. Long-press the 1x button to temporarily reset, and release to restore."
        ),
        "tutorial_step_capture_title" to Pair("写真撮影", "Capture Photos"),
        "tutorial_step_capture_description" to Pair(
            "シャッターボタンをタップして撮影します。撮影画像は10分後に自動的に削除されます。",
            "Tap the shutter button to capture. Captured images are automatically deleted after 10 minutes."
        ),
        "tutorial_step_theater_title" to Pair("シアターモード", "Theater Mode"),
        "tutorial_step_theater_description" to Pair(
            "美術館や劇場でのご利用時にONにしてください。画面の色が変わり、一定時間後にUIが自動的に非表示になります。",
            "Turn this on when visiting museums or theaters. The screen color changes and UI auto-hides after a period."
        ),
        "tutorial_step_message_title" to Pair("スクロールメッセージ", "Scrolling Message"),
        "tutorial_step_message_description" to Pair(
            "画面上部にメッセージがスクロール表示されます。周囲の方にアプリの使用目的を伝えることができます。設定から編集可能です。",
            "A message scrolls at the top of the screen to inform others about the app's purpose. Editable in Settings."
        ),
        "tutorial_step_settings_title" to Pair("設定", "Settings"),
        "tutorial_step_settings_description" to Pair(
            "ズーム倍率、言語、スクロールメッセージなどの設定を変更できます。",
            "Change zoom level, language, scrolling message, and more."
        ),
        "tutorial_completion_title" to Pair("準備完了！", "All Set!"),
        "tutorial_completion_message" to Pair(
            "チュートリアルが完了しました。それでは、ミテルンデスをお楽しみください！",
            "Tutorial complete. Enjoy using Miterundesu!"
        ),
        "tutorial_completion_button" to Pair("使い始める", "Start Using"),
        "tutorial_step_indicator" to Pair("ステップ {current}/{total}", "Step {current}/{total}"),

        // ===== Settings =====
        "settings_title" to Pair("設定", "Settings"),
        "camera_settings" to Pair("カメラ設定", "Camera Settings"),
        "language_setting" to Pair("言語設定", "Language"),
        "language_japanese" to Pair("日本語", "日本語"),
        "language_english" to Pair("English", "English"),
        "scrolling_message" to Pair("スクロールメッセージ", "Scrolling Message"),
        "scrolling_message_normal" to Pair("通常モード", "Normal Mode"),
        "scrolling_message_theater" to Pair("シアターモード", "Theater Mode"),
        "scrolling_message_description" to Pair(
            "画面上部に表示されるメッセージを編集できます。",
            "Edit the message displayed at the top of the screen."
        ),
        "reset_settings" to Pair("設定をリセット", "Reset Settings"),
        "reset_confirm" to Pair("設定をリセットしますか？", "Reset all settings?"),
        "reset_confirm_message" to Pair(
            "すべての設定がデフォルト値に戻ります。この操作は取り消せません。",
            "All settings will be restored to defaults. This cannot be undone."
        ),
        "app_info" to Pair("アプリ情報", "App Info"),
        "version" to Pair("バージョン", "Version"),
        "version_format" to Pair("バージョン {number}", "Version {number}"),
        "tutorial_show" to Pair("チュートリアルを表示", "Show Tutorial"),
        "terms_of_service" to Pair("利用規約", "Terms of Service"),
        "privacy_policy" to Pair("プライバシーポリシー", "Privacy Policy"),
        "press_mode_section" to Pair("プレスモード", "Press Mode"),
        "settings_zoom_step" to Pair("10刻み", "Steps of 10"),
        "offline_warning" to Pair(
            "オフラインのため、プレスモード関連の機能が制限されています。",
            "Press Mode features are limited because you are offline."
        ),

        // ===== Accessibility =====
        "accessibility_shutter_button" to Pair("撮影ボタン", "Capture Button"),
        "accessibility_zoom_in" to Pair("拡大ボタン", "Zoom In Button"),
        "accessibility_zoom_out" to Pair("縮小ボタン", "Zoom Out Button"),
        "accessibility_zoom_reset" to Pair("ズームリセットボタン", "Zoom Reset Button"),
        "accessibility_theater_toggle" to Pair("シアターモード切替ボタン", "Theater Mode Toggle"),
        "accessibility_settings_button" to Pair("設定ボタン", "Settings Button"),
        "accessibility_explanation_button" to Pair("説明ボタン", "Explanation Button"),
        "accessibility_thumbnail" to Pair("撮影した写真を表示", "View captured photos"),
        "accessibility_close" to Pair("閉じる", "Close"),
        "accessibility_gallery" to Pair("画像ギャラリー", "Image Gallery"),
        "accessibility_time_remaining" to Pair("残り時間: {minutes}分{seconds}秒", "Time remaining: {minutes}m {seconds}s"),
        "accessibility_page_indicator" to Pair("{total}枚中{current}枚目", "Image {current} of {total}"),
        "accessibility_image_viewer" to Pair("画像ビューアー", "Image Viewer"),
        "accessibility_capture_preview" to Pair("撮影プレビュー", "Capture Preview"),
        "accessibility_zoom_level" to Pair("現在のズーム倍率: {zoom}倍", "Current zoom level: {zoom}x"),
        "accessibility_theater_mode_on" to Pair("シアターモード: オン", "Theater Mode: On"),
        "accessibility_theater_mode_off" to Pair("シアターモード: オフ", "Theater Mode: Off"),
        "accessibility_image_expired" to Pair("この画像は期限切れです", "This image has expired"),
        "accessibility_delete_button" to Pair("画像を削除", "Delete image"),
        "accessibility_password_toggle" to Pair("パスワードの表示/非表示", "Show/hide password"),
        "accessibility_loading" to Pair("読み込み中", "Loading"),
        "accessibility_error" to Pair("エラーが発生しました", "An error occurred"),
        "accessibility_scroll_message" to Pair("スクロールメッセージ表示中", "Scrolling message displayed"),
        "accessibility_logo" to Pair("ミテルンデスのロゴ", "Miterundesu logo"),
        "accessibility_watermark" to Pair("透かし情報", "Watermark information"),

        // ===== Time Format =====
        "time_minutes_seconds" to Pair("{minutes}分{seconds}秒", "{minutes}m {seconds}s"),
        "time_remaining_format" to Pair("残り{time}", "{time} remaining"),
        "time_expired" to Pair("期限切れ", "Expired"),
        "time_remaining_short" to Pair("{minutes}:{seconds}", "{minutes}:{seconds}"),
        "time_just_now" to Pair("たった今", "Just now"),
        "time_minutes_ago" to Pair("{minutes}分前", "{minutes} min ago"),
        "time_hours_ago" to Pair("{time}時間前", "{time} hours ago"),

        // ===== Image Deleted =====
        "image_deleted_title" to Pair("自動削除されました", "Automatically Deleted"),
        "image_deleted_reason" to Pair(
            "プライバシー保護のため、撮影から10分が経過した画像は自動的に削除されます。",
            "For privacy protection, images are automatically deleted 10 minutes after capture."
        ),
        "image_deleted_close" to Pair("閉じる", "Close"),

        // ===== Explanation Screen =====
        "explanation_title" to Pair("このアプリについて", "About This App"),
        "explanation_purpose_title" to Pair("アプリの目的", "App Purpose"),
        "explanation_purpose_description" to Pair(
            "ミテルンデスは、視覚障害者や高齢者の方が、美術館・博物館・劇場などの文化施設で展示物や舞台をより快適にご覧いただくための、プライバシー重視のカメラ拡大鏡アプリです。",
            "Miterundesu is a privacy-focused camera magnifier app designed to help visually impaired and elderly users view exhibits and performances at cultural facilities."
        ),
        "explanation_how_to_use_title" to Pair("このアプリの使い方", "How to Use"),
        "explanation_how_to_use_description" to Pair(
            "カメラを対象物に向けて、ピンチ操作やボタンで拡大してご覧ください。撮影した画像は10分後に自動的に削除されます。",
            "Point the camera at the subject and zoom in using pinch or buttons. Captured images are automatically deleted after 10 minutes."
        ),
        "explanation_why_needed_title" to Pair("なぜこのアプリが必要か", "Why This App is Needed"),
        "explanation_why_needed_description" to Pair(
            "文化施設では写真撮影が禁止されていることが多く、視覚障害者の方が展示物を十分に楽しめないことがあります。このアプリは、撮影画像を自動削除することで、施設のルールを守りながら、すべての人が文化を楽しめるようにします。",
            "Photography is often prohibited at cultural facilities, making it difficult for visually impaired people to fully enjoy exhibits. This app automatically deletes captured images, allowing everyone to enjoy culture while respecting facility rules."
        ),
        "explanation_theater_title" to Pair("劇場・美術館での利用について", "Use at Theaters & Museums"),
        "explanation_theater_description" to Pair(
            "シアターモードをONにすると、文化施設での利用に最適化されます。画面の色がオレンジに変わり、一定時間操作がないとUIが自動的に非表示になります。",
            "Turn on Theater Mode for optimized use at cultural facilities. The screen turns orange and the UI auto-hides after a period of inactivity."
        ),
        "explanation_privacy_title" to Pair("プライバシーについて", "About Privacy"),
        "explanation_privacy_description" to Pair(
            "撮影した画像は10分後に自動削除されます。スクリーンショットや画面録画も防止されます。",
            "Captured images are automatically deleted after 10 minutes. Screenshots and screen recording are also prevented."
        ),
        "explanation_website" to Pair("ウェブサイト", "Website"),
        "explanation_social_x" to Pair("X（Twitter）", "X (Twitter)"),
        "explanation_social_instagram" to Pair("Instagram", "Instagram"),

        // ===== WhatsNew =====
        "whats_new_title" to Pair("新機能", "What's New"),
        "whats_new_version" to Pair("バージョン {number}", "Version {number}"),
        "whats_new_dismiss" to Pair("了解", "Got It"),
        "whats_new_feature_1x_hold_title" to Pair("1xボタン長押し機能", "1x Button Long-Press"),
        "whats_new_feature_1x_hold_description" to Pair(
            "1xボタンを長押しすると、一時的にズームが1xにリセットされます。ボタンを離すと元のズーム倍率に戻ります。",
            "Long-press the 1x button to temporarily reset zoom to 1x. Release to restore the previous zoom level."
        ),

        // ===== Onboarding =====
        "onboarding_welcome" to Pair("ようこそ", "Welcome"),
        "onboarding_get_started" to Pair("はじめましょう", "Get Started"),
        "onboarding_feature_camera" to Pair("カメラ拡大鏡", "Camera Magnifier"),
        "onboarding_feature_camera_desc" to Pair(
            "高品質なカメラで対象物を拡大して見ることができます。",
            "View objects magnified through a high-quality camera."
        ),
        "onboarding_feature_privacy" to Pair("プライバシー保護", "Privacy Protection"),
        "onboarding_feature_privacy_desc" to Pair(
            "撮影画像は10分後に自動削除。スクリーンショットも防止されます。",
            "Captured images auto-delete after 10 minutes. Screenshots are also prevented."
        ),
        "onboarding_feature_theater" to Pair("シアターモード", "Theater Mode"),
        "onboarding_feature_theater_desc" to Pair(
            "美術館や劇場での利用に最適化されたモードです。",
            "A mode optimized for museums and theaters."
        ),
        "onboarding_skip" to Pair("スキップ", "Skip"),

        // ===== Watermark =====
        "watermark_date_format" to Pair("yyyy/MM/dd HH:mm", "yyyy/MM/dd HH:mm"),
        "watermark_id_prefix" to Pair("ID:", "ID:"),

        // ===== Network =====
        "network_offline" to Pair("オフラインです", "You are offline"),
        "network_online" to Pair("オンラインに復帰しました", "Back online"),
        "network_required" to Pair("この機能にはインターネット接続が必要です", "Internet connection required for this feature"),

        // ===== Permissions =====
        "permission_camera_title" to Pair("カメラへのアクセス", "Camera Access"),
        "permission_camera_message" to Pair(
            "このアプリはカメラを使用して対象物を拡大表示します。",
            "This app uses the camera to magnify objects."
        ),
        "permission_camera_denied" to Pair(
            "カメラの使用が許可されていません。設定アプリからカメラへのアクセスを許可してください。",
            "Camera access is not granted. Please allow camera access in the Settings app."
        ),
        "permission_go_to_settings" to Pair("設定を開く", "Open Settings"),

        // ===== Zoom Controls =====
        "zoom_button_in" to Pair("+", "+"),
        "zoom_button_out" to Pair("-", "-"),
        "zoom_button_reset" to Pair("1x", "1x"),
        "zoom_continuous_hint" to Pair("長押しで連続ズーム", "Hold for continuous zoom"),
        "zoom_reset_hint" to Pair("長押しで一時リセット", "Hold to temporarily reset"),
        "zoom_max_reached" to Pair("最大ズームに達しました", "Maximum zoom reached"),
        "zoom_min_reached" to Pair("最小ズームに達しました", "Minimum zoom reached"),

        // ===== Image Preview =====
        "preview_title" to Pair("プレビュー", "Preview"),
        "preview_close" to Pair("閉じる", "Close"),
        "preview_zoom_controls" to Pair("ズームコントロール", "Zoom Controls"),
        "preview_double_tap_reset" to Pair("ダブルタップでリセット", "Double-tap to reset"),

        // ===== Error Messages =====
        "error_generic" to Pair("エラーが発生しました", "An error occurred"),
        "error_network" to Pair("ネットワークエラーが発生しました", "A network error occurred"),
        "error_timeout" to Pair("タイムアウトしました", "Request timed out"),
        "error_server" to Pair("サーバーエラーが発生しました", "A server error occurred"),
        "error_unknown" to Pair("不明なエラーが発生しました", "An unknown error occurred"),
        "error_database" to Pair("データベースエラーが発生しました", "A database error occurred"),
        "error_image_load" to Pair("画像の読み込みに失敗しました", "Failed to load image"),
        "error_image_save" to Pair("画像の保存に失敗しました", "Failed to save image"),

        // ===== Confirmation Dialogs =====
        "confirm_delete_image" to Pair(
            "この画像を削除しますか？この操作は取り消せません。",
            "Delete this image? This cannot be undone."
        ),
        "confirm_delete_all_images" to Pair(
            "すべての画像を削除しますか？この操作は取り消せません。",
            "Delete all images? This cannot be undone."
        ),
        "confirm_reset_settings" to Pair(
            "すべての設定をデフォルト値に戻しますか？",
            "Reset all settings to defaults?"
        ),
        "confirm_logout" to Pair(
            "プレスモードからログアウトしますか？",
            "Log out from Press Mode?"
        ),

        // ===== Shutter =====
        "shutter_button" to Pair("シャッター", "Shutter"),
        "shutter_capturing" to Pair("撮影中", "Capturing"),

        // ===== Footer =====
        "footer_thumbnail" to Pair("サムネイル", "Thumbnail"),
        "footer_zoom_level" to Pair("ズーム倍率", "Zoom Level"),

        // ===== Header =====
        "header_logo" to Pair("ロゴ", "Logo"),
        "header_scrolling_message" to Pair("スクロールメッセージ", "Scrolling Message"),

        // ===== Press Account Status Display =====
        "press_status_icon_active" to Pair("有効", "Active"),
        "press_status_icon_expired" to Pair("期限切れ", "Expired"),
        "press_status_icon_deactivated" to Pair("無効", "Deactivated"),

        // ===== Tutorial Spotlight =====
        "spotlight_tap_to_continue" to Pair("タップして続ける", "Tap to continue"),
        "spotlight_step" to Pair("ステップ {current}/{total}", "Step {current}/{total}"),

        // ===== Auto-delete =====
        "auto_delete_info" to Pair(
            "撮影画像は10分後に自動的に削除されます",
            "Captured images are automatically deleted after 10 minutes"
        ),
        "auto_delete_timer" to Pair("自動削除まで: {time}", "Auto-delete in: {time}"),

        // ===== Misc =====
        "no_data" to Pair("データなし", "No data"),
        "unknown" to Pair("不明", "Unknown"),
        "not_available" to Pair("利用不可", "Not available"),
        "tap_to_retry" to Pair("タップして再試行", "Tap to retry"),
        "swipe_to_navigate" to Pair("スワイプして移動", "Swipe to navigate"),
        "pinch_to_zoom" to Pair("ピンチでズーム", "Pinch to zoom"),
        "double_tap_to_reset" to Pair("ダブルタップでリセット", "Double-tap to reset"),
        "pull_to_refresh" to Pair("引いて更新", "Pull to refresh"),

        // ===== Scrolling Message Defaults =====
        "default_scrolling_message_normal" to Pair(
            "すみません、目が不自由なのでこのアプリで拡大して見ています",
            "Excuse me, I have a visual impairment and am using this app to magnify"
        ),
        "default_scrolling_message_theater" to Pair(
            "すみません、拡大して見ています",
            "Excuse me, I am using magnification"
        ),

        // ===== Badge =====
        "badge_image_count" to Pair("{count}", "{count}"),
        "badge_time_remaining" to Pair("{minutes}m {seconds}s", "{minutes}m {seconds}s"),
        "badge_expired" to Pair("期限切れ", "Expired"),

        // ===== Image Gallery Specifics =====
        "gallery_zoom_controls" to Pair("ズームコントロール", "Zoom Controls"),
        "gallery_page_indicator" to Pair("ページインジケーター", "Page Indicator"),
        "gallery_swipe_hint" to Pair("左右にスワイプして画像を切り替え", "Swipe left/right to switch images"),
        "gallery_pinch_hint" to Pair("ピンチで画像を拡大", "Pinch to zoom image"),
        "gallery_close" to Pair("ギャラリーを閉じる", "Close Gallery"),

        // ===== Press Mode Login Specifics =====
        "press_login_show_password" to Pair("パスワードを表示", "Show Password"),
        "press_login_hide_password" to Pair("パスワードを隠す", "Hide Password"),
        "press_login_user_id_placeholder" to Pair("ユーザーIDを入力", "Enter User ID"),
        "press_login_password_placeholder" to Pair("パスワードを入力", "Enter Password"),
        "press_login_loading" to Pair("ログイン中...", "Logging in..."),
        "press_login_about" to Pair("プレスアカウントについて", "About Press Accounts"),

        // ===== Explanation Screen Detail =====
        "explanation_accessibility_title" to Pair("アクセシビリティ", "Accessibility"),
        "explanation_accessibility_description" to Pair(
            "このアプリはTalkBack（スクリーンリーダー）に完全対応しています。",
            "This app is fully compatible with TalkBack (screen reader)."
        ),
        "explanation_auto_delete_title" to Pair("10分自動削除", "10-Minute Auto-Delete"),
        "explanation_auto_delete_description" to Pair(
            "撮影した画像は10分後に自動的に削除されるため、施設のルールを守りながら安心してご利用いただけます。",
            "Captured images are automatically deleted after 10 minutes, so you can use the app with confidence while respecting facility rules."
        ),

        // ===== Version Specific =====
        "version_1_0_0" to Pair("初期リリース", "Initial Release"),
        "version_1_1_0" to Pair("1xボタン長押し機能を追加", "Added 1x button long-press feature"),

        // ===== Settings Detail =====
        "settings_zoom_current" to Pair("現在: {zoom}x", "Current: {zoom}x"),
        "settings_language_current" to Pair("現在の言語: {language}", "Current language: {language}"),
        "settings_message_hint_normal" to Pair("通常モードのメッセージ", "Normal mode message"),
        "settings_message_hint_theater" to Pair("シアターモードのメッセージ", "Theater mode message"),
        "settings_press_logged_in" to Pair("ログイン中: {name}", "Logged in: {name}"),
        "settings_press_not_logged_in" to Pair("未ログイン", "Not logged in"),

        // ===== Additional Accessibility =====
        "accessibility_image_number" to Pair("{current}枚目の画像（全{total}枚）", "Image {current} of {total}"),
        "accessibility_time_format" to Pair("残り{minutes}分{seconds}秒", "{minutes} minutes {seconds} seconds remaining"),
        "accessibility_swipe_left" to Pair("左にスワイプして次の画像", "Swipe left for next image"),
        "accessibility_swipe_right" to Pair("右にスワイプして前の画像", "Swipe right for previous image"),
        "accessibility_double_tap" to Pair("ダブルタップで決定", "Double-tap to activate"),
        "accessibility_pinch_zoom" to Pair("ピンチで拡大・縮小", "Pinch to zoom in/out"),
        "accessibility_image_deleted" to Pair("画像が自動削除されました", "Image has been automatically deleted"),
        "accessibility_tutorial_step" to Pair("チュートリアル ステップ{current}/{total}", "Tutorial step {current} of {total}"),
        "accessibility_press_status" to Pair("プレスアカウントステータス: {status}", "Press account status: {status}"),
        "accessibility_network_status" to Pair("ネットワーク: {status}", "Network: {status}"),
        "accessibility_connected" to Pair("接続済み", "Connected"),
        "accessibility_disconnected" to Pair("未接続", "Disconnected"),
    )

    fun localizedString(key: String): String {
        val pair = strings[key] ?: return key
        return if (settingsManager.language.value == "en") pair.second else pair.first
    }

    fun localizedString(key: String, vararg replacements: Pair<String, String>): String {
        var result = localizedString(key)
        for ((placeholder, value) in replacements) {
            result = result.replace("{$placeholder}", value)
        }
        return result
    }

    fun localizedString(key: String, replacements: Map<String, String>): String {
        var result = localizedString(key)
        for ((placeholder, value) in replacements) {
            result = result.replace("{$placeholder}", value)
        }
        return result
    }

    fun formatTimeRemaining(minutes: Int, seconds: Int): String {
        val timeStr = localizedString(
            "time_minutes_seconds",
            "minutes" to minutes.toString(),
            "seconds" to seconds.toString().padStart(2, '0')
        )
        return localizedString("time_remaining_format", "time" to timeStr)
    }

    fun formatPageIndicator(current: Int, total: Int): String {
        return localizedString(
            "page_indicator_format",
            "current" to current.toString(),
            "total" to total.toString()
        )
    }

    fun formatZoomLevel(zoom: Float): String {
        return localizedString(
            "zoom_factor_format",
            "zoom" to String.format("%.1f", zoom)
        )
    }

    fun formatDaysRemaining(days: Long): String {
        return localizedString(
            "press_account_days_remaining_format",
            "days" to days.toString()
        )
    }

    fun formatImageCount(count: Int): String {
        return localizedString(
            "image_count_format",
            "count" to count.toString()
        )
    }

    fun formatAccessibilityTimeRemaining(minutes: Int, seconds: Int): String {
        return localizedString(
            "accessibility_time_remaining",
            "minutes" to minutes.toString(),
            "seconds" to seconds.toString()
        )
    }

    fun formatTutorialStep(current: Int, total: Int): String {
        return localizedString(
            "tutorial_step_indicator",
            "current" to current.toString(),
            "total" to total.toString()
        )
    }

    val allKeys: Set<String>
        get() = strings.keys

    val stringCount: Int
        get() = strings.size
}
