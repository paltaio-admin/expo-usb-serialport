import { NativeEventEmitter, NativeModules } from 'react-native'
import UsbSerialportForAndroid, { Device } from './native_module'
import UsbSerial from './usb_serial'
import type { Parity } from './constants'

export { Device, UsbSerial }
export { Codes, DataReceivedEvent, Parity } from './constants'
export { EventData, Listener } from './usb_serial'

const eventEmitter = new NativeEventEmitter(
  NativeModules.UsbSerialportForAndroid,
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
   * DEVICE_NOT_FOND
   *
   * See {@link Codes}
   * @param deviceId
   */
  tryRequestPermission: (deviceId: number) => Promise<boolean>
  /**
   * May return error with these codes:
   * DEVICE_NOT_FOND
   *
   * See {@link Codes}
   * @param deviceId
   */
  hasPermission: (deviceId: number) => Promise<boolean>
  /**
   * May return error with these codes:
   * DEVICE_NOT_FOND
   * DRIVER_NOT_FOND
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
    return UsbSerialportForAndroid.list()
  },

  async tryRequestPermission(deviceId: number): Promise<boolean> {
    const result = await UsbSerialportForAndroid.tryRequestPermission(deviceId)
    return result === 1
  },

  hasPermission(deviceId: number): Promise<boolean> {
    return UsbSerialportForAndroid.hasPermission(deviceId)
  },

  async open(deviceId: number, options: OpenOptions): Promise<UsbSerial> {
    await UsbSerialportForAndroid.open(
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
