import { NativeEventEmitter, NativeModules } from 'react-native'
import UsbSerialPortForAndroid, { Device } from './native_module'
import UsbSerial from './usb_serial'
import type { Driver, Parity } from './constants'

export { Device, UsbSerial }
export { Codes, DataReceivedEvent, Driver, Parity } from './constants'
export { EventData, Listener } from './usb_serial'

const eventEmitter = new NativeEventEmitter(
  NativeModules.UsbSerialPortForAndroid,
)

export interface OpenOptions {
  baudRate: number
  parity: Parity
  dataBits: number
  stopBits: number
}

export interface Manager {
  list: () => Promise<Device[]>
  /**
   * Return true if already has permission, otherwise will request permission and return false.
   *
   * May return error with these codes:
   * DEVICE_NOT_FOUND
   *
   * See {@link Codes}
   * @param deviceId
   */
  addDevice: (vendorId: number, productId: number, driver: Driver) => Promise<void>
  tryRequestPermission: (deviceId: number) => Promise<boolean>
  /**
   * May return error with these codes:
   * DEVICE_NOT_FOUND
   *
   * See {@link Codes}
   * @param deviceId
   */
  hasPermission: (deviceId: number) => Promise<boolean>
  /**
   * May return error with these codes:
   * DEVICE_NOT_FOUND
   * DRIVER_NOT_FOUND
   * NOT_ENOUGH_PORTS
   * PERMISSION_DENIED
   * OPEN_FAILED
   *
   * See {@link Codes}
   * @param deviceId
   * @param options
   */
  open: (deviceId: number, options: OpenOptions) => Promise<UsbSerial>
}

const defaultManager: Manager = {
  list(): Promise<Device[]> {
    return UsbSerialPortForAndroid.list()
  },

  addDevice(vendorId: number, productId: number, driver: Driver): Promise<void> {
    return UsbSerialPortForAndroid.addDevice(vendorId, productId, driver)
  },

  async tryRequestPermission(deviceId: number): Promise<boolean> {
    const result = await UsbSerialPortForAndroid.tryRequestPermission(deviceId)
    return result === 1
  },

  hasPermission(deviceId: number): Promise<boolean> {
    return UsbSerialPortForAndroid.hasPermission(deviceId)
  },

  async open(deviceId: number, options: OpenOptions): Promise<UsbSerial> {
    await UsbSerialPortForAndroid.open(
      deviceId,
      options.baudRate,
      options.dataBits,
      options.stopBits,
      options.parity,
    )
    return new UsbSerial(deviceId, eventEmitter)
  },
}

export const UsbSerialManager: Manager = defaultManager
