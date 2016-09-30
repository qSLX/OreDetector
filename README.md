# OreDetector
Plugin for [Minecraft Sponge server](https://www.spongepowered.org/).

## Descripiton ##
This plugin will notify player by repeated ping sound when he is at close range to any ore. You can set minimum range below which detector does not work and also maximum range, separately horizontaly and verticaly. It can hurt game experience if range is set too far, so I choosed default values quite low. The sound comes from direction where ore has been detected and is louder if nearer.

## Commands ##
```
oredetector [on|off] - It simple switches detecting on or off
```

## Config ##
Config is created after stopping server. Default values are these currently assigned below.
```ini
# Minimum and maximum horizontal and vertical range of detector. Possible values are from 1 to 10.
MinHorizontalRange=2
MaxHorizontalRange=3
MinVerticalRange=2
MaxVerticalRange=2

# Comma separated names of blocks which should be detected. Available names are:
# coal_ore, diamond_ore, emerald_ore, gold_ore, iron_ore, lapis_ore, lit_redstone_ore, quartz_ore, redstone_ore
DetectedBlocks="diamond_ore,emerald_ore,gold_ore,redstone_ore,lit_redstone_ore"
```

## TODO ##
Few things:
- Better search pattern, current is far from ideal
- Permissions to use
- Detector as equippable item with limited usage
- Different config per world
- More things to detect
