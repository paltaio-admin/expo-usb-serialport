import { NativeModules } from 'react-native'

export const {
  CODE_DEVICE_NOT_FOUND,
  CODE_DRIVER_NOT_FOUND,
  CODE_NOT_ENOUGH_PORTS,
  CODE_PERMISSION_DENIED,
  CODE_OPEN_FAILED,
  CODE_DEVICE_NOT_OPEN,
  CODE_SEND_FAILED,
  CODE_READ_FAILED,
  CODE_DEVICE_NOT_OPEN_OR_CLOSED,
} = NativeModules.UsbSerialPortForAndroid.getConstants()

export const Codes = {
  DEVICE_NOT_FOUND: CODE_DEVICE_NOT_FOUND,
  DRIVER_NOT_FOUND: CODE_DRIVER_NOT_FOUND,
  NOT_ENOUGH_PORTS: CODE_NOT_ENOUGH_PORTS,
  PERMISSION_DENIED: CODE_PERMISSION_DENIED,
  OPEN_FAILED: CODE_OPEN_FAILED,
  DEVICE_NOT_OPEN: CODE_DEVICE_NOT_OPEN,
  SEND_FAILED: CODE_SEND_FAILED,
  READ_FAILED: CODE_READ_FAILED,
  DEVICE_NOT_OPEN_OR_CLOSED: CODE_DEVICE_NOT_OPEN_OR_CLOSED,
}

export enum Parity {
  None = 0,
  Odd,
  Even,
  Mark,
  Space,
};

export enum Driver {
  CDC_ACM = 'cdc-acm',
  FTDI = 'ftdi',
  CH34X = 'ch34x',
  CP21XX = 'cp21xx',
  CHROME_CCD = 'chrome-ccd',
  GSM_MODEM = 'gsm-modem',
  PROLIFIC = 'prolific',
}

export const DataReceivedEvent = 'usbSerialPortDataReceived'
