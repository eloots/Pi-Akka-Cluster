# Configuring TP-Link Travel Router for class room environment

## TP-Link 300Mbps Wireless N Nano Router

This is the "travel router" we will use: its the TP-Link model `TL-WR802N`

### Reset the router & connect to router via Wifi

- Power the router via USB
- Press the reset button on the side of the router for at least 5 seconds and release
- Connect to the router via Wifi
    - The SSID of the router is noted on the back of the router
    - You may need to enter a password - it also can be found in the same place as the SSID

### Set the IP network of the LAN port on the router

- Surf to [http://tplinkwifi.net](http://tplinkwifi.net)
- If asked, Log-in using admin/admin credentials
- Click on `Network` in the menu on the left side
- More options become available. Click on `LAN`
- Enter the desired IP address & netmask of the LAN port
    - For example: 192.168.34.1, 255.255.255.0
- Push the `Save` button. The router will now reboot

- Re-connect via Wifi to the route. Your IP address will be the IP address entered above...
- Surf to [http://tplinkwifi.net](http://tplinkwifi.net)
- If asked, Log-in using admin/admin credentials
- Select `Quick Setup` in the menu on the left side
    - Click `Next`
    - Select `Hotspot Router` and click `Next`
    - Select `Dynamic IP` and click `Next`
    - The router perform a scan for Wifi networks. Click on `Connect` for the network you want to connect to.
        - Enter the password and click `Next`
    - Click `Finish`
    - The router will reboot


- Disable the Wifi network on your laptop
- Connect your laptop to the router via a UTP cable
- Surf to [http://tplinkwifi.net](http://tplinkwifi.net)
- Log-in if prompted
- Click on `DHCP` in the menu on the left side
    - Change the `Start IP Address`, for example, to 192.168.34.5
    - Change the ` End IP Address`, for example, to 192.168.34.99
    - Click `Save`
- Renew the DHCP license on your laptop. You should be all set...