![Logo](https://bundabrg.github.io/PortalNetwork/img/title.png)

[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](https://lbesson.mit-license.org/)
[![GitHub release](https://img.shields.io/github/release/Bundabrg/PortalNetwork)](https://GitHub.com/Bundabrg/PortalNetwork/releases/)
[![GitHub commits](https://img.shields.io/github/commits-since/Bundabrg/PortalNetwork/v1.2.0.svg)](https://GitHub.com/Bundabrg/PortalNetwork/commit/)
[![Github all releases](https://img.shields.io/github/downloads/Bundabrg/PortalNetwork/total.svg)](https://GitHub.com/Bundabrg/PortalNetwork/releases/)
![HitCount](http://hits.dwyl.com/bundabrg/portalnetwork.svg)

![Workflow](https://github.com/bundabrg/PortalNetwork/workflows/build/badge.svg)
![Workflow](https://github.com/bundabrg/PortalNetwork/workflows/docs/badge.svg)
[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://GitHub.com/Bundabrg/PortalNetwork/graphs/commit-activity)
[![GitHub contributors](https://img.shields.io/github/contributors/Bundabrg/PortalNetwork)](https://GitHub.com/Bundabrg/PortalNetwork/graphs/contributors/)
[![GitHub issues](https://img.shields.io/github/issues/Bundabrg/PortalNetwork)](https://GitHub.com/Bundabrg/PortalNetwork/issues/)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/Bundabrg/PortalNetwork.svg)](http://isitmaintained.com/project/Bundabrg/PortalNetwork "Average time to resolve an issue")
[![GitHub pull-requests](https://img.shields.io/github/issues-pr/Bundabrg/PortalNetwork)](https://GitHub.com/Bundabrg/PortalNetwork/pull/)
 

---

[**Documentation**](https://bundabrg.github.io/PortalNetwork/)

[**Source Code**](https://github.com/bundabrg/PortalNetwork/)

---

PortalNetwork is a portal system that gives control back to the players to be able to create portals that can dial each other. It supports different types of portals and allows players to change the portal shape.  Vehicles are also supported.

## Features

* Players can create up to 256 networks of portals with up to 16 portals in each network.  Any portal can dial any other portal in the same network.

* Three different looks of portals.  Nether, End Gateway and Hidden (Invisible) Portals

* Portals can exist on different worlds and can dial each other across worlds.

* Vehicles like Minecarts, Horses, Pigs and boats will teleport as well and maintain their passengers.

* Blocks other than Obsidian can be placed in the portal area. This allows rails to be placed for example.

* Portals only need a base to be built.  When they are dialled they will create their own frames if necessary and clean up after.

* Whatever enters one portal will maintain its relative orientation when existing another portal.  This includes existing the back of the portal when entering the front

* Relative velocity is also maintained. This means flying through with an elytra works well.

* If a portal has one side blocked (for example it is against a wall) then the player existing it will be flipped 180 to exit out the other side. If both sides are blocked then you have a naughty player.

* An API to allow plugins to add additional portal types.