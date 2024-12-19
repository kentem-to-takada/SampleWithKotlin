# Native ModuleをKotlinで実装したサンプルアプリ

## 起動

```bash
npm run android
```

## コンパス機能

[android/app/src/main/java/com/samplewithkotlin/compass](./android/app/src/main/java/com/samplewithkotlin/compass)

<img src="./screenshots/compass.png" alt="compass_screenshot" width=360 />

```mermaid
sequenceDiagram
    actor User as ユーザー
    participant JS as JavaScript (React Native)
    participant Native as ネイティブモジュール (Kotlin)
    participant Sensors as 加速度センサー／地磁気センサー (Android)

    User ->> JS: アプリを起動
    JS ->> Native: startCompassUpdates()を呼び出し
    Native ->> Sensors: センサーリスナーを登録

    loop 方位データ取得
        Sensors ->> Native: onSensorChangedイベントを送信
        Native ->> JS: onUpdateDataイベントで方位データを送信
        JS ->> JS: データをUIに反映
    end

    User ->> JS: アプリを閉じる／機能を停止
    JS ->> Native: stopCompassUpdates()を呼び出し
    Native ->> Sensor: センサーリスナー解除／リソース解放
```

## 音声認識機能

[android/app/src/main/java/com/samplewithkotlin/speechRecognition](./android/app/src/main/java/com/samplewithkotlin/speechRecognition)

<img src="./screenshots/voice-recognition.png" alt="voice-recognition_screenshot" width=360 />

```mermaid
sequenceDiagram
    actor User as ユーザー
    participant JS as JavaScript（React Native）
    participant Native as ネイティブモジュール（Kotlin）
    participant Speech as SpeechRecognizer（Android）

    User ->> JS: ボタンを押して音声認識開始
    JS ->> Native: startListening()を呼び出し
    Native ->> Speech: 音声認識の初期化と開始

    loop 音声認識中
        Speech ->> Native: onResultsイベントを送信（部分的な結果）
        Native ->> JS: onResultsイベントを送信（部分的な結果）
    end

    Speech ->> Native: onResultsイベントを送信（最終結果）
    Native ->> JS: onResultsイベントを送信（最終結果）

    User ->> JS: ボタンを押して音声認識停止
    JS ->> Native: stopListening()を呼び出し
    Native ->> Speech: 音声認識を停止しリソース解放
```
