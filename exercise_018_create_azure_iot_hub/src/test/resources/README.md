# Exercise 18

## IoT Hub Setup
These steps show how to set up the Azure cloud resources in the Azure portal for
provisioning your devices. This includes creating an Azure account, creating a
resource group, and  creating a new IoT Hub.

We have codes for free Azure accounts/credits if you don't have an Azure
subscription - please ask an instructor.

## Steps

### Create an Azure Account

Set up an Azure account using the free trial code provided (ask an instructor
for the code)

### Create an IoT hub

Click the **Create a resource** button found on the upper left-hand corner of
the Azure portal. Select **Internet of Things**, select **IoT Hub**, and click
the **Create** button. 

- Create a new **resource group** and name it scaladays2018*yournameornickname*.
  Note the name as you will need it for future provisioning.  For more
  information, see [Use resource groups to manage your Azure
  resources](https://github.com/MicrosoftDocs/azure-docs/blob/master/articles/azure-resource-manager/resource-group-portal.md)


- Select **East US 2** as the **region** and name it
  scaladays2018*yournameornickname*.

- **Name** your IoT hub scaladays2018*yournameornickname*. If the name you enter
  is available, a green check mark appears.

Click the **Review and Create** button. 

Your new IoT Hub will  take a few minutes to provision.  Once the IoT hub is
successfully deployed, the hub summary blade automatically opens.

**NOTE:** This exercise covers basic steps for the IoT Hub that we need for this
tutorial.  For reference, have a look at the [full steps and
options](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-create-through-portal#endpoints)
