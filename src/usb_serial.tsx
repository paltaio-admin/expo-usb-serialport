import type { EventEmitter, EventSubscription } from 'react-native'
import { DataReceivedEvent } from './constants'
import UsbSerialPortForAndroid from './native_module'

export interface EventData {
  deviceId: number
  data: string
}

export type Listener = (data: EventData) => void

export default class UsbSerial {
  deviceId: number
  private eventEmitter: EventEmitter
  private listeners: Listener[]
  private subscriptions: EventSubscription[]

  constructor(deviceId: number, eventEmitter: EventEmitter) {
    this.deviceId = deviceId
    this.eventEmitter = eventEmitter
    this.listeners = []
    this.subscriptions = []
  }

  /**
   * Send data with hex string.
   *
   * May return error with these codes:
   * DEVICE_NOT_OPEN
   * SEND_FAILED
   *
   * See {@link Codes}
   * @param hexStr
   */
  send(hexStr: string): Promise<null> {
    return UsbSerialPortForAndroid.send(this.deviceId, hexStr)
  }

  /**
   * Send data with hex string & wait for a response
   *
   * May return error with these codes:
   * DEVICE_NOT_OPEN
   * SEND_FAILED
   * READ_FAILED
   *
   * See {@link Codes}
   * @param hexStr
   */
  sendWithResponse(hexStr: string, bytes: number): Promise<null> {
    return UsbSerialPortForAndroid.sendWithResponse(
      this.deviceId,
      hexStr,
      bytes,
    )
  }

  /**
   * Listen to data received event.
   *
   * @param listener
   * @returns EventSubscription
   */
  onReceived(listener: Listener) {
    const listenerProxy = (event: EventData) => {
      if (event.deviceId !== this.deviceId) {
        return
      }
      if (!event.data) {
        return
      }

      listener(event)
    }

    this.listeners.push(listenerProxy)
    const sub = this.eventEmitter.addListener(DataReceivedEvent, listenerProxy)
    this.subscriptions.push(sub)
    return sub
  }

  /**
   *
   * May return error with these codes:
   * DEVICE_NOT_OPEN_OR_CLOSED
   *
   * See {@link Codes}
   * @returns Promise<null>
   */
  close(): Promise<any> {
    for (const sub of this.subscriptions) {
      sub.remove()
    }
    return UsbSerialPortForAndroid.close(this.deviceId)
  }
}
