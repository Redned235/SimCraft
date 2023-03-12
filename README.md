# SimCraft

A program to convert SimCity 4 (Deluxe) cities into Minecraft worlds.

**This is still very much a heavy work in progress and cities are unlikely to be translated perfectly.** 

![image](https://user-images.githubusercontent.com/29153871/210161518-753d89e8-3cef-415b-a840-0b00771f4d16.png)

## What Currently Works
- Terrain conversion
- Agriculture plots
- Buildings & prop placement (most schematics have not been made for these however)
- Roads & streets

## What Needs Implementing
- Avenues & highways
- Underground tiles (subways, pipes, etc.)
- Many, many building & prop builds. Placement works, but most builds have not been created yet.
- Bridges & Trains

## Running the Program
SimCity 4 must be installed for this to work, as SimCraft needs to access various game files.

1. Download the SimCraft jar from the [latest release](https://github.com/Redned235/SimCraft/releases).
2. Once downloaded, double-click the jar to open the UI.
3. Select the SimCity region directory, Minecraft world export directory and (if applicable) the SimCity install directory.
4. Click "Export to Minecraft" and all cities in the specifeid region directory will be converted to a Minecraft world!

<details>
<summary>CLI Instructions</summary>
<br>
The command line interface allows for additional debugging capabilities that are useful for development.
<br>
1. Download the jar from the [latest release](https://github.com/Redned235/SimCraft/releases).
2. Create a new directory and place the jar in it.
3. Open up your command line and insert the following code:
    ```shell
    java -jar SimCraft-cli.jar -c <city directory> -g <game directory> -o <output>
    ```
   **Note**: SimCraft will attempt to find the game directory automatically on Windows, so the `-g` option can be omitted.
4. Once everything has completed, place the output directory inside your Minecraft `saves` file, and teleport to the coordinates the city was pasted at (it will print this in the console).

</details>

## Contributing
See the [Contributing](https://github.com/Redned235/SimCraft/blob/master/CONTRIBUTING.md) file for more information.

## Libraries Used
- [SimReader](https://github.com/Redned235/SimReader)
- [LevelParser](https://github.com/Redned235/LevelParser)
- [Cloudburst NBT](https://github.com/CloudburstMC/NBT)
- [Cloudburst Math](https://github.com/CloudburstMC/math)
- [jopt-simple](https://github.com/jopt-simple/jopt-simple)

## Credits
Much of this project would not be possible without the following resources & repositories:
- [SC4Devotion Wiki](https://wiki.sc4devotion.com/)
- [SC4Mapper](https://github.com/wouanagaine/SC4Mapper-2013/)
- [SC4Parser](https://github.com/Killeroo/SC4Parser)

Also big thanks to [mine-city-2000](https://github.com/jgosar/mine-city-2000/) for the original inspiration for this project!
