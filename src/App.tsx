import React from 'react';
import {SafeAreaView, StyleSheet} from 'react-native';
import {NavigationContainer} from '@react-navigation/native';
import {createMaterialTopTabNavigator} from '@react-navigation/material-top-tabs';
import CompassScreen from './screens/CompassScreen';
import SpeechRecognitionScreen from './screens/SpeechRecognitionScreen';

export const Tab = createMaterialTopTabNavigator();

const App = () => (
  <NavigationContainer>
    <SafeAreaView style={styles.container}>
      <Tab.Navigator
        screenOptions={{
          tabBarLabelStyle: {color: '#e3e3e3'},
          tabBarStyle: {backgroundColor: '#242526'},
          sceneStyle: {backgroundColor: '#1b1b1d'},
        }}>
        <Tab.Screen name="CompassTab" component={CompassScreen} options={{title: 'コンパス'}} />
        <Tab.Screen name="SpeechRecognitionTab" component={SpeechRecognitionScreen} options={{title: '音声認識'}} />
      </Tab.Navigator>
    </SafeAreaView>
  </NavigationContainer>
);

export default App;

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});
