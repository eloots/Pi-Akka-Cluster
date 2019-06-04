## Introduction

This folder contains a slightly modified version of the [rpi_ws281x](https://github.com/jgarff/rpi_ws281x) project.

## Modifications

The following modifications were made:

- main.c 
  - Changed # LEDs to 10
  - Will display a running red LED on startup
  - Sending a SIGTERM will terminate the program and will turn all LEDs to green
- Removal of python and Golang samples (unneeded for this project)

## Build instructions

- Move this repo to a Raspberry Pi (eg. on that is running Hypriot)
- cd into the copied repo
- Install build tools:

```
sudo apt install build-essential
sudo apt-get install swig
sudo apt-get install scons
```

- Build the application by running `scons`: this will create an executable named `test`
- Encode the `test` file in base 64 format:

```
base64 test > test_encoded
```

- If your file needs to be integrate in a SD image, transfer the file `test_encoded` to the laptop and add it to the `cloud-init` yaml file like here:

```
  - encoding: b64
  - content: |
      f0VMRgEBAQAAAAAAAAAAAAIAKAABAAAAOA4BADQAAABUFQEAAAQABTQAIAAJACgAJwAmAAEAAHCc
      PAAAnDwBAJw8AQAIAAAACAAAAAQAAAAEAAAABgAAADQAAAA0AAEANAABACABAAAgAQAABQAAAAQA
      AAADAAAAVAEAAFQBAQBUAQEAGQAAABkAAAAEAAAAAQAAAAEAAAAAAAAAAAABAAAAAQCoPAAAqDwA
      <elided>
    owner: root:staff
    permissions: '0755'
    path: /usr/local/bin/progresstracker
```
