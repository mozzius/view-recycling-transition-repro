import { useState } from 'react';
import { Button, StyleSheet, Switch, Text, View } from 'react-native';
import NativeViewRecycling from './specs/NativeViewRecycling';
import TransitionContainer from './specs/TransitionContainerNativeComponent';

type Step = 'idle' | 'child removed' | 'container removed' | 'views mounted';

function App() {
  const recycling = NativeViewRecycling.isEnabled();
  const [step, setStep] = useState<Step>('idle');

  function run() {
    // 1. Remove the child. TransitionContainer keeps it drawn for a 1s exit animation.
    setStep('child removed');
    // 2. Remove TransitionContainer before that animation ends.
    setTimeout(() => setStep('container removed'), 300);
    // 3. Mount new views. With recycling, the first one reuses the removed child.
    setTimeout(() => setStep('views mounted'), 600);
  }

  return (
    <View style={styles.screen}>
      <View style={styles.row}>
        <Text style={styles.label}>View recycling (restarts the app)</Text>
        <Switch
          accessibilityLabel="View recycling"
          value={recycling}
          onValueChange={NativeViewRecycling.setEnabledAndRestart}
        />
      </View>
      <Button title="Run" onPress={run} disabled={step !== 'idle'} />
      <Text style={styles.label}>Step: {step}</Text>

      {(step === 'idle' || step === 'child removed') && (
        <TransitionContainer style={styles.container}>
          {step === 'idle' && <View style={styles.child} />}
        </TransitionContainer>
      )}

      {step === 'views mounted' && (
        <View style={styles.grid}>
          {Array.from({ length: 10 }, (_, i) => (
            <View key={i} style={styles.cell} />
          ))}
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    gap: 16,
    padding: 24,
    paddingTop: 64,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  label: {
    fontSize: 16,
  },
  container: {
    height: 120,
    backgroundColor: 'gainsboro',
  },
  child: {
    width: 100,
    height: 100,
    margin: 10,
    backgroundColor: 'tomato',
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  cell: {
    width: 40,
    height: 40,
    backgroundColor: 'seagreen',
  },
});

export default App;
