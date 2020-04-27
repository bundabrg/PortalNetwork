## Permission Summary

| Permission                   |  Description
|---                           |---
| portalnetwork.admin          | Access to all Admin commands
| portalnetwork.command.give   | Access to `give`
| portalnetwork.command.list   | Access to `list`
| portalnetwork.command.reload | Access to `reload`

No permission is necessary to build a portal.

## Commands

Execute commands with `/portalnetwork <command>` or `/pn <command>`.

### `give`

Give a player a `PortalBlock`

`/pn give [-type <portal type>] [<player name>]`

Where:

* `-type <portal type>`: Type of portal block to give. Defaults to `nether`. Built in types are `nether`, `end`, `hidden`.
* `<player name>`: Who to give it to. Defaults to yourself.

!!! info "Permissions (any of)"
    * portalnetwork.admin
    * portalnetwork.command.give
   
!!! examples
    /pn give -type end Bob
    
    /pn give
    
    /pn give -type hidden

### `list`

List all portals

`/pn list`

!!! info "Permissions (any of)"
    * portalnetwork.admin
    * portalnetwork.command.list
   
!!! examples
    /pn list
    

### `reload`

Reload all configuration.

`/pn reload`

!!! info "Permissions (any of)"
    * portalnetwork.admin
    * portalnetwork.command.reload
   
!!! examples
    /pn reload