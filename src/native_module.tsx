import { NativeModules } from 'react-native'
import type { Driver } from './constants'

export interface Device {
  readonly deviceId: number
  readonly vendorId: number
  readonly productId: number
}

export interface PortInfo {
  readonly dtr: boolean
  readonly rts: boolean
}

interface UsbSerialPortForAndroidAPI {
  list: () => Promise<Device[]>
  // return 1 if already has permission, 0 will request permission
  tryRequestPermission: (deviceId: number) => Promise<number>
  addDevice: (vendorId: number, productId: number, driver: Driver) => Promise<void>
  addListener: (eventName: string) => void
  removeListeners: (eventName: string, count: number) => void
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
  getPortInfo: (deviceId: number) => Promise<PortInfo | null>
  setPortDtrRts: (deviceId: number, opts: { dtr: boolean, rts: boolean }) => Promise<boolean>
}

const UsbSerialPortForAndroid: UsbSerialPortForAndroidAPI
  = NativeModules.UsbSerialPortForAndroid

export default UsbSerialPortForAndroid
