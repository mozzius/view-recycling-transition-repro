import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  isEnabled(): boolean;
  setEnabledAndRestart(enabled: boolean): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('NativeViewRecycling');
