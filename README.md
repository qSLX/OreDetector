# OreDetector
=============

## Descripiton ##
This is a plugin for Minecraft Sponge server. It will notify player by ping sound when he is at close range to any ore. You can set minimum range below which detector does not work and also maximum separately horizontaly and verticaly. It can hurt game experience if range is set too far, so I choosed default values quite low.

## Commands ##
```
oredetector [on|off] - It simple switches detecting on or off
```

## Config ##
Default values are these currently assigned below. Values in squares are allowed minimum and maximum.

Minimum and maximum horizontal and vertical range of detector
```
MinHorizontalRange=2 [1:10]
MaxHorizontalRange=3 [1:10]
MinVerticalRange=2 [1:10]
MaxVerticalRange=2 [1:10]
```

Comma separated names of blocks which should be detected. Available names: coal_ore, diamond_ore, emerald_ore, gold_ore, iron_ore, lapis_ore, lit_redstone_ore, quartz_ore, redstone_ore

```
'DetectedBlocks="diamond_ore,emerald_ore,gold_ore,redstone_ore,lit_redstone_ore"'
```
