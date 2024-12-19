import {Button, NativeModules, PermissionsAndroid, StyleSheet, Text, View} from 'react-native';
import React, {useEffect, useState} from 'react';
import {TypedNativeEventEmitter} from '../TypedNativeEventEmitter';
import type {SpeechRecognitionModuleEvents} from '../types/SpeechRecognitionModule';

/**
 * 音声録音の権限が既に付与されているかどうかを確認する
 * 付与されていない場合は、ユーザーに権限をリクエストする。
 * @returns 権限が付与されている場合は`true`、それ以外の場合は`false`を解決するプロミス。
 */
const ensureAudioRecordPermission = async () => {
  try {
    if (await PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.RECORD_AUDIO)) {
      return true;
    }
    const granted = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.RECORD_AUDIO);
    return granted === PermissionsAndroid.RESULTS.GRANTED;
  } catch (e) {
    console.error(e);
    return false;
  }
};

const {SpeechRecognitionModule} = NativeModules;

const SpeechRecognitionScreen = () => {
  const [result, setResult] = useState('');
  const [error, setError] = useState('');
  const [isRecording, setIsRecording] = useState(false);

  useEffect(() => {
    const speechRecognitionEmitter = new TypedNativeEventEmitter<SpeechRecognitionModuleEvents>(
      SpeechRecognitionModule,
    );

    const onResultsListener = speechRecognitionEmitter.addListener('onResults', ({text}) => {
      setResult(text);
    });

    const onErrorListener = speechRecognitionEmitter.addListener('onError', ({errorCode, errorMessage}) => {
      setError(`Error: ${errorMessage} (code: ${errorCode})`);
      setIsRecording(false);
    });

    return () => {
      onResultsListener.remove();
      onErrorListener.remove();
    };
  }, []);

  const handleStart = async () => {
    setError('');

    const granted = await ensureAudioRecordPermission();
    if (granted) {
      SpeechRecognitionModule.startListening();
      setIsRecording(true);
    } else {
      setError('Error: 音声録音の権限が必要です。');
    }
  };

  const handleStop = () => {
    SpeechRecognitionModule.stopListening();
    setIsRecording(false);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.result}>認識結果: {result}</Text>
      <Text style={styles.error}>{error}</Text>
      {!isRecording ? (
        <Button title="開始" color="blue" onPress={handleStart} />
      ) : (
        <Button title="終了" color="red" onPress={handleStop} />
      )}
    </View>
  );
};

export default SpeechRecognitionScreen;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  result: {
    color: '#fff',
  },
  error: {
    color: 'red',
    marginTop: 8,
  },
});
