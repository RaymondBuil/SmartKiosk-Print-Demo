## Kiosk Print demo
This sample app demonstrates how to configure and print labels on a Link-OS BlueTooth printer using the PrintConnect app.
When starting the app for the first time, config.txt, label1.zpl, label2.zip and template.zpl will be created into the /sdcard folder.
To make this sample app to work, the PrintConnect app needs to be installed on your Zebra Device.
PrintConnect app can be found on Google Play Store at: https://play.google.com/store/apps/details?id=com.zebra.printconnect&hl=nl&gl=US

To create a SmartKiosk Solution, the Zebra SmartKiosk Printer Mounting Accessory can be used to mount the CC6000 and supported LinkOS printer.
By TapAndPair or by searching the network, the Link OS printer can be connected to the CC6000.
This Kiosk Print Demo app makes use of PrintConnect to:
1. Configure the printer, by sending the config.txt file
2. Printing label1, by sending the label1.zpl file
3. Printing label2, by sending the label2.zpl file
4. Printing the template when scanning a barcode. The Barcode Type and its content is getting printed on the label.

All mentioned files are created at the first start of the application and stored into the /sdcard folder.
All files can be modified, containing your own configuration, label1, label2 and template definition.

## Contributing
This repo welcomes contributions - please refer to our contributon guide [here](CONTRIBUTING.MD)

## Code of Conduct
Your safety is important to us, thus all contributions to and interactions with ZebraDevs projects have to adhere to our Code of Conduct.
You can find the Code of Conduct [here](Code_of_Conduct.md).

## Licence
[MIT](LICENSE.txt)
