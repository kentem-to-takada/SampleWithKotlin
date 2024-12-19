import {NativeModule} from 'react-native';

export interface CompassModule extends NativeModule {
  /**
   * コンパスのデータ取得を開始する
   * @param threshold 方位角の更新時に使用する閾値
   * @returns 処理結果を表すPromiseオブジェクト
   */
  startCompassUpdates(threshold: number): Promise<Boolean>;

  /**
   * コンパスのデータ取得を停止する
   */
  stopCompassUpdates(): void;
}

export interface CompassModuleEvents {
  /**
   * データが更新されたときに発火するイベント
   */
  onUpdateData: {
    /** 方位角（0°～360°） */
    azimuth: number;
  };
}
