import { NativeModules } from 'react-native'

export interface Device {
  readonly deviceId: number
  readonly vendorId: number
  readonly productId: number
}

interface UsbSerialportForAndroidAPI {
  list: () => Promise<Device[]>
  // return 1 if already has permission, 0 will request permission
  tryRequestPermission: (deviceId: number) => Promise<number>
  hasPermission: (deviceId: number) => Promise<boolean>
  open: (
    deviceId: number,
    baudRate: number,
    dataBits: number,
    stopBits: number,
    parity: number
  ) => Promise<number>
  send: (deviceId: number, hexStr: string) => Promise<null>
  sendWithResponse: (
    deviceId: number,
    hexStr: string,
    bytes: number
  ) => Promise<null>
  close: (deviceId: number) => Promise<null>
}

const UsbSerialportForAndroid: UsbSerialportForAndroidAPI
  = NativeModules.UsbSerialportForAndroid

export default UsbSerialportForAndroid
