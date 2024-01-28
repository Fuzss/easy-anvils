# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
