package com.samplewithkotlin.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule
import kotlin.math.abs

/**
 * コンパス機能を提供するクラス
 */
@ReactModule(name = CompassModule.NAME)
class CompassModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), SensorEventListener {
    // クラス内で使用する定数
    companion object {
        /** モジュール名 */
        const val NAME = "CompassModule"
    }

    /** センサー管理クラス */
    private var sensorManager: SensorManager? = null

    /** 現在の方位角（0°～360°） */
    private var currentAzimuth: Int = 0

    /** 方位角の更新時に使用する閾値（小さいほど微細な変化に反応する） */
    private var threshold: Int = 0

    /** 加速度センサーから取得した重力データを格納する配列 */
    private val gravity = FloatArray(3)

    /** 地磁気センサーから取得した磁場データを格納する配列 */
    private val geomagnetic = FloatArray(3)

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
     * コンパスのデータ取得を開始する
     * @param threshold 方位角の更新時に使用する閾値
     * @param promise 処理結果
     */
    @ReactMethod
    fun startCompassUpdates(threshold: Int, promise: Promise) {
        try {
            this.threshold = threshold

            sensorManager = reactContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)

            Log.d(NAME, "startCompassUpdates")
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(NAME, "startCompassUpdates", e)
            promise.reject("startCompassUpdates", e.message)
        }
    }

    /**
     * コンパスのデータ取得を停止する
     */
    @ReactMethod
    fun stopCompassUpdates() {
        sensorManager?.unregisterListener(this)
        Log.d(NAME, "stopCompassUpdates")
    }

    /**
     * センサーデータが更新された際に呼び出されるメソッド
     */
    override fun onSensorChanged(event: SensorEvent?) {
        // 低パスフィルタによってデータの変化を滑らかにするための係数（目安は0.9～0.99）
        // 値が大きい: データが安定して滑らかになるが、急激な変化に対応しづらい
        // 値が小さい: 反応が速くなるが、ノイズの影響を受けやすくなる
        val alpha = 0.97f

        synchronized(this) {
            when (event?.sensor?.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
                }

                Sensor.TYPE_MAGNETIC_FIELD -> {
                    geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0]
                    geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1]
                    geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2]
                }
            }

            // 回転行列と傾斜行列を計算
            val rotationMatrix = FloatArray(9)
            val inclinationMatrix = FloatArray(9)
            val success =
                SensorManager.getRotationMatrix(
                    rotationMatrix,
                    inclinationMatrix,
                    gravity,
                    geomagnetic
                )
            // 回転行列の計算に失敗した場合は処理を終了
            if (!success) {
                return
            }

            // 方位角（azimuth）を取得
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthRadians = orientation[0]

            // 方位角をラジアンから度に変換し、0°～360°に正規化
            val azimuthDegrees = Math.toDegrees(azimuthRadians.toDouble()).toFloat()
            val normalizedAzimuth = (azimuthDegrees + 360) % 360

            // 閾値未満の変化の場合は更新しない
            if (abs(currentAzimuth - normalizedAzimuth) <= threshold) {
                return
            }
            currentAzimuth = normalizedAzimuth.toInt()

            // 方位角をReact Native側にイベントとして送信
            val params = Arguments.createMap().apply {
                putInt("azimuth", currentAzimuth)
            }
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("onUpdateData", params)
        }
    }

    /**
     * センサー精度が変更された際に呼び出されるメソッド（今回は未使用）
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
