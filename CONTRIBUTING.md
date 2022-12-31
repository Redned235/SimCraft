# Contributing

Contributions to this project are fully welcomed! However, getting started with contributing may be a tad tricky without a bit of background information. Here are a few tips to help you get started:

## SimCity4 Data Storage

### File Types
SimCity 4 stores most all of its data in files known as [DBPF](https://wiki.sc4devotion.com/index.php?title=DBPF)s, or Database Packed Files. They are essentially a compressed file that contains many other "subfiles", or directories. It may also contain other miscellaneous files, such as images.

Each "section" in the file is referenced by a [PersistentResourceKey](https://wiki.sc4devotion.com/index.php?title=Type_Group_Instance) (also known as a TypeGroupInstance). This is a unique identifier made up of 3 integers that identifies a specific position in the file. This can be used to identify where buildings are, props are, etc.

### [Savegame (.sc4) Files](https://wiki.sc4devotion.com/index.php?title=Savegame)
This is where the bulk of information regarding a city is stored. It contains the locations for all buildings, props, flora, etc., along with the size of the city, its location on the map, the mayor, population, or really anything that is specific to that city.

### [Exemplar Files](https://wiki.sc4devotion.com/index.php?title=Exemplar)
Often stored as a `.dat`, Exemplar files contain the actual properties and information for various resources. For example, Savegame files will contain all buildings placed in a city, but the only information stored on disk there is the PersistentResourceKey. The Exemplar file on the other hand, contains all the properties for that building with that resource key. Here is an example of how that looks: [SimReader Example](https://github.com/Redned235/SimReader/blob/master/README.md#usage).

Each Exemplar Subfile is marked with a unique PersistentResourceKey. Exemplar Subfiles can contain various [properties](https://wiki.sc4devotion.com/index.php?title=Exemplar_properties) that are relevant to the data being stored. 

## Builds & Props
Unfortunately, there is not an easy way to quickly view what every building and prop looks like in a fancy way. Much of the work will need to be manually done, however there are a few things this program can do to assist with that.

- When starting the program, enable debug mode by using the `-debug` flag. This will place a sign at every build location with various metadata about it
- Set the flag `-print-missing` upon startup to see a list of missing props and buildings. The names of these builds contain various metadata that is important to assisting with their builds:

### Adding Missing Builds
Using the `print-missing` flag mentioned above, you can see a list of missing buildings. Most of these build's Exemplar names contain their dimensions (i.e. IR**15x7**_1Farm4_0850). The first coordinate is on the X plane, and the second is Z. Sometimes a third Y coordinate is included, but this is not as important as the other two as no prop will exist on top of another. Although these are the sizes SimCity 4 uses, they almost identically correlate to Minecraft coordinates too.

Again, seeing what these buildings look can be very tricky, unless you have Minecraft and SimCity 4 open side-by-side. However, you may have some luck finding the corresponding building on the [SC4D Wiki](https://wiki.sc4devotion.com) as some buildings are documented and have their exemplar name listed too.

Builds used in this program are stored in the `src/main/resources/schematics` location. When testing builds, you can also provide your own schematics by creating a `schematics`
 directory in the same directory the SimCraft jar is located in.

**A few tips to keep in mind for builds:**
- Try to stay within the same X & Z bounds. Otherwise, this can cause buildings or props to overlap and look very ugly.
- For buildings, add a few blocks underneath the structure. This is for cases where the building is slightly above the terrain it is being placed on (i.e. a hilly area). It will prevent issues such as [this](https://bugs.mojang.com/secure/attachment/321934/plains%20village%20floating%20house%20bare%20dirt.png)
  - You may need to adjust the Y offset slightly in cases for this. You can do this by using an [NBTEditor](https://github.com/Howaner/NBTEditor) (or if you have IntelliJ, download the Minecraft Plugin for IntelliJ) and setting a negative value for `SCOffsetY` to how far you built down (i.e. -2 for if you went down 2 blocks from the schematic's surface level).

**Saving builds**
Once you have your build made, using WorldEdit, create a schematic of this build, and give it the same name as the Exemplar. Place it in the schematics file and rereun the program.