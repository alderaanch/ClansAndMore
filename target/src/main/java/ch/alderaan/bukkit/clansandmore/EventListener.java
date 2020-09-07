package ch.alderaan.bukkit.clansandmore;

import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;


public final class EventListener implements Listener, CommandExecutor{
	
	List<HashMap<String,String>> bedList =  new ArrayList<HashMap<String,String>>(); // list contains all bed locations and their owners (this is saved)
	List<HashMap<String,String>> playerList = new ArrayList<HashMap<String,String>>(); //list contains all players and their clans (this is saved)
	List<String> clanList = new ArrayList<String>(); //list contains all clans (this is saved)
	List<HashMap<String, String>> portalList = new ArrayList<HashMap<String, String>>(); //list contains all portals with their location, target and name (this is saved)
	List<HashMap<String, String>> inviteList = new ArrayList<HashMap<String,String>>(); //list contains all invites (not saved)
	
	HashMap<String, Object> data; //HashMap contains all game-data.
	
	@SuppressWarnings("unchecked")
	public EventListener(HashMap<String, Object> data) {
		//load data
		this.data = data;
		this.bedList = (List<HashMap<String,String>>) data.get("beds");
		this.playerList = (List<HashMap<String,String>>) data.get("players");
		this.clanList = (List<String>) data.get("clans");
		this.portalList = (List<HashMap<String,String>>) data.get("portals");
	}
	
	
	/*
	 * BLOCK MANIPULATION
	 */
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.getBlock().getBlockData() instanceof org.bukkit.block.data.type.Bed) { //remove bed from bedlist
			int bedx = event.getBlock().getX();
			int bedy = event.getBlock().getY();
			int bedz = event.getBlock().getZ();
			bedList.removeIf(n -> (((n.get("x").equals(Integer.toString(bedx)) || n.get("x").equals(Integer.toString(bedx -1)) 
															 || n.get("x").equals(Integer.toString(bedx +1))) && 
									(n.get("z").equals(Integer.toString(bedz)) || n.get("z").equals(Integer.toString(bedz -1)) 
															 || n.get("z").equals(Integer.toString(bedz + 1))) &&
									n.get("y").equals(Integer.toString(bedy)))));    //remove bed from bedlist (+/- 1 block because beds are double blocks
			data.put("beds", bedList); //update data
			System.out.println(bedx);
			System.out.println(bedList);
		}else if(isBuildingProhibited(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getPlayer().getDisplayName(), 25)) { //check building priviledge 
			event.setCancelled(true);
			event.getPlayer().sendMessage("You are in building prohibited area");
		}else if(event.getBlock().getBlockData().getMaterial() == Material.GLOWSTONE && getPortal(event.getBlock().getLocation()) != null) { //remove portal
			Location loc = event.getBlock().getLocation();
			portalList.removeIf(n -> (Integer.parseInt(n.get("x")) == loc.getBlockX() &&
									Integer.parseInt(n.get("y")) == loc.getBlockY() &&
									Integer.parseInt(n.get("z")) == loc.getBlockZ()));
			data.put("portals", portalList);
			System.out.println("portal destroyed!");
		}
		
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(isBuildingProhibited(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getPlayer().getDisplayName(), 25)) { //check building priviledge
			event.setCancelled(true);
			event.getPlayer().sendMessage("You are in building prohibited area");
		}else if(event.getBlock().getBlockData() instanceof org.bukkit.block.data.type.Bed) { //check if bed
			if(isBuildingProhibited(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getPlayer().getDisplayName(), 50)) { //check if bed radius is overlapping
				event.setCancelled(true);
			}else { // add bed to bed list
				HashMap<String, String> bed = new HashMap<String, String>();
				bed.put("x", Integer.toString(event.getBlock().getX()));
				bed.put("y", Integer.toString(event.getBlock().getY()));
				bed.put("z", Integer.toString(event.getBlock().getZ()));
				
				if(getClan(event.getPlayer().getDisplayName()) != null) { // if player is in clan
					bed.put("owner", getClan(event.getPlayer().getDisplayName())); //put clan as owner
				}else { //if player is not in a clan
					bed.put("owner", event.getPlayer().getDisplayName()); // put player as owner
				}
				
				bedList.add(bed); // ad the bed to the bedlist
				data.put("beds", bedList); //update data
			}
		}
	}
	
	/*
	 * PLAYER MANIPULATION
	 */
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(event.getTo().distance(event.getFrom()) == 0) return; // dont trigger if player only looked around
		
		Location loc = event.getPlayer().getLocation(); // Get Location of player
		loc.setY(loc.getY() - 1);						// Get the block under the player
		HashMap<String, String> portal = getPortal(loc); //Get the portal info (null if not a portal)
		
		if(portal != null) { //teleport if block is a portal
			Location targLoc = loc; //initlialize target portal with source portal (stays that way if theres no target)
			
			for(HashMap<String,String> targPort : portalList) { //retrieve target portal from list
				if(targPort.get("name").equals(portal.get("target"))) {
					targLoc.setX(Integer.parseInt(targPort.get("x")) + 2);
					targLoc.setY(Integer.parseInt(targPort.get("y")) + 1);
					targLoc.setZ(Integer.parseInt(targPort.get("z")));
				}
			}
				event.getPlayer().teleport(targLoc); //teleport player
				event.getPlayer().sendMessage("You were teleported to " + portal.get("target"));
		}
	}
	
	/*
	 * COMMANDS
	 */
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("foundclan") && args.length == 1) {			//FOUND A CLAN
			if(clanList.contains(args[0])) { //check if clan already exists
				sender.sendMessage("Clan already exists");
			}else { //add clan to clanlist and player to new clan
				clanList.add(args[0]);
				playerList.removeIf(n -> n.get("playerName").equals(sender.getName()));
				HashMap<String,String> player = new HashMap<String, String>();
				player.put("playerName", sender.getName());
				player.put("playerClan", args[0]);
				playerList.add(player);
				data.put("players", playerList);
				data.put("clans", clanList);
				sender.sendMessage("You created the clan " + player.get("playerClan"));
			}
			return true;
		}else if(cmd.getName().equalsIgnoreCase("joinclan") && args.length == 1) { //JOIN A CLAN
			if(clanList.contains(args[0]) && isInvited(sender.getName(), args[0])) { //check if clan exists and if player is invited to it
				playerList.removeIf(n -> n.get("playerName").equals(sender.getName()));
				HashMap<String,String> player = new HashMap<String,String>();
				player.put("playerName", sender.getName());
				player.put("playerClan", args[0]);
				playerList.add(player);
				data.put("players", playerList);
				sender.sendMessage("You joined the clan " + player.get("playerClan"));
			}else {
				sender.sendMessage("Clan does not exists or you are not invited.");
			}

			return true;
		}else if(cmd.getName().equalsIgnoreCase("leaveclan")){ // LEAVE A CLAN
			if(getClan(sender.getName()) != null && getClan(sender.getName()) != sender.getName()) { //check if player is in a clan and remove from clan if so
				playerList.removeIf(n -> n.get("playerName").equals(sender.getName()));
				data.put("players", playerList);
				sender.sendMessage("You left your clan");
			}else {
				sender.sendMessage("You are in no clan");
			}
			return true;
		}else if(cmd.getName().equalsIgnoreCase("clan")) { //SHOW YOUR CLAN
			if(getClan(sender.getName()) != null && getClan(sender.getName()) != sender.getName()) { //check if player is in a clan and print out clan if so
				sender.sendMessage("You are in: " + getClan(sender.getName()));
			}else {
				sender.sendMessage("You are in no clan");
			}
			return true;
		}else if(cmd.getName().equalsIgnoreCase("clans")) { // SHOW ALL CLANS
			for(String clan : clanList) {
				sender.sendMessage(clan);
			}
			return true;
		}else if(cmd.getName().equalsIgnoreCase("inviteclan") && args.length == 1) { //INVITE SOMEONE TO A CLAN
			if(getClan(sender.getName()) != null) { //check if issuing player is in a clan and add target player to source players clan if so
				HashMap<String,String> invite = new HashMap<String,String>();
				invite.put("clan", getClan(sender.getName()));
				invite.put("player", args[0]);
				inviteList.add(invite);
				sender.sendMessage("You invited " + invite.get("player") + " to your clan");
				sender.getServer().getPlayer(invite.get("player")).sendMessage("You were invited to " + invite.get("clan"));
			}else {
				sender.sendMessage("You are in no clan");
			}
			return true;
		}else if(cmd.getName().equalsIgnoreCase("setport") && args.length == 2) { //CREATE A GLOW STONE PORTAL
			Location loc = sender.getServer().getPlayer(sender.getName()).getLocation();
			loc.setY(loc.getBlockY() - 1);
			if(loc.getBlock().getBlockData().getMaterial() == Material.GLOWSTONE) { //check if player stands on glowstone
				HashMap<String, String> portal = new HashMap<String, String>();
				portal.put("x", Integer.toString(loc.getBlockX()));
				portal.put("y", Integer.toString(loc.getBlockY()));
				portal.put("z", Integer.toString(loc.getBlockZ()));
				portal.put("name", args[0]);
				portal.put("target", args[1]);
				portalList.add(portal);
				data.put("portals", portalList);
				sender.sendMessage("You set the Portal at " + portal.get("name") + " to " + portal.get("target"));
			}else {
				sender.sendMessage("Your are not standing on glowstone");
			}
			return true;
		}
		return false;
	}
	
	/*
	 * SUPPORT METHODS
	 */
	
	private boolean isBuildingProhibited(int x, int y, int z, String playerName, int rad) { //check building priviledge (true if player cant build, false if player can build)
		boolean isPriv = false;
		if(bedList != null) {
			for(HashMap<String, String> bed : bedList) {
				int bedx= Integer.parseInt(bed.get("x"));
				int bedy= Integer.parseInt(bed.get("y"));
				int bedz= Integer.parseInt(bed.get("z"));
				String bedOwner = bed.get("owner");

				if(x <= bedx + rad && x >= bedx - rad && // check if clan owns bed in 50 block radius
					y <= bedy + rad && y >= bedy - rad &&
					z <= bedz + rad && z >= bedz - rad &&
					(!getClan(playerName).equals(bedOwner) &&
					!playerName.equals(bedOwner))) {
					isPriv = true;
				}
			}
		}

		return isPriv;
	}
	
	private String getClan(String playerName) { //get clan from player
		String clanName = playerName; //default clan is own player name
		for(HashMap<String,String> player : playerList) {  //check all players in playerlist
			if(player.get("playerName").equals(playerName)) {
				clanName = player.get("playerClan");
			}
		}
		return clanName;
	}
	
	private boolean isInvited(String playerName, String clanName) { //check if player is invited to a specific clan
		boolean isInvited = false;
		for(HashMap<String, String> invite : inviteList) { //check all invites in invite list
			if(invite.get("player").equals(playerName) && invite.get("clan").equals(clanName)) {
				isInvited = true;
			}
		}
		return isInvited;
	}
	
	private HashMap<String, String> getPortal(Location loc) { //get portal information of block under player (null if no portal)
		HashMap<String, String> portal = null;
		for(HashMap<String,String> port : portalList) {
			if(Integer.parseInt(port.get("x")) == loc.getBlockX() &&
				Integer.parseInt(port.get("y")) == loc.getBlockY() &&
				Integer.parseInt(port.get("z")) == loc.getBlockZ()) {
				portal = port;
			}
		}
		
		return portal;
	}
	
	public HashMap<String,Object> getData(){ //get data from this eventlistener for saving purposes
		return data;
	}
}
