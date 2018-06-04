# LED Status strip header assembly PCB prototype

In this document, I show the building of a PCB prototype using the following components:

- 20-pin female header with 90 degrees bent header pins. Note that the header I used 
- 2 male & 2 female connectors
- a 1N4001 diode
- an 8 RGB-LED strip
- a PCB board (single copper islands with a 1/10 inch pitch) used for experiments cut to the right width. The board is not cut to the correct length.

These following pictures shows the different components.

![components](images/PCB-prototype/IMG_1057.JPG)

PCB board solder side:

![components](images/PCB-prototype/IMG_1061.JPG)

PCB board component side:

![components](images/PCB-prototype/IMG_1060.JPG)

The assist the soldering of the male connectors to the LED strip, we use a custom "_caliber_ PCB" as shown here

![components](images/PCB-prototype/IMG_1059.JPG)

We start by putting the male connectors in the female connectors in the _caliber_

![components](images/PCB-prototype/IMG_1062.JPG)

Next, we clamp the LED strip to the contacts on the male connectors using alligator clamps and solder the outer contact of each connector to the LED strip:

![components](images/PCB-prototype/IMG_1063.JPG)

Now, we solder the two remaining outer contacts to the LED strip:

![components](images/PCB-prototype/IMG_1064.JPG)

The LED strip with connectors can now be removed gently using a flat screwdriver: 

![components](images/PCB-prototype/IMG_1065.JPG)

We now finish this part by soldering the remaining two contacts on each connector to the LED strip (viewed from 3 different angles):

![components](images/PCB-prototype/IMG_1066.JPG)

![components](images/PCB-prototype/IMG_1067.JPG)

![components](images/PCB-prototype/IMG_1068.JPG)

We're now going to solder the female header to the PCB board. As the 3 contacts we're using on the GPIO connector are all on the outer edge of the connector, we can trim the "long" pins leaving only a single row of pins (that are bent 90 degrees on this connector):

![components](images/PCB-prototype/IMG_1069.JPG)

![components](images/PCB-prototype/IMG_1070.JPG)

Now solder the connector on the PCB board and solder the 1N4001 diode on the same side of the PCB board:

![components](images/PCB-prototype/IMG_1071.JPG)

![components](images/PCB-prototype/IMG_1072.JPG)

Next, we plug the male connectors on the LED strip into two female connectors. Note the orientation of the pins on the female connector. We clamp the assembly to the PCB board and solder two contacts to the PCB board:

![components](images/PCB-prototype/IMG_1074.JPG)

We remove the clamps and solder two outer contacts on the other side:

![components](images/PCB-prototype/IMG_1075.JPG)

Again, we take the LED strip from the PCB board using a flat-blade screw driver and solder the remaining contact to the board:

![components](images/PCB-prototype/IMG_1076.JPG)

We can now finish the assembly by wiring things up. Of course, with a custom PCB this step would not be needed and we could just proceed with testing the PCB.

Here we connect anode of the 1N4001 diode to the first pin on the header, the cathode to the 3rd pin of the female connector and the data signal pin (6th pin counting from the pin closest to the female connector) to the 2nd pin of the female connector.

![components](images/PCB-prototype/IMG_1078.JPG)

We now complete the connections by connecting the ground (GND) pin:

![components](images/PCB-prototype/IMG_1079.JPG)

When we plug the LED strip back in, we can plug the complete assembly on the Pi's GPIO connector (3 views shown):

![components](images/PCB-prototype/IMG_1084.JPG)

![components](images/PCB-prototype/IMG_1085.JPG)

![components](images/PCB-prototype/IMG_1086.JPG)

And test it:

![components](images/PCB-prototype/IMG_1087.JPG)

__Success !!!__

This assembly

- is "low" enough to be plugged into a stacked Raspberry Pi.
- Stands relatively far away from the GPIO connector. Still, it stays within the outer boundary of the Pi stack plates. Potentially, this distance may be reduced by using another connector type... if one can find one...


### Component part-numbers

- Male connectors
    - Manufacturer: _Preci-Dip_
    - Part-number: _800-80-004-20-001101_
- Female connectors
    - Manufacturer: _Preci-Dip_
    - Part-number: _801-87-004-20-002101_
- LED Strip:
    - _8-channel-WS2812-5050-RGB-LED_
    - [Ali-express parts link](https://nl.aliexpress.com/item/8-channel-WS2812-5050-RGB-LED-lights-development-board-for-Arduino/32769045926.html?spm=a2g0s.9042311.0.0.Mwyjuo)
- 20-pin Female header:
    - [Ali-express parts link](https://nl.aliexpress.com/item/50pcs-2x10P-20-poles-2-54mm-Female-PCB-Pin-Header-Right-Angle-Single-row-Through-Hole/32734093247.html?spm=a2g0s.9042311.0.0.5de84c4dhC6yLS)

As reported by @raboof, there's this [6-pin, single row, female header](https://www.pololu.com/product/2706) that fits the bill perfectly for this assembly.