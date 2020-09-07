package ch.alderaan.bukkit.clansandmore;

import org.bukkit.plugin.java.JavaPlugin;

public class ClansAndMore extends JavaPlugin{
	
	private EventListener listener;
	private DataHandler dh;
	
	@Override
	public void onEnable() {
		
		dh = new DataHandler("./plugins/TestProject/data.yml"); //Create DataHandler for Game-Data.
		dh.loaddb();
		listener = new EventListener(dh.getDb());
		
		getServer().getPluginManager().registerEvents(listener, this); // register eventlistener
		getLogger().info("Plugin TestProject enabled.");
		
		this.getCommand("foundclan").setExecutor(listener);
		this.getCommand("joinclan").setExecutor(listener);
		this.getCommand("leaveclan").setExecutor(listener);
		this.getCommand("clan").setExecutor(listener);
		this.getCommand("inviteclan").setExecutor(listener);
		this.getCommand("setport").setExecutor(listener);
		this.getCommand("clans").setExecutor(listener);
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Plugin TestProject disabled.");
		dh.setdb(listener.getData());
		dh.savedb();
	}
}