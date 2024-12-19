package com.samplewithkotlin.speechRecognition

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.util.Locale

/**
 * 音声認識機能を提供するクラス
 */
@ReactModule(name = SpeechRecognitionModule.NAME)
class SpeechRecognitionModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    // クラス内で使用する定数
    companion object {
        /** モジュール名 */
        const val NAME = "SpeechRecognitionModule"
    }

    /** メインスレッド用のハンドラー（音声認識はメインスレッドで実行する必要があるため） */
    private val mainHandler = Handler(Looper.getMainLooper())

    /** 音声認識サービスを提供するクラス */
    private var speechRecognizer: SpeechRecognizer? = null

    override fun getName() = NAME

    /**
     * イベントリスナーを追加するためのダミーメソッド（React Nativeの仕様上必要）
     */
    @ReactMethod
    fun addListener(eventName: String) {
        // KEEP: Required for RN built-in Event Emitter Calls
    }

    /**
     * イベントリスナーを削除するためのダミーメソッド（React Nativeの仕様上必要）
     */
    @ReactMethod
    fun removeListeners(count: Int) {
        // KEEP: Required for RN built-in Event Emitter Calls
    }

    /**
     * 音声認識を開始する
     */
    @ReactMethod
    fun startListening() {
        Log.d(NAME, "startListening")
        // メインスレッドで実行
        mainHandler.post {
            // 音声認識の設定を定義するIntentを作成
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                // オンラインで音声認識を行う場合はコメントアウト
                // ※ただし、事前にオフライン用の音声認識モデルをダウンロードしておく必要あり
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(reactContext).apply {
                setRecognitionListener(object : RecognitionListener {
                    // 音声認識の準備が完了した際のコールバック
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(NAME, "onReadyForSpeech")
                        sendEvent("onReady", null)
                    }

                    // 音声入力を開始した際のコールバック
                    override fun onBeginningOfSpeech() {
                        Log.d(NAME, "onBeginningOfSpeech")
                        sendEvent("onBeginning", null)
                    }

                    // 音量レベルが変化した際のコールバック（今回は未使用）
                    override fun onRmsChanged(rmsdB: Float) {}

                    // 音量データのバッファが利用可能になった際のコールバック（今回は未使用）
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    // 音声入力が終了した際のコールバック
                    override fun onEndOfSpeech() {
                        Log.d(NAME, "onEndOfSpeech")
                        sendEvent("onEnd", null)
                    }

                    // エラー発生時のコールバック
                    override fun onError(error: Int) {
                        Log.e(NAME, "onError[errorCode: $error]")
                        val message = errorCodeToMessage(error)
                        val params = Arguments.createMap().apply {
                            putInt("errorCode", error)
                            putString("errorMessage", message)
                        }
                        sendEvent("onError", params)
                    }

                    // 音声認識の結果が得られた際のコールバック
                    override fun onResults(results: Bundle?) {
                        Log.d(NAME, "onResults")
                        val matches =
                            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val params = Arguments.createMap().apply {
                            putString("text", matches?.joinToString(" "))
                        }
                        sendEvent("onResults", params)
                    }

                    // 部分的な音声認識結果が得られた際のコールバック（今回は未使用）
                    override fun onPartialResults(partialResults: Bundle?) {}

                    // その他のイベントが発生した際のコールバック（今回は未使用）
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
                startListening(intent)
            }
        }
    }

    /**
     * 音声認識を停止する
     */
    @ReactMethod
    fun stopListening() {
        Log.d(NAME, "stopListening")

        mainHandler.post {
            speechRecognizer?.apply {
                stopListening()
                destroy()
            }

            speechRecognizer = null
        }
    }

    /**
     * イベントをReact Native側に送信する
     * @param eventName イベント名
     * @param params イベントデータ
     */
    private fun sendEvent(eventName: String, params: WritableMap?) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    /**
     * エラーコードをエラーメッセージに変換する
     * @param error エラーコード
     * @return エラーメッセージ
     */
    private fun errorCodeToMessage(error: Int) = when (error) {
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network operation timed out."
        SpeechRecognizer.ERROR_NETWORK -> "Other network related errors."
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
        SpeechRecognizer.ERROR_SERVER -> "Server sends error status."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input."
        SpeechRecognizer.ERROR_NO_MATCH -> "No recognition result matched."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions."
        SpeechRecognizer.ERROR_TOO_MANY_REQUESTS -> "Too many requests from the same client."
        SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> "Server has been disconnected, e.g. because the app has crashed."
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> "Requested language is not available to be used with the current recognizer."
        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "Requested language is supported, but not available currently (e.g. not downloaded yet)."
        SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT -> "The service does not allow to check for support."
        SpeechRecognizer.ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS -> "The service does not support listening to model downloads events."
        else -> "An unexpected error has occurred."
    }
}
