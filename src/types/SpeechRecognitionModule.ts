import {NativeModule} from 'react-native';

export interface SpeechRecognitionModule extends NativeModule {
  /** 音声認識を開始する */
  startListening(): void;

  /** 音声認識を終了する */
  stopListening(): void;
}

export interface SpeechRecognitionModuleEvents {
  /** 音声認識の準備ができたときに発火するイベント */
  onReady: null;

  /** 音声認識が開始されたときに発火するイベント */
  onBeginning: null;

  /** 音声認識の結果が生成されたときに発火するイベント */
  onResults: {
    /** 音声認識認識の結果 */
    text: string;
  };

  /** 音声認識でエラーが発生したときに発火するイベント */
  onError: {
    /**
     * エラーコード
     * @see https://developer.android.com/reference/android/speech/RecognitionListener#onError(int)
     */
    errorCode: number;
    /** エラーメッセージ */
    errorMessage: string;
  };
}
