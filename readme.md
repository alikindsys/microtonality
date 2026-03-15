An port of [Microtonal Note Blocks](https://modrinth.com/mod/microtonal-note-blocks) to the latest minecraft version with some quality of life changes.

## Changes from the original mod
1. Noteblocks work as intended out of the box, that is the tuning is set to 12-TET (Default western tuning) and they are at the lowest octave.
2. Octaves change based on the active tuning so if you set the tuning to 4-TET, the first octave will be reached at `note=3`, requiring only 8 clicks to go back to `note=0, octave=false` (default state). As far as we could test, this didn't work previously, it's just more intuitive then clicking 12 times and hearing the highest note 8 more times until the octave changed. 

## Plans for the future
- *(maybe)* We have the idea to add a modded noteblock that allows for arbitrary tunings (like 31-TET for example). The mod is currently limited to 24-TET since it uses the vanilla `NOTE` block property and you can't really dynamically modify block properties.

- Change the tuning fork to have a gui where you can set a specific tuning by right clicking air/shift-clicking a noteblock and then pasting it across a bunch of noteblocks. Sorta like the AE2 configuration wrench / Mekanism configuration card.

- *(maybe)* Datafixers for the original mod so worlds that used that mod can be seamlessly upgraded forward. **for the time being treat it as if incompatible**.
