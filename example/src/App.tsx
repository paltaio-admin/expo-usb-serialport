/* eslint-disable no-console */
import * as React from 'react'

import { Button, StyleSheet, Text, View } from 'react-native'
import { Parity } from 'src/constants'
import {
  UsbSerialManager,
} from 'src/index.android'
import type {
  Device,
  UsbSerial,
} from 'src/index.android'

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
})

export default function App() {
  const [result, setResult] = React.useState<Device[]>([])
  const usbSerialPort = React.useRef<UsbSerial | null>(null)

  React.useEffect(() => {
    UsbSerialManager.list().then((devices) => {
      console.log(devices)
      setResult(devices)
    })
  }, [])

  return (
    <View style={styles.container}>
      {result.map(device => (
        <Text key={device.deviceId}>
          deviceId:
          {' '}
          {device.deviceId}
          , venderId:
          {' '}
          {device.vendorId}
          , productId:
          {' '}
          {device.productId}
        </Text>
      ))}

      <Button
        title="request usb permission"
        onPress={async () => {
          try {
            await UsbSerialManager.tryRequestPermission(2004)
          }
          catch (err) {
            console.log(err)
          }
        }}
      />
      <Button
        title="open"
        onPress={async () => {
          try {
            usbSerialPort.current = await UsbSerialManager.open(2004, {
              baudRate: 38400,
              parity: Parity.None,
              dataBits: 8,
              stopBits: 1,
            })
            console.log('open success')
          }
          catch (err) {
            console.log(err)
          }
        }}
      />
      <Button
        title="close"
        onPress={async () => {
          try {
            await usbSerialPort.current?.close()
          }
          catch (err) {
            console.log(err)
          }
          console.log('close success')
        }}
      />
    </View>
  )
}
