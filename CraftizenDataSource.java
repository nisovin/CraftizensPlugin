import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class CraftizenDataSource {

	protected static Object dbLock = new Object();
	protected static Object craftizenLock = new Object();
	protected static Object questLock = new Object();

	abstract HashSet<Craftizen> loadCraftizens();
	
	abstract void saveCraftizen(Craftizen c);
	
	abstract void addCraftizenDialog(String npcid, String dialogid, String dialog);
	
	void deleteCraftizen(Craftizen c) {
		deleteCraftizen(c.getId());
	}
	
	abstract void deleteCraftizen(String id);
	
	abstract ArrayList<String> getQuestList();
	
	abstract QuestInfo loadQuestInfo(String id);
	
	abstract ArrayList<QuestInfo> getAvailableQuests(Craftizen c, Player p);
	
	abstract HashMap<QuestInfo,String> getActiveQuests(Player p);
	
	abstract void saveActiveQuest(Player player, Quest quest);
	
	abstract void saveQuestProgress(Player player, Quest quest, String progress);
	
	abstract void dropActiveQuest(Player player, Quest quest);

	abstract void saveCompletedQuest(Player player, Quest quest);
	
	abstract void saveQuest(QuestInfo quest);
	
	abstract void deleteQuest(String questid);
	
}
