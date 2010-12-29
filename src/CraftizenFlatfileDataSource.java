import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;


public class CraftizenFlatfileDataSource extends CraftizenDataSource
{
	static final Logger log = Logger.getLogger("Minecraft");

	private static final String DATA_FOLDER = "craftizens/";

	private HashMap<String, ArrayList<String>> npcQuests;

	public CraftizenFlatfileDataSource()
	{
		(new File(DATA_FOLDER)).mkdir();
	}

	public HashSet<Craftizen> loadCraftizens()
	{
		synchronized (craftizenLock)
		{
			HashSet<Craftizen> npcs = new HashSet<Craftizen>();
			npcQuests = new HashMap<String, ArrayList<String>>();

			// get file list
			File[] files = (new File(DATA_FOLDER)).listFiles();

			// load npc data
			for (File file : files)
			{
				if (file.getName().startsWith("npc-"))
				{
					PropertiesFile data = new PropertiesFile(DATA_FOLDER
							+ file.getName());
					Craftizen npc = new Craftizen(data.getString("npc_id"),
							data.getString("npc_name"), data.getDouble("posx"),
							data.getDouble("posy"), data.getDouble("posz"),
							(float) data.getDouble("rotation"),
							(float) data.getDouble("pitch"),
							data.getInt("item_in_hand"));

					int i = 0;
					ArrayList<String> dialog = new ArrayList<String>();
					while (data.containsKey("dialog_" + i))
					{
						dialog.add(data.getString("dialog_" + i));
						i++;
					}
					npc.setDialog(dialog);
					npcs.add(npc);
					npcQuests.put(npc.getId(), new ArrayList<String>());
				}
			}

			// load quest data
			for (File file : files)
			{
				if (file.getName().startsWith("quest-"))
				{
					PropertiesFile data = new PropertiesFile(DATA_FOLDER
							+ file.getName());
					ArrayList<String> quests = npcQuests.get(data
							.getString("start_npc"));
					quests.add(data.getString("id"));
					npcQuests.put(data.getString("start_npc"), quests);
				}
			}
			return npcs;
		}
	}

	public void saveCraftizen(Craftizen c)
	{
		synchronized (craftizenLock)
		{
			PropertiesFile data = new PropertiesFile(DATA_FOLDER + "npc-"
					+ c.getId() + ".txt");
			data.setString("npc_id", c.getId());
			data.setString("npc_name", c.getName());
			data.setDouble("posx", c.getX());
			data.setDouble("posy", c.getY());
			data.setDouble("posz", c.getZ());
			data.setDouble("rotation", c.getRotation());
			data.setDouble("pitch", c.getPitch());
			data.setInt("item_in_hand", c.getItemInHand());
		}
	}

	public void addCraftizenDialog(String npcid, String dialogid, String dialog)
	{
		synchronized (craftizenLock)
		{
			PropertiesFile data = new PropertiesFile(DATA_FOLDER + "npc-"
					+ npcid + ".txt");
			int i = -1;
			while (data.containsKey("dialog_" + ++i))
			{
			}
			data.setString("dialog_" + i, dialog);
		}
	}

	public void deleteCraftizen(String id)
	{
		synchronized (craftizenLock)
		{
			(new File(DATA_FOLDER + "npc-" + id + ".txt")).delete();
		}
	}

	public ArrayList<String> getQuestList()
	{
		synchronized (questLock)
		{
			ArrayList<String> quests = null;

			// get file list
			File[] files = (new File(DATA_FOLDER)).listFiles();

			// load quest data
			for (File file : files)
			{
				if (file.getName().startsWith("quest-"))
				{
					PropertiesFile data = new PropertiesFile(DATA_FOLDER
							+ file.getName());
					if (quests == null) quests = new ArrayList<String>();
					quests.add(data.getString("id"));
				}
			}

			return quests;
		}
	}

	public QuestInfo loadQuestInfo(String id)
	{
		synchronized (questLock)
		{
			if ((new File(DATA_FOLDER + "quest-" + id + ".txt")).exists())
			{
				PropertiesFile data = new PropertiesFile(DATA_FOLDER + "quest-"
						+ id + ".txt");
				QuestInfo quest = new QuestInfo(data.getString("id"),
						data.getString("quest_type"),
						data.getString("quest_name"),
						data.getString("quest_desc"),
						data.getString("start_npc"), data.getString("end_npc"),
						data.getString("prereq"),
						data.getString("items_provided"),
						data.getString("rewards"), data.getString("location"),
						data.getString("data"),
						data.getString("completion_text"),
						data.getString("rankreq"),
						data.getString("rankreward"), data.getString("cost"),
						data.getString("prize"));
				return quest;
			}
			else
			{
				return null;
			}
		}
	}

	public ArrayList<QuestInfo> getAvailableQuests(Craftizen c, Player p)
	{
		synchronized (questLock)
		{
			ArrayList<QuestInfo> quests = new ArrayList<QuestInfo>();
			// getting quests this NPC has
			ArrayList<String> questNames = npcQuests.get(c.id);
			if (questNames != null) {
				for (String q : npcQuests.get(c.id))
				{
					// now we only add QuestInfo objects that the player can
					// actually access
					QuestInfo quest = loadQuestInfo(q);
	
					// logic for ranks
					String[] groups = p.getGroups();
					if (groups.length > 0)
					{
						if (groups[0] == null || groups[0].equals(""))
						{
							if (!quest.rankReq.isEmpty())
							{
								continue;
							}
						}
						else
						{
							if ((!quest.rankReq.isEmpty())
									&& (!quest.rankReq.equalsIgnoreCase(groups[0])))
							{
								continue;
							}
						}
					}
					else
					{
						if (!quest.rankReq.isEmpty())
						{
							continue;
						}
					}
	
					PropertiesFile player = loadPlayer(p);
					// logic for active quests
					String[] activeQuests = player.getString("active-quests")
							.split(",");
					boolean activeFound = false;
					for (String aq : activeQuests)
					{
						if (aq.equalsIgnoreCase(q))
						{
							activeFound = true;
							break;
						}
					}
					if (activeFound)
					{
						continue;
					}
					// logic for already completed quests
					String[] completedQuests = player.getString("completed-quests")
							.split(",");
					boolean completedFound = false;
					for (String cq : completedQuests)
					{
						if (cq.equalsIgnoreCase(q))
						{
							completedFound = true;
							break;
						}
					}
					if (completedFound)
					{
						continue;
					}
	
					// add the quest to the list
					quests.add(quest);
				}
			}
			return quests;
		}
	}

	private PropertiesFile loadPlayer(Player p)
	{
		return new PropertiesFile(DATA_FOLDER + "player-" + p.getName()
				+ ".txt");
	}

	public HashMap<QuestInfo, String> getActiveQuests(Player p)
	{
		synchronized (questLock)
		{
			HashMap<QuestInfo, String> quests = new HashMap<QuestInfo, String>();

			PropertiesFile player = loadPlayer(p);
			String[] q = player.getString("active-quests").split(",");
			for (String aq : q)
			{
				QuestInfo quest = loadQuestInfo(aq);
				if (quest == null)
				{
					continue;
				}
				String progress = player.getString("progress-" + aq);
				quests.put(quest, progress);
			}
			return quests;
		}
	}

	public void saveActiveQuest(Player player, Quest quest)
	{
		synchronized (questLock)
		{
			PropertiesFile p = loadPlayer(player);
			if (p.getString("active-quests").isEmpty())
			{
				p.setString("active-quests", quest.getId());
			}
			else
			{
				p.setString("active-quests", p.getString("active-quests") + ","
						+ quest.getId());
			}
		}
	}

	public void saveQuestProgress(Player player, Quest quest, String progress)
	{
		synchronized (questLock)
		{
			PropertiesFile p = loadPlayer(player);
			p.setString("progress-" + quest.getId(), progress);
		}
	}

	public void dropActiveQuest(Player player, Quest quest)
	{
		synchronized (questLock)
		{
			PropertiesFile p = loadPlayer(player);
			String[] q = p.getString("active-quests").split(",");
			String activeQuests = "";
			for (String aq : q)
			{
				if (!quest.getId().equalsIgnoreCase(aq))
				{
					if (activeQuests.isEmpty())
					{
						activeQuests = aq;
					}
					else
					{
						activeQuests = activeQuests + "," + aq;
					}
				}
			}
			p.setString("active-quests", activeQuests);
		}
	}

	public void saveCompletedQuest(Player player, Quest quest)
	{
		dropActiveQuest(player, quest);
		synchronized (questLock)
		{
			PropertiesFile p = loadPlayer(player);
			if (p.getString("completed-quests").isEmpty())
			{
				p.setString("completed-quests", quest.getId());
			}
			else
			{
				p.setString("completed-quests", p.getString("completed-quests")
						+ "," + quest.getId());
			}
		}
	}

	public void saveQuest(QuestInfo quest)
	{
		synchronized (questLock)
		{
			PropertiesFile data = new PropertiesFile(DATA_FOLDER + "quest-"
					+ quest.getId() + ".txt");

			data.setString("id", quest.id);
			data.setString("quest_name", (quest.name == null) ? "" : quest.name);
			data.setString("quest_type", (quest.type == null) ? "" : quest.type);
			data.setString("quest_desc", (quest.desc == null) ? "" : quest.desc);
			data.setString("start_npc", (quest.pickUp == null) ? ""
					: quest.pickUp);
			data.setString("end_npc", (quest.turnIn == null) ? ""
					: quest.turnIn);
			data.setString("prereq", (quest.prereq == null) ? "" : quest.prereq);
			data.setString("items_provided",
					(quest.itemsProvidedStr == null) ? ""
							: quest.itemsProvidedStr);
			data.setString("rewards", (quest.rewardsStr == null) ? ""
					: quest.rewardsStr);
			data.setString("location", (quest.location == null) ? ""
					: quest.location);
			data.setString("data", (quest.data == null) ? "" : quest.data);
			data.setString("completion_text",
					(quest.completionText == null) ? "" : quest.completionText);
			data.setString("rankreq", (quest.rankReq == null) ? ""
					: quest.rankReq);
			data.setString("rankreward", (quest.rankReward == null) ? ""
					: quest.rankReward);
			data.setInt("cost", quest.cost);
			data.setInt("prize", quest.prize);
		}
	}

	public void deleteQuest(String questid)
	{
		synchronized (questLock)
		{
			(new File(DATA_FOLDER + "quest-" + questid + ".txt")).delete();
		}
	}

}
