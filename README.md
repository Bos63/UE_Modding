# UE4/5 Modding Guides 
A collection of UE4 (and 5) Modding Guides.</br>
The perfect place for anyone new, to learn UE modding and start creating mods <u><strong>today</strong></u>.</br>

## Before we begin...
These two tools every modder needs to have:<br>
- [Unreal Engine](https://www.unrealengine.com/en-US/)
- [FModel](https://fmodel.app/)

## Where to start?
If you're completely new, I would suggest picking on a simple mod idea/goal that will teach you the basics.<br>
For example: learn how to extract game files, modify a texture/model and repackage it into a mod (UE4/5). <br>
Or modify default values of a blueprint, like the max ammo of a weapon (UE4).

Just so you get the idea and the principles of UE modding and how it works.

<i>(If you're new and want to jump head-first into custom maps... then don't, that doesn't work like that).</i>

## The Basics
We will start with how we can browse and export game files.

- [Finding AES key (.pak encryption)](./TheBasics/AesKey.md)
- [Extracting Cooked Assets - repak](./TheBasics/ExtractingCooked.md) (`.pak` only)
- [Extracting IoStore Cooked Assets - retoc/ZenTools](./TheBasics/ExtractingIoStore.md) (`.pak`/`.utoc`/`.ucas`)
- [Extracting mappings - UE4SS](./TheBasics/Extractingusmap.md)
- [Exporting - FModel](./TheBasics/ExportingFModel.md)
- [Exporting - UModel](./TheBasics/ExportingUModel.md)
- [Previewing Animations - UModel](./TheBasics/UModelAnimations.md)
- [Browsing UAssets - FModel](./TheBasics/UsingFModel.md)

## Basic Modding
Let's start changing values!</br>
This is essential to <b>ANY</b> value changing.</br>
- [Editing UAsset values - UAssetGUI](./BasicModding/UAssetGUI.md)
- [Editing UAsset values - Hex](./BasicModding/HexEditing.md) *(Manual hex, in case UAssetGUI fails)*
- [Editing UMaps - stove](./BasicModding/EditingUmaps.md) (UE4, `.pak` only)
- [Disabling/Removing textures of objects](./BasicModding/DisablingObjects.md)
- [Creating Pak Files](./BasicModding/UnrealPak.md) (`.pak` only)
- [Creating IoStore Pak Files](./BasicModding/IoStorePacking.md) (`.pak`/`.utoc`/`.ucas`)
- [Mod example - modifying Blueprint default values](./BasicModding/example1.md) (UE4)
- [Editing IoStore Assets - retoc](./BasicModding/EditingIoStoreAssets.md) (`.pak`/`.utoc`/`.ucas`)

## Intermediate Modding
Dummying/replacing Assets like textures, materials, static meshes, and SkeletalMeshes (such as characters). </br>

- [Creating a UE4/5 project](./IntermediateModding/CreatingProject.md)
- [Cooking/compiling in UE4 and UE5 with chunks](./IntermediateModding/CookingContent.md)
- [Replacing textures](./IntermediateModding/ChangingTextures.md)
- [Changing StaticMesh](./IntermediateModding/ChangingSM.md)
- [Changing SkeletalMesh](./IntermediateModding/ChangingSK.md)
- [Merging SkeletalMesh](./IntermediateModding/MergingSK.md)
- [Replacing Fonts](./IntermediateModding/ReplacingFonts.md)
- [Translation Mods](./IntermediateModding/TranslationMod.md)

## Advanced Modding
- [Dummying a Material for MaterialInstances](./AdvancedModding/ReplicatingMI.md)
- [Introduction to Blueprint/Logic Mods](./AdvancedModding/BpModsIntro.md)
- [Introduction to Blueprint Dummying/Replication](./AdvancedModding/BpReplication.md)

### Blueprint Modding
I won't be going into detail as that is where Unreal Engine experience comes in and this will cover the bare basics.</br>
<b>Note:</b> If you're new to UE - just tinker with it, everything is on YouTube.

- [Working with a mod loader - UML/UE4SS/DML/NML](./BPModding/WorkingWithML.md)
- [ModActor structure and lifecycle](./BPModding/ModActorLifeCycle.md)
- [Creating a Widget](./BPModding/CreateWidget.md)
- [Hotkeys for BP mods](./BPModding/Hotkeys.md) 
- [Config Variables - mod configurations](./BPModding/ConfigVariables.md)
- [Custom Mod GameSaves](./BPModding/GameSaves.md)
- [Mod Example - Custom logger (UserWidget)](./BPModding/CustomLogger.md) 
- [Modding Archetypes (Class Default Objects, Component Templates & Default Subobjects)](./BPModding/DefaultObjects.md) ![new](https://img.shields.io/badge/←-NEW-brightgreen)   

## Expert
At this stage, you already know how to swap/modify any UAsset and do blueprints as your second language, YET looking for more advanced stuff to try.
- ["Injecting" custom Widgets into game menus using Blueprints](./ExpertModding/GameMenus.md)
- [Dumping C++ headers (UHT) using UE4SS](./ExpertModding/GeneratingUHT.md)
- [Dummying/replicating C++ headers using UHT](./ExpertModding/UEClasses.md)
- [Mod Example - Headers in practice, (game: Sprawl)]() (Coming Soon)

## Masterclass
I won't be covering any more complex witch-craft so if you've learned all of the above, then you can continue learning and evolving on your own.<br>

## Game Memory
Useful for creating speedrunning livesplit, custom randomizers and even trainers.
- [Finding CE pointers](./GameMemory/findingPointers.md)
- [Finding offsets using UE4SS](./GameMemory/findingPointers2.md)


##  Blender
- [Importing models (.psk and .glTF2 files)](./Misc/BlenderImportModels.md)
- [Importing animations (.psa files)](./Misc/BlenderImportAnimations.md)

## Textures
- [Importing packed textures to Blender](./Misc/BlenderImportTextures.md)
- [Importing packed textures to Substance Painter](./Misc/SubstanceImportTextures.md) 
- [Exporting from Substance Painter to UnrealEngine](./Misc/SubstanceExport.md) 

<hr>

# Useful Links/Tools

### Browse and Edit UAssets
- [FModel](https://fmodel.app/)
- [UModel](https://www.gildor.org/en/projects/umodel)
- [UAssetGUI](https://github.com/atenfyr/UAssetGUI)
- [stove](https://github.com/bananaturtlesandwich/stove)

### UE
- [Epic Launcher](https://www.epicgames.com/store/en-US/)
- [Universal Unreal Engine 4 Unlocker (UUU)](https://framedsc.github.io/GeneralGuides/universal_ue4_consoleunlocker.htm)
- [UnrealPak](https://github.com/Dmgvol/UE_Modding/raw/main/Tools/UnrealPak.zip) - written by [FluffyQuack](https://www.fluffyquack.com/)
- [UE4SS - UE4/5 Scripting System](https://github.com/UE4SS-RE/RE-UE4SS)

### 3D Modeling
- [Blender](https://www.blender.org/) or [Steam's version](https://store.steampowered.com/app/365670/Blender/)
- [Blender 4 Psk plugin](https://github.com/DarklightGames/io_scene_psk_psa) - written by **DarklightGames**
- [Blender 3 Psk plugin](https://github.com/Befzz/blender3d_import_psk_psa) - written by **Befzz**

### Reverse Engineering
- [Cheat Manager](https://www.cheatengine.org/) - Written by **Dark Byte**
- [x64dbg](https://x64dbg.com/) - Written by **mrexodia**
- [BinaryNinja](https://binary.ninja/)
- [Hex Editor Neo](https://freehexeditorneo.com/)

### The rest
- [All available/known UE modding tools](https://github.com/Buckminsterfullerene02/UE-Modding-Tools) written by **Buckminsterfullerene**.

### Discords
[UE Modding](https://discord.gg/unreal-engine-modding-876613187204685934)

---

# Credits
- [FatihG_](https://www.youtube.com/c/fatihG/) for teaching me how to mod.
- [atenfyr](https://github.com/atenfyr/)(adolescent in Discord) for [UAssetGUI](https://github.com/atenfyr/UAssetGUI) and [UAssetAPI](https://github.com/atenfyr/UAssetAPI).
- [RussellJerome](https://github.com/RussellJerome) for creating the [ModLoader](https://github.com/RussellJerome/UnrealModLoader).


### Special Thanks:
- LongerWarrior, JanisSG, Jan and Animayyo for their amazing support throughout the whole journey of GR modding.
- Mythical
- Narknon
- Cranch
- Buckminsterfullerene02
- Atenfyer/Adolescent
- Spuds
- Truman 
- Lisht/Kein
- KunoDemetries
- HyperModule

---

