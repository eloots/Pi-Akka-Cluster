# Exercise 19

## IoT Hub Device Setup
These steps show how to set up the Azure device in the Azure portal. This
includes creating a device and reviewing connection strings.

## Steps


### Register a device in the IoT hub

- Open the Iot Hub you just created if it is not alread open in the portal

- In your IoT hub navigation menu, open **IoT devices**, then click **Add** to
  register a device in your IoT hub.

- Enter **node-0** as the **Device ID** for the new device. Note that device IDs
  are case sensitive.

- Click **Save**.

After the device is created, open the device from the list in the **IoT
devices** pane.

- Note the **Connection string---primary key**.  We will be using this key
  later.

**NOTE:** This covers basic steps for the IoT Hub device setup that we need for
this tutorial.  For reference, have a look at the [full steps and
options](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-get-started)
