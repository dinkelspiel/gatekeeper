# Gatekeeper

A simple way to have a player invites player structured whitelist for survival servers

Gatekeeper keeps a track of which players each player has invited so that you can ban bad actors who are inviting undesired players.

# Usage Player

## Inviting players
Permission: `keii.gatekeeper.invite`
Invite a player using `/invite <Minecraft Username>`

# Usage Admin

## Save
Permission: `keii.gatekeeper.save`
Save the data using `/gatekeeper save`

Saving happens automatically at server stop and /reload

## Load
Permission: `keii.gatekeeper.load`
Load the data using `/gatekeeper load`

Loading happens automatically at server start and after /reload