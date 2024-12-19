import {type EmitterSubscription, NativeEventEmitter} from 'react-native';

/**
 * 型安全なNativeEventEmitterクラス。
 * `NativeEventEmitter`を拡張し、イベント名やペイロードに型を付けて安全に利用できるようにしたもの。
 * `Events`型パラメータを通じて、イベント名とそのペイロードの型をマッピングできるようにしている。
 */
export class TypedNativeEventEmitter<Events extends Record<string, any>> extends NativeEventEmitter {
  /**
   * イベントリスナーを追加する
   * @param eventName 登録するイベント名
   * @param listener イベント発火時に呼び出されるコールバック関数
   * @param context （オプション）このリスナーがバインドされるコンテキストオブジェクト
   * @returns 登録されたリスナーのサブスクリプションオブジェクト
   */
  public override addListener<K extends keyof Events>(
    eventName: K,
    listener: (payload: Events[K]) => void,
    context?: Object,
  ): EmitterSubscription {
    return super.addListener(eventName as string, listener, context);
  }
}
