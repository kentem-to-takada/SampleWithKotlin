import React, {useEffect, useState} from 'react';
import {Animated, Easing, Image, NativeModules, StyleSheet, Text, useAnimatedValue, View} from 'react-native';
import {TypedNativeEventEmitter} from '../TypedNativeEventEmitter';
import type {CompassModuleEvents} from '../types/compassModule';

const {CompassModule} = NativeModules;

const DIRECTIONS = [
  '北',
  '北北東',
  '北東',
  '東北東',
  '東',
  '東南東',
  '南東',
  '南南東',
  '南',
  '南南西',
  '南西',
  '西南西',
  '西',
  '西北西',
  '北西',
  '北北西',
] as const;

/**
 * 与えられた方位角に基づいてコンパスの方向を返す
 * @param azimuth 度単位の方位角値
 * @returns 方位角に対応するコンパスの方向
 */
const getDirection = (azimuth: number): string => {
  const index = Math.round(azimuth / 22.5) % 16;
  return DIRECTIONS[index];
};

const CompassScreen = () => {
  const [azimuth, setAzimuth] = useState(0);
  const [direction, setDirection] = useState(() => getDirection(azimuth));
  const rotateAnimation = useAnimatedValue(0);

  useEffect(() => {
    const compassEmitter = new TypedNativeEventEmitter<CompassModuleEvents>(CompassModule);

    const subscription = compassEmitter.addListener('onUpdateData', ({azimuth: _azimuth}) => {
      setAzimuth(_azimuth);
      setDirection(getDirection(_azimuth));

      Animated.timing(rotateAnimation, {
        toValue: _azimuth,
        duration: 200,
        easing: Easing.linear,
        useNativeDriver: true,
      }).start();
    });

    // コンパスの更新を開始
    (async () => {
      await CompassModule.startCompassUpdates(1);
    })();

    return () => {
      CompassModule.stopCompassUpdates();
      subscription.remove();
    };

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const rotate = rotateAnimation.interpolate({
    inputRange: [0, 360],
    outputRange: ['0deg', '360deg'],
  });

  return (
    <View style={styles.container}>
      <Text style={styles.text}>
        {direction}（{azimuth}°）
      </Text>
      <View style={styles.compassContainer}>
        <Image source={require('../../assets/compass_background.png')} style={styles.background} />
        <Animated.Image
          source={require('../../assets/compass_needle.png')}
          style={[styles.needle, {transform: [{rotate}]}]}
        />
      </View>
    </View>
  );
};

export default CompassScreen;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  text: {
    color: '#fff',
    fontSize: 20,
    marginBottom: 20,
  },
  compassContainer: {
    width: 240,
    height: 240,
    justifyContent: 'center',
    alignItems: 'center',
  },
  background: {
    position: 'absolute',
    width: 240,
    height: 240,
    resizeMode: 'contain',
  },
  needle: {
    width: 220,
    height: 220,
    position: 'absolute',
    resizeMode: 'contain',
  },
});
