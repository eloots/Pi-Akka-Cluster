# Exercise 20

## Send telemetry from a device to an IoT hub
These steps show how to set up a simulated Raspberry pi device sending
temperature and humidity data from a simulated BME280 sensor. You will be using
java to simulate a device sending data to your IoT hub using the device
connection strings you created in the previous step.

## Steps

### Ensure you have maven on your laptop
To build the samples, you need to install Maven 3. You can download Maven for
multiple platforms from [Apache Maven](https://maven.apache.org/download.cgi).

You can verify the current version of Maven on your development machine using
the following command:

```cmd/sh
mvn --version
```

### Download the simulated device java code

Retrieve the code sample repo to your laptop:
```cmd/sh
git clone https://github.com/Azure-Samples/azure-iot-samples-java.git

```

You can also download a [zip file
here](https://github.com/Azure-Samples/azure-iot-samples-java/archive/master.zip)


### Send telemetry to the IoT Hub

The simulated device application connects to a device-specific endpoint on your
IoT hub, sends simulated telemetry, and listens for direct method calls from
your hub. In this quickstart, the direct method call from the hub tells the
device to change the interval at which it sends telemetry. The simulated device
sends an acknowledgement back to your hub after it executes the direct method.

- In a terminal window, navigate to the root folder of the sample Java project. 

- navigate to the **iot-hub\Quickstarts\simulated-device-2** folder.

- Open **src/main/java/com/microsoft/docs/iothub/samples/SimulatedDevice.java**
  file in a text editor of your choice.

- Replace the value of the `connString` variable with the device connection
  string you made a note of previously. Then save your changes to
  **SimulatedDevice.java** file.

- In the terminal window, run Maven to build the simulated device application:

    ```cmd/sh
    mvn clean package
    ```

 - In the terminal window, run the following commands to run the simulated
   device application:

    ```cmd/sh
    java -jar target/simulated-device-2-1.0.0-with-deps.jar
    ```
You should see messages in the console window running the simulated device

- Open the Iot Hub you just created if it is not alread open in the portal.
  Scoll down and Review the **Device twin operations** and the **Device to cloud
  messages** graphs.  You should see data rollng into both graphs.  

Next, we will be using the [Akka Stream library for Azure IoT
Hub](https://github.com/Azure/toketi-iothubreact) to interact with our simulated
device and the Azure IoT Hub.
