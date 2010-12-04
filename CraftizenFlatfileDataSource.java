import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;

public class CraftizenFlatfileDataSource extends CraftizenDataSource {

	private static final String DATA_FOLDER = "craftizens/";
	
	private HashMap<String,ArrayList<String>> npcQuests;

	public CraftizenFlatfileDataSource() {
		(new File(DATA_FOLDER)).mkdir();
	}
	
	public HashSet<Craftizen> loadCraftizens() {
		synchronized (craftizenLock) {
			HashSet<Craftizen> npcs = new HashSet<Craftizen>();
			npcQuests = new HashMap<String,ArrayList<String>>();
			
			// get file list
			File [] files = (new File(DATA_FOLDER)).listFiles();
			
			// load npc data
			for (File file : files) {
				if (file.getName().startsWith("npc-")) {
					PropertiesFile data = new PropertiesFile(DATA_FOLDER + file.getName());
					Craftizen npc = new Craftizen(
							data.getString("npc_id"),
							data.getString("npc_name"),
							data.getDouble("posx"),
							data.getDouble("posy"),
							data.getDouble("posz"),
							(float)data.getDouble("rotation"),
							(float)data.getDouble("pitch"),
							data.getInt("item_in_hand")
						);
					
					int i = 0;
					ArrayList<String> dialog = new ArrayList<String>();
					while (data.containsKey("dialog_" + i)) {
						dialog.add(data.getString("dialog_" + i));
						i++;
					}
					npc.setDialog(dialog);
					npcs.add(npc);
					npcQuests.put(npc.getId(), new ArrayList<String>());
				}
			}
			
			// TODO: load quest data
			
		
			return npcs;
		}
	}
	
	public void saveCraftizen(Craftizen c) {
		synchronized (craftizenLock) {
			PropertiesFile data = new PropertiesFile(DATA_FOLDER + "npc-" + c.getId() + ".txt");
			data.setString("npc_id",c.getId());
			data.setString("npc_name",c.getName());
			data.setDouble("posx",c.getX());
			data.setDouble("posy",c.getY());
			data.setDouble("posz",c.getZ());
			data.setDouble("rotation",c.getRotation());
			data.setDouble("pitch",c.getPitch());
			data.setInt("item_in_hand",c.getItemInHand());
		}
	}
	
	public void addCraftizenDialog(String npcid, String dialogid, String dialog) {
		synchronized (craftizenLock) {
			PropertiesFile data = new PropertiesFile(DATA_FOLDER + "npc-" + npcid + ".txt");
			int i = -1;
			while (data.containsKey("dialog_" + ++i)) {}
			data.setString("dialog_" + i, dialog);
		}
	}
	
	public void deleteCraftizen(String id) {
		synchronized (craftizenLock) {
			(new File(DATA_FOLDER + "npc-" + id + ".txt")).delete();
		}
	}
	
	public ArrayList<String> getQuestList() { 
		return new ArrayList<String>(); 
	}
	
	public QuestInfo loadQuestInfo(String id) { 
		return null;
	}
	
	public ArrayList<QuestInfo> getAvailableQuests(Craftizen c, Player p) { 
		return new ArrayList<QuestInfo>(); 
	}
	
	public HashMap<QuestInfo,String> getActiveQuests(Player p) { 
		return new HashMap<QuestInfo,String>();
	}
	
	public void saveActiveQuest(Player player, Quest quest) {}
	
	public void saveQuestProgress(Player player, Quest quest, String progress) {}
	
	public void dropActiveQuest(Player player, Quest quest) {}

	public void saveCompletedQuest(Player player, Quest quest) {}
	
	public void saveQuest(QuestInfo quest) {}
	
	public void deleteQuest(String questid) {}

}
