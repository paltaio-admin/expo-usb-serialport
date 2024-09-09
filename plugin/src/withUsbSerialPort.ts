import type { ConfigPlugin } from '@expo/config-plugins';

const withUsbSerialPort: ConfigPlugin = (config) => {
  return config;
};

export default withUsbSerialPort;
