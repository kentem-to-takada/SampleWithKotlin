import type {CompassModule} from './compassModule';
import type {SpeechRecognitionModule} from './SpeechRecognitionModule';

declare module 'react-native' {
  interface NativeModulesStatic {
    /** コンパス機能を提供するネイティブモジュール */
    CompassModule: CompassModule;
    /** 音声認識機能を提供するネイティブモジュール */
    SpeechRecognitionModule: SpeechRecognitionModule;
  }
}
