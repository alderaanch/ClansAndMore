# ClansAndMore
A Minecraft Bukkit/Spigot Server plugin, that allows clans to be formed, building protection and portals
## Description
This Plugin allows to create clans, join them, invite people to them, protect areas, create portals.  
Area Protection is done by placing beds. This feature is enabled upon enabling the Plugin itself. Upon placing a bed an area of X +/- 25, Y +/- 25, Z +/- 25 is Protected against grief for all other players except the ones in the Clan you are in while placing the bed.  
If you are switching or leaving clans, it is best to replace your bed, so that your "old" clan can't destroy or build on your area.  
## Commands
/foundclan [clanname]: creates a clan and joins the command-issuer to it (if it doesn't already exist.  
/inviteclan [playername]: invites a player to the clan you are currently in. (player can now join the clan with "/joinclan", until server-restart.  
/joinclan [clanname]: joins the command-issuer to the clan specified, if he is invited.  
/clan: Shows you the clanname you are currently in.  
/clans Shows all clans in the server.  
