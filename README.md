# expo-usb-serialport

React Native USB serialport module for Android platform based on [mik3y/usb-serial-for-android](https://github.com/mik3y/usb-serial-for-android)

## Installation

```sh
pnpm add @paltaio/expo-usb-serialport
```

## Usage

```js
import { UsbSerialManager, Parity, Codes } from "@paltaio/expo-usb-serialport";

// ...
const devices = await UsbSerialManager.list();

try {
  await UsbSerialManager.tryRequestPermission(2004);
  const usbSerialPort = await UsbSerialManager.open(2004, { baudRate: 38400, parity: Parity.None, dataBits: 8, stopBits: 1 });

  const sub = usbSerialPort.onReceived((event) => {
    console.log(event.deviceId, event.data);
  });
  // unsubscribe
  // sub.remove();

  await usbSerialPort.send('00FF');
  
  usbSerialPort.close();
} catch(err) {
  console.log(err);
  if (err.code === Codes.DEVICE_NOT_FOUND) {
    // ...
  }
}
```

See [documentation](https://bastengao.com/react-native-usb-serialport-for-android/) for details.

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
