/**
 * Copyright © 2016-2019 Lightbend, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * NO COMMERCIAL SUPPORT OR ANY OTHER FORM OF SUPPORT IS OFFERED ON
 * THIS SOFTWARE BY LIGHTBEND, Inc.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eroled;
import com.pi4j.io.gpio.*;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;

import java.io.IOException;

// ER-OLEDM032-1 is a 256x64 each pixel is a 4-bit gray-scale value.
public class BasicOLED {
    protected SpiDevice spi = null;
    protected GpioPinDigitalOutput dc = null;
    protected GpioPinDigitalOutput reset = null;

    public BasicOLED() throws IOException, InterruptedException {

        GpioFactory.setDefaultProvider(new RaspiGpioProvider(RaspiPinNumberingScheme.BROADCOM_PIN_NUMBERING));
        final GpioController gpio = GpioFactory.getInstance();
        reset = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, PinState.HIGH);
        reset.high();

        dc = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24);
        spi = SpiFactory.getInstance(SpiChannel.CS0,
                5000000, // default spi speed 1 MHz
                SpiMode.MODE_3);

        resetOLED();
        writeInstruction(0xFD);
        writeInstruction(0xFD); //SET COMMAND LOCK
        writeData(0x12); // UNLOCK
        writeInstruction(0xAE);// DISPLAY OFF
        writeInstruction(0xB3); // DISPLAYDIVIDE CLOCKRADIO/OSCILLATAR FREQUANCY
        writeData(0x91);
        writeInstruction(0xCA);// multiplex ratio
        writeData(0x3F);// duty = 1/64
        writeInstruction(0xA2);// set offset
        writeData(0x00);
        writeInstruction(0xA1);// # start line
        writeData(0x00);
        writeInstruction(0xA0); //set remap
        writeData(0x14);
        writeData(0x11);

        writeInstruction(0xAB); // funtion selection
        writeData(0x01); // selection external vdd
        writeInstruction(0xB4);
        writeData(0xA0);
        writeData(0xfd);
        writeInstruction(0xC1); // set contrast current
        writeData(0x80);
        writeInstruction(0xC7); // master contrast current control
        writeData(0x0f);

        writeInstruction(0xB1); // SET PHASE LENGTH
        writeData(0xE2);
        writeInstruction(0xD1);
        writeData(0x82);
        writeData(0x20);
        writeInstruction(0xBB); // SET PRE-CHANGE VOLTAGE
        writeData(0x1F);
        writeInstruction(0xB6); // SET SECOND PRE-CHARGE PERIOD
        writeData(0x08);
        writeInstruction(0xBE); // SET VCOMH
        writeData(0x07);
        writeInstruction(0xA6); // normal display
        clearRam();
        writeInstruction(0xAF); // display ON

    }

    public void resetOLED() throws InterruptedException {
        reset.low();
        Thread.sleep(500);
        reset.high();
        Thread.sleep(1000);
    }


    public void writeInstruction(int dataBytes) throws IOException {
        dc.low();
        spi.write((byte) dataBytes);
    }


    public void writeData(int dataBytes) throws IOException {
        dc.high();
        spi.write((byte) dataBytes);
    }

    public void writeDataBytes(byte[] dataBytes) throws IOException {
        if (dataBytes == null) return;
        dc.high();
        spi.write(dataBytes);
    }


    public void clearRam() throws IOException {
        writeInstruction(0x15);
        writeData(0x00);
        writeData(0x77);
        writeInstruction(0x75);
        writeData(0x00);
        writeData(0x7f);
        writeInstruction(0x5C);
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 120; x++) {
                writeData(0x00);
            }
        }
    }

    public void setDataWindow(int x, int y, int width, int height) throws IOException {
        x = x / 4; // Column address is 4-pixel-groups (2 bytes)
        // Pass in x=0,4,8, etc

        width = width / 4;// # With limited to 4-pixel increments
        // # Pass in width=4,8, etc

        writeInstruction(0x75);
        writeData(y);
        writeData(y + height - 1);

        writeInstruction(0x15);
        writeData(0x1C + x);
        writeData(0x1C + x + width - 1);
    }

    public void setRowAddress(int add) throws IOException {
        writeInstruction(0x75);
        add = 0x3f & add;
        writeData(add);
        writeData(0x3f);
    }

    public void setColumnAddress(int add) throws IOException {
        add = 0x3f & add;
        writeInstruction(0x15);
        writeData(0x1c + add);
        writeData(0x5b);
    }
}
