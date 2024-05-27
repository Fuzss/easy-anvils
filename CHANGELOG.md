# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v20.4.6-1.20.4] - 2024-05-27
### Fixed
- Fix dupe glitch where multiple players could take the anvil result at the same time

## [v20.4.5-1.20.4] - 2024-05-26
### Added
- Add a few new config options:
  - Durability bonus for combining two items of the same kind, 12% by default (e.g. combining two pickaxes with varying durability)
  - Durability bonus for adding a repair material, 25% by default (e.g. adding a diamond to a damaged diamond pickaxe)
  - No prior work penalty will be added when combining enchanted books, enabled by default
- Renamed mobs can now drop a name tag on death (disabled by default)
- Optional crafting recipe for name tags (disabled by default)
### Changed
- The name tag tooltip hint can now be hidden via a config option
- Change prior work penalty calculations, the previous fixed increase approach is now combined with the vanilla behavior where the penalty doubles after every operation
- This means the penalty will double until a maximum increase is reached (4 by default), after that it will increase by that constant value:

```
Operation:              1,  2,  3,  4,  5,  6,  7, ...
Vanilla:                0,  1,  3,  7, 15, 31, <too expensive past 39>
Easy Anvils (v20.4.4):  0,  4,  8, 12, 16, 20, 24, ...
Easy Anvils (v20.4.5):  0,  1,  3,  7, 11, 15, 19, ...
```

## [v20.4.4-1.20.4] - 2024-05-25
### Fixed
- Renaming an item without using formatting codes will no longer remove italics / light blue formatting for renamed / enchanted items

## [v20.4.3-1.20.4] - 2024-03-04
### Fixed
- Fix switching rename and repair cost config option to `LIMITED` always setting the cost to 39 when the too expensive limit is left at -1

## [v20.4.2-1.20.4] - 2024-01-28
### Changed
- Apply anvil menu changes in a less invasive way, also send less update packets to clients
### Fix
- Fix unable to change item name on anvil

## [v20.4.1-1.20.4] - 2024-01-27
### Fix
- Exclude Better Nether & Better End anvils by default to avoid a crash trying to use them

## [v20.4.0-1.20.4] - 2024-01-24
- Ported to Minecraft 1.20.4
- Ported to NeoForge
### Added
- Add a formatting codes guide to anvil & name tag screen
- Add experimental support for double click dragging to select multiple words to anvil & name tag text boxes
### Changed
- Overhaul large portions of the mod, anvil changes are no longer applied via Mixin, instead anvil blocks are replaced internally
- Should not result in any noticeable gameplay differences
- This also means modded anvils are now fully supported, although any custom properties like custom repairing behavior will be overwritten
- Modded anvils can opt out from being replaced by Easy Anvils by being added to the `easyanvils:unaltered_anvils` block tag
