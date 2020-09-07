package ch.alderaan.bukkit.clansandmore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class DataHandler {
	private HashMap<String, Object> db = new HashMap<String,Object>();
	private File dbFile;
	private FileConfiguration dbConfig;
	
	public DataHandler(String filePath) {
		dbFile = new File(filePath);
	}
	
	public void loaddb() {
		if(!dbFile.exists()) {
			try {
				dbFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		dbConfig = YamlConfiguration.loadConfiguration(dbFile);
		for(String key : dbConfig.getKeys(true)) {
			db.put(key, dbConfig.get(key));
		}
		
		if(db.isEmpty()) {
			db.put("clans", new ArrayList<String>());
			db.put("beds", new ArrayList<HashMap<String,String>>());
			db.put("players", new ArrayList<HashMap<String,String>>());
			db.put("portals", new ArrayList<HashMap<String,String>>());
		}
	}
	
	public void setdb(HashMap<String,Object> db) {
		this.db = db;
	}
	
	public void savedb() {
		for(Map.Entry<String,Object> entry : db.entrySet()) {
			dbConfig.set(entry.getKey(), entry.getValue());
		}
		try {
			dbConfig.save(dbFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public HashMap<String, Object> getDb(){
		return db;
	}
}
