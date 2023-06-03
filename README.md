#Lobby System
About:
A plugin which adds a waiting room/lobby for your mini game server. Once the minimum required amount of players have joined, a countdown starts and allows players to /vote for a map, which the winning map is chosen and players are warped to it.

This plugin was designed to work with the War plugin however it may be adapted to your needs. (https://github.com/iangry0/war/)

#Commands:
* /Vote (map)
* /Vote Addmap (map) - adds a new map spawn point at your current location

In the configuration, all messages are configurable

#How to setup plugin:
On first install, go to your mini game portal gate or map then create run /Vote addmap (map) to create a teleportation warp at your location. This is the place players will be teleported to when the map wins the vote

![image](https://github.com/iangry0/LobbySystem/assets/77093975/519884b4-771c-4585-9823-205981ef0974)


You can add as many maps as you like. Once this is complete, go into your configuration to set a lobby world. Make sure to set spawn there and allow all players to go to spawn on join.
Once the plugin detects the minimum amount of players in the lobby world, the voting begins and allows players to type /vote (map). There is a countdown for 2 minutes. The players then get teleported to the winning map.



#To do:

   * Configurable minimum player amount to start countdown/voting
   * Permissions
   * Remove map command
   * Reload config command
   * Allow command to be run for all players on voting map win instead of just teleporting to the area


![image](https://github.com/iangry0/LobbySystem/assets/77093975/4744e5d2-dd9e-4b82-870f-66c371b77901)
