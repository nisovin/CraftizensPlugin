import java.util.logging.*;
import java.util.ArrayList;
import java.util.HashMap;

public class CraftizensListener extends PluginListener {
	static final Logger log = Logger.getLogger("Minecraft");

	public boolean onCommand(Player player, String [] split) {
		if (split[0].equals("/craftnpc") && player.canUseCommand("/craftnpc")) {
			npcCommand(player, split);
			return true;
		} else if ((split[0].equalsIgnoreCase("/quest") || split[0].equalsIgnoreCase("/q")) && player.canUseCommand("/quest")) {
			questCommand(player, split);
			return true;
		} else if (split[0].equalsIgnoreCase("/qadmin") && player.canUseCommand("/qadmin")) {
			qadminCommand(player, split);
			return true;
		}
		return false;
	}
	
	public void npcCommand(Player player, String [] command) {	
		if (command.length > 1 && command[1].equals("clear")) {
			for (Craftizen npc : Craftizens.npcs) {
				npc.delete();
			}
			Craftizens.npcs.clear();
		} else if (command.length > 1 && command[1].equals("list")) {
			for (Craftizen npc : Craftizens.npcs) {
				player.sendMessage(Craftizens.TEXT_COLOR + npc.getId() + " - " + npc.getName() + " - " +
						((int) Math.floor(npc.getX())) + "," + 
						((int) Math.floor(npc.getY())) + "," + 
						((int) Math.floor(npc.getZ())) + ",");
			}
		} else if (command.length > 3 && command[1].equals("create")) {
			String id = command[2];
			String n = "";
			for (int i = 3; i < command.length; i++) {
				n += command[i] + " ";
			}
			n = n.substring(0,n.length()-1);
			Craftizen c = new Craftizen(id, n, player.getX(), player.getY(), player.getZ(), player.getRotation(), player.getPitch(), player.getItemInHand());
			Craftizens.npcs.add(c);
			Craftizens.data.saveCraftizen(c);
		
			player.sendMessage("NPC '" + id + "' created.");
			
		} else if (command.length > 4 && command[1].equals("adddialog")) {
			String npcid = command[2];
			String dialogid = command[3];
			String dialog = "";
			for (int i = 4; i < command.length; i++) {
				dialog += command[i] + " ";
			}
			dialog = dialog.trim();
			Craftizen npc = Craftizen.getCraftizen(npcid);
			if (npc != null) {
				npc.addDialog(dialog);
				Craftizens.data.addCraftizenDialog(npcid, dialogid, dialog);
				player.sendMessage("Dialog added for npc " + npcid);
			} else {
				player.sendMessage("No such npc");
			}
		
		} else if (command.length == 3 && command[1].equals("delete")) {
			
			Craftizen.delete(command[2]);
			Craftizens.data.deleteCraftizen(command[2]);
			
			player.sendMessage("NPC '" + command[2] + "' deleted.");
			
		} else {
			player.sendMessage(Craftizens.TEXT_COLOR + "Usage instructions:");
			player.sendMessage(Craftizens.TEXT_COLOR + "   /npc list - list your npcs");
			player.sendMessage(Craftizens.TEXT_COLOR + "   /npc create <id> <name> - create a new npc");
			player.sendMessage(Craftizens.TEXT_COLOR + "   /npc adddialog <npcid> <dialogid> <dialogtext> - add some dialog for your npc");
			player.sendMessage(Craftizens.TEXT_COLOR + "   /npc delete <id> - delete your npc");	
		}
	
	}
	
	@SuppressWarnings("unchecked")
	public void questCommand(Player player, String [] command) {
		if (command.length == 1) {
			questCommandUsage(player);
		} else if ("view".startsWith(command[1].toLowerCase()) && command.length == 3) {
			if (Craftizens.pendingQuests.containsKey(player.getName())) {
				Object o = Craftizens.pendingQuests.get(player.getName());
				if (o instanceof ArrayList<?>) {
					ArrayList<QuestInfo> quests = null;
					try {
// TODO: this is extremely poor programming practice. Need to refactor/fix later.
						quests = (ArrayList<QuestInfo>) o;
					} catch (ClassCastException e) {
						Craftizen.log.warning("[Craftizen] Pending quests was not an ArrayList<QuestInfo>!");
					}
					int i = -1;
					try {
						i = Integer.parseInt(command[2]);
					} catch (NumberFormatException e) {
					}
					if (i > 0 && quests.size() >= i) {
						QuestInfo qi = quests.get(i-1);
						qi.show(player);
						Craftizens.pendingQuests.put(player.getName(), qi);
					} else {
						player.sendMessage(Craftizens.TEXT_COLOR + "Invalid quest number.");
					}				
				} else {
					player.sendMessage(Craftizens.TEXT_COLOR + "No quest options to view.");
				}
			} else {
				player.sendMessage(Craftizens.TEXT_COLOR + "No quest options to view.");
			}
			
		} else if ("accept".startsWith(command[1].toLowerCase()) && command.length == 2) {
			if (Craftizens.pendingQuests.containsKey(player.getName())) {
				Object o = Craftizens.pendingQuests.get(player.getName());
				if (o instanceof QuestInfo) {
					QuestInfo qi = (QuestInfo)o;
					if (qi.checkBalance(player, true)) {
						Quest q = qi.createQuest(player, true);
						Craftizens.pendingQuests.remove(player.getName());
						if (!Craftizens.activeQuests.containsKey(player.getName())) {
							Craftizens.activeQuests.put(player.getName(),new ArrayList<Quest>());
						}
						Craftizens.activeQuests.get(player.getName()).add(q);
						Craftizens.data.saveActiveQuest(player, q);
						player.sendMessage(Craftizens.TEXT_COLOR + "Quest accepted!");
					} else {
						player.sendMessage(Craftizens.TEXT_COLOR + "You can't afford this quest! It costs " + qi.cost + ".");
					}
				} else {
					player.sendMessage(Craftizens.TEXT_COLOR + "No quest to accept.");
				}
			} else {
				player.sendMessage(Craftizens.TEXT_COLOR + "No quest to accept.");
			}
			
		} else if ("progress".startsWith(command[1].toLowerCase()) && command.length == 3) {
			if (Craftizens.activeQuests.containsKey(player.getName())) {
				ArrayList<Quest> quests = Craftizens.activeQuests.get(player.getName());
				int i = -1;
				try {
					i = Integer.parseInt(command[2]);
				} catch (NumberFormatException e) {
				}
				if (i > 0 && quests.size() >= i) {
					String [] prog = quests.get(i-1).getProgress().split("@");
					for (int j = 0; j < prog.length; j++) {
						player.sendMessage(Craftizens.TEXT_COLOR + (j==0?"Quest progress: ":"   ") + prog[j]);
					}
				} else {
					player.sendMessage(Craftizens.TEXT_COLOR + "Invalid quest number.");
				}
			} else {
				player.sendMessage(Craftizens.TEXT_COLOR + "No active quests.");
			}
			
		} else if ("list".startsWith(command[1].toLowerCase()) || command[1].equalsIgnoreCase("ls")) {
			if (Craftizens.activeQuests.containsKey(player.getName())) {
				ArrayList<Quest> quests = Craftizens.activeQuests.get(player.getName());
				if (quests.size() > 0) {
					player.sendMessage(Craftizens.TEXT_COLOR + "Active quests:");
					for (int i = 0; i < quests.size(); i++) {
						player.sendMessage("   " + (i+1) + ": " + quests.get(i).getName());
					}
				} else {
					player.sendMessage(Craftizens.TEXT_COLOR + "No active quests.");
				}
			} else {
				player.sendMessage(Craftizens.TEXT_COLOR + "No active quests.");
			}
			
		} else if ("description".startsWith(command[1].toLowerCase()) && command.length == 3) {
			if (Craftizens.activeQuests.containsKey(player.getName())) {
				ArrayList<Quest> quests = Craftizens.activeQuests.get(player.getName());
				int i = -1;
				try {
					i = Integer.parseInt(command[2]);
				} catch (NumberFormatException e) {
				}
				if (i > 0 && quests.size() >= i) {
					quests.get(i-1).show(player);
				} else {
					player.sendMessage(Craftizens.TEXT_COLOR + "Invalid quest number.");
				}
			} else {
				player.sendMessage(Craftizens.TEXT_COLOR + "No active quests.");
			}
			
		} else if ("compass".startsWith(command[1].toLowerCase()) && command.length == 3) {
			if (Craftizens.activeQuests.containsKey(player.getName())) {
				ArrayList<Quest> quests = Craftizens.activeQuests.get(player.getName());
				int i = -1;
				try {
					i = Integer.parseInt(command[2]);
				} catch (NumberFormatException e) {
				}
				if (i > 0 && quests.size() >= i) {
					quests.get(i-1).compass();
				} else {
					player.sendMessage(Craftizens.TEXT_COLOR + "Invalid quest number.");
				}
			} else {
				player.sendMessage(Craftizens.TEXT_COLOR + "No active quests.");
			}			
			
		} else if ("drop".startsWith(command[1].toLowerCase())) {
			if (Craftizens.activeQuests.containsKey(player.getName())) {
				ArrayList<Quest> quests = Craftizens.activeQuests.get(player.getName());
				int i = -1;
				try {
					i = Integer.parseInt(command[2]);
				} catch (NumberFormatException e) {
				}
				if (i > 0 && quests.size() >= i) {
					Craftizens.data.dropActiveQuest(player, quests.get(i-1));
					player.sendMessage(Craftizens.TEXT_COLOR + "Quest " + quests.get(i-1).getName() + " dropped.");
					quests.remove(i-1);
				} else {
					player.sendMessage(Craftizens.TEXT_COLOR + "Invalid quest number.");
				}				
			} else {
				player.sendMessage(Craftizens.TEXT_COLOR + "No active quests.");
			}
		
		} else /* help */ {
			questCommandUsage(player);
		}
	}
	
	public void questCommandUsage(Player player) {
		player.sendMessage(Craftizens.TEXT_COLOR + "Usage instructions:");
		player.sendMessage(Craftizens.TEXT_COLOR + "   /quest list - list your active quests");
		player.sendMessage(Craftizens.TEXT_COLOR + "   /quest progress # - shows progress on the quest");
		player.sendMessage(Craftizens.TEXT_COLOR + "   /quest desc # - shows the quest description");
		player.sendMessage(Craftizens.TEXT_COLOR + "   /quest compass # - makes your compass point to the quest");
	}
	
	public void qadminCommand(Player player, String [] command) {
		if (command.length == 3 && command[1].equalsIgnoreCase("new")) {
			if (command[2].matches("^[a-zA-Z0-9]+$")) {
				if (Craftizens.newQuests == null) {
					Craftizens.newQuests = new HashMap<String,QuestInfo>();
				}
				Craftizens.newQuests.put(player.getName(), new QuestInfo(command[2]));
				player.sendMessage("Quest " + command[2] + " created.");
			} else {
				player.sendMessage("Invalid quest id.");
			}
			
		} else if (command.length == 3 && command[1].equalsIgnoreCase("load")) {
			QuestInfo q = Craftizens.data.loadQuestInfo(command[2]);
			if (q != null) {
				if (Craftizens.newQuests == null) {
					Craftizens.newQuests = new HashMap<String,QuestInfo>();
				}
				Craftizens.newQuests.put(player.getName(), q);
				player.sendMessage("Quest " + command[2] + " (" + q.name + ") loaded.");
			} else {
				player.sendMessage("No such quest id.");
			}
			
		} else if (command.length == 3 && command[1].equalsIgnoreCase("info")) {
			QuestInfo q = Craftizens.data.loadQuestInfo(command[2]);
			if (q != null) {
				player.sendMessage("Quest ID: " + q.id);
				player.sendMessage("Name: " + q.name);
				player.sendMessage("Type: " + q.type);
				player.sendMessage("Desc: " + ((q.desc.length()>50)?q.desc.substring(0,50)+"...":q.desc));
				player.sendMessage("Start NPC: " + q.pickUp);
				player.sendMessage("End NPC: " + q.turnIn);
				player.sendMessage("Prereq quest: " + q.prereq);
				player.sendMessage("Items prov: " + q.itemsProvidedStr);
				player.sendMessage("Rewards: " + (q.rewardsStr == null ? "None" : q.rewardsStr));
				player.sendMessage("Rank requirement: " + q.rankReq);
				player.sendMessage("Rank reward: " + q.rankReward);
				player.sendMessage("Cost: " + q.cost + " (iConomy is currently " + (Craftizens.ICONOMY_DETECTED ? "enabled)" : "disabled)"));
				player.sendMessage("Prize: " + q.prize + " (iConomy is currently " + (Craftizens.ICONOMY_DETECTED ? "enabled)" : "disabled)"));
				player.sendMessage("Comp text: " + ((q.completionText != null && q.completionText.length()>50)?q.completionText.substring(0,50)+"...":q.completionText));
				player.sendMessage("Data: " + q.data);
				q = null;
			} else {
				player.sendMessage("No such quest id.");
			}
			
		} else if (command.length == 2 && command[1].equalsIgnoreCase("list")) {
			ArrayList<String> quests = Craftizens.data.getQuestList();
			if (quests != null) {
				String s = "";
				for (int i = 0; i < quests.size(); i++) {
					s += quests.get(i) + " ";
				}
				player.sendMessage("Quests: " + s);
			} else {
				player.sendMessage("No quests.");
			}
			
		} else if (command.length == 3 && command[1].equalsIgnoreCase("delete")) {
			QuestInfo qi = Craftizens.data.loadQuestInfo(command[2]);
			if (qi != null) {
				Craftizens.pendingQuests.clear();
				for (String p : Craftizens.activeQuests.keySet()) {
					ArrayList<Quest> quests = Craftizens.activeQuests.get(p);
					for (int i = 0; i < quests.size(); i++) {
						if (quests.get(i).getId().equals(qi.getId())) {
							quests.remove(i);
							break;
						}
					}
				}
				Craftizens.data.deleteQuest(command[2]);
				player.sendMessage("Quest " + command[2] + " deleted.");
			} else {
				player.sendMessage("No such quest.");
			}
			
		// iConomy Hook
		} else if (command.length >= 2 && command[1].equalsIgnoreCase("iConomy")) {
			if (command.length > 2 && command[2].equals("-disable")) {
				Craftizens.ICONOMY_DETECTED = false;
				player.sendMessage("iConomy disabled. Use /qadmin iConomy to turn it back on.");
			} else if (Craftizens.loadiConomy()) {
				player.sendMessage("iConomy loaded successfully.");
				player.sendMessage("use /qadmin iConomy -disable to turn it off.");
			} else {
				player.sendMessage("iConomy failed to load.");
				player.sendMessage("Perhaps you don't have the plugin running?");
			}
			
		} else if (command.length >= 2 && Craftizens.newQuests != null && Craftizens.newQuests.containsKey(player.getName())) {
		
			QuestInfo quest = Craftizens.newQuests.get(player.getName());
			
			// id
			if (command[1].equalsIgnoreCase("id")) {
				if (command.length == 3) {
					if (quest.setId(command[2])) {
						player.sendMessage("Quest id set to: " + command[2] + ".");	
					} else {
						player.sendMessage("Invalid quest id.");
					}
				} else if (command.length == 2) {
					player.sendMessage("Quest id: " + quest.id);
				} else {
					player.sendMessage("Use /qadmin id [questid] to set the quest id.");
				}
			
			// set name
			} else if (command[1].equalsIgnoreCase("name")) {
				if (command.length > 2) {
					String n = "";
					for (int i = 2; i < command.length; i++) {
						n = n + command[i] + " ";
					}
					n = n.trim();
					if (quest.setName(n)) {
						player.sendMessage("Quest " + quest.id + " name set to: " + n + ".");
					} else {
						player.sendMessage("Invalid quest name.");
					}
				} else if (quest.name != null) {
					player.sendMessage("Quest " + quest.id + " name: " + quest.name);
				} else {
					player.sendMessage("Use /qadmin name [quest name] to set the quest name.");
				}
				
			// set type
			} else if (command[1].equalsIgnoreCase("type")) {
				if (command.length == 3) {
					if (quest.setType(command[2])) {
						player.sendMessage("Quest " + quest.id + " type set to: " + command[2] + ".");
					} else {
						player.sendMessage("Invalid quest type.");
					}
				} else if (command.length == 2 && quest.type != null) {
					player.sendMessage("Quest " + quest.id + " type: " + quest.type);
				} else {
					player.sendMessage("Use /qadmin type [type] to set the quest type.");
				}
			
			// desc
			} else if (command[1].equalsIgnoreCase("desc")) {
				if (command.length > 2) {
					if (command[2].equalsIgnoreCase("-delete") && command.length == 3) {
						quest.desc = "";
						player.sendMessage("Quest description cleared.");
					} else {
						String s = "";
						for (int i = 2; i < command.length; i++) {
							s = s + command[i] + " ";
						}
						quest.desc += s;
						player.sendMessage("Quest description appended.");
					}
				} else if (quest.desc != null) {
					player.sendMessage("Quest " + quest.id + " description: ");	
					String [] descLines = quest.desc.split("@");
					for (String s : descLines) {
						player.sendMessage(s);
					}
				} else {
					player.sendMessage("Use /qadmin desc [quest description] to append to the descriptin.");
					player.sendMessage("Use /qadmin desc -delete to start over.");
				}

			// start npc
			} else if (command[1].equalsIgnoreCase("startnpc")) {
				if (command.length == 3) {
					if (quest.setPickUp(command[2])) {
						player.sendMessage("Quest start npc set to " + command[2] + ".");
					} else {
						player.sendMessage("Invalid npc id.");
					}
				} else if (command.length == 2 && quest.pickUp != null) {
					player.sendMessage("Quest " + quest.id + " start npc: " + quest.pickUp);
				} else {
					player.sendMessage("Use /qadmin startnpc [npcid] to set the start npc.");
				}
				
			// end npc
			} else if (command[1].equalsIgnoreCase("endnpc")) {
				if (command.length == 3) {
					if (quest.setTurnIn(command[2])) {
						player.sendMessage("Quest end npc set to " + command[2]);
					} else {
						player.sendMessage("Invalid npc id.");
					}
				} else if (command.length == 2 && quest.turnIn != null) {
					player.sendMessage("Quest " + quest.id + " end npc: " + quest.turnIn);
				} else {
					player.sendMessage("Use /qadmin endnpc [npcid] to set the end npc.");
				}
			
			// prereq
			} else if (command[1].equalsIgnoreCase("prereq")) {
				if (command.length == 3) {
					if (command[2].equals("-delete")) {
						quest.prereq = null;
						player.sendMessage("Quest prerequisite removed.");
					} else if (quest.setPrereq(command[2])) {
						player.sendMessage("Quest prerequisite set to " + command[2] + ".");	
					} else {
						player.sendMessage("Invalid quest id.");
					}
				} else if (command.length == 2 && quest.prereq != null) {
					player.sendMessage("Quest " + quest.id + " prereq quest: " + quest.prereq);
				} else {
					player.sendMessage("Use /qadmin prereq [questid] to set the prereq quest.");
					player.sendMessage("Use /qadmin prereq -delete to remove the prereq quest.");
				}	
			
			// rankreq
			} else if (command[1].equalsIgnoreCase("rankreq")) {
				if (command.length == 3) {
					if (command[2].equals("-delete")) {
						quest.rankReq = null;
						player.sendMessage("Quest rank requirement removed.");
					} else if (quest.setRankReq(command[2])){
						player.sendMessage("Quest rank requirmenet set to " + command[2] + ".");
					} else {
						player.sendMessage("Invalid rank requirement.");
					}
				} else if (command.length == 2 && quest.rankReq != null) {
					player.sendMessage("Quest " + quest.id + " rank requirement: " + quest.rankReq);
				} else {
					player.sendMessage("Use /qadmin rankreq [rankname] to set the rank requirement.");
					player.sendMessage("Use /qadmin rankreq -delete to remove the rank requirement.");
				}

			// rankreward
			} else if (command[1].equalsIgnoreCase("rankreward")) {
				if (command.length == 3) {
					if (command[2].equals("-delete")) {
						quest.rankReward = null;
						player.sendMessage("Quest rank reward removed.");
					} else if (quest.setRankReward(command[2])){
						player.sendMessage("Quest rank reward set to " + command[2] + ".");
					} else {
						player.sendMessage("Invalid rank reward.");
					}
				} else if (command.length == 2 && quest.rankReward != null) {
					player.sendMessage("Quest " + quest.id + " rank reward: " + quest.rankReward);
				} else {
					player.sendMessage("Use /qadmin rankreward [rankname] to set the rank reward.");
					player.sendMessage("Use /qadmin rankreward -delete to remove the rank reward.");
				}

			// cost
			} else if (command[1].equalsIgnoreCase("cost")) {
				if (command.length == 3) {
					if (command[2].equals("-delete")) {
						quest.cost = 0;
						player.sendMessage("Quest cost removed.");
					} else if (quest.setCost(command[2])){
						player.sendMessage("Quest cost set to " + command[2] + ".");
					} else {
						player.sendMessage("Invalid cost.");
					}
				} else if (command.length == 2) {
					player.sendMessage("Quest " + quest.id + " cost: " + quest.cost);
				} else {
					player.sendMessage("Use /qadmin cost [rankname] to set the quest cost.");
					player.sendMessage("Use /qadmin cost -delete to remove the quest cost.");
				}
				
			// prize
			} else if (command[1].equalsIgnoreCase("prize")) {
				if (command.length == 3) {
					if (command[2].equals("-delete")) {
						quest.prize = 0;
						player.sendMessage("Quest prize removed.");
					} else if (quest.setPrize(command[2])){
						player.sendMessage("Quest prize set to " + command[2] + ".");
					} else {
						player.sendMessage("Invalid prize.");
					}
				} else if (command.length == 2) {
					player.sendMessage("Quest " + quest.id + " prize: " + quest.prize);
				} else {
					player.sendMessage("Use /qadmin prize [rankname] to set the quest prize.");
					player.sendMessage("Use /qadmin prize -delete to remove the quest prize.");
				}
				
			// items provided
			} else if (command[1].equalsIgnoreCase("itemsprovided")) {
				if (command.length > 2) {
					String s = "";
					for (int i = 2; i < command.length; i++) {
						s += command[i] + " ";
					}
					s = s.trim();
					if (s.indexOf(":") < 0) {
						s += ":";
						Inventory inv = player.getInventory();
						for (int i = 0; i <= 8; i++) {
							Item item = inv.getItemFromSlot(i);
							if (item != null && item.getItemId() > 0) {
								s += item.getItemId() + " " + item.getAmount() + ",";
							}
						}
						s = s.substring(0,s.length()-1);
					}
					if (quest.setItemsProvided(s)) {
						player.sendMessage("Quest items provided set to " + s + ".");
					} else {
						player.sendMessage("Invalid items provided string.");
					}
				} else if (command.length == 2 && quest.itemsProvidedStr != null) {
					player.sendMessage("Quest " + quest.id + " items provided: " + quest.itemsProvidedStr);
				} else {
					player.sendMessage("Use /qadmin itemsprovided [description string] to set items provided");
					player.sendMessage("The items in your equip bar will be used");
				}
			
			// rewards
			} else if (command[1].equalsIgnoreCase("rewards")) {
				if (command.length > 2) {
					String s = "";
					for (int i = 2; i < command.length; i++) {
						s += command[i] + " ";
					}
					s = s.trim();
					if (s.indexOf(":") < 0) {
						s += ":";
						Inventory inv = player.getInventory();
						for (int i = 0; i <= 8; i++) {
							Item item = inv.getItemFromSlot(i);
							if (item != null && item.getItemId() > 0) {
								s += item.getItemId() + " " + item.getAmount() + ",";
							}
						}
						s = s.substring(0,s.length()-1);
					}
					if (quest.setRewards(s)) {
						player.sendMessage("Quest rewards set to " + s);
					} else {
						player.sendMessage("Invalid rewards string.");
					}
				} else if (command.length == 2 && quest.itemsProvidedStr != null) {
					player.sendMessage("Quest " + quest.id + " rewards: " + quest.rewardsStr);
				} else {
					player.sendMessage("Use /qadmin rewards [description string] to set rewards");
					player.sendMessage("The items in your equip bar will be used");
				}
			
			// location
			} else if (command[1].equalsIgnoreCase("location")) {
				if (command.length == 3) {
					if (command[2].equalsIgnoreCase("-delete")) {
						quest.location = null;
						player.sendMessage("Quest " + quest.id + " location data reset.");
					} else if (command[2].equalsIgnoreCase("yrange")) {
						// TODO
					}
				} else if (command.length == 2 && quest.location != null) {
					player.sendMessage("Quest " + quest.id + " location: " + quest.location);
				} else {
					player.sendMessage("Right click on blocks to set a location polygon");
					player.sendMessage("Use /qadmin location -delete to remove location data");
				}
			
			// data
			} else if (command[1].equalsIgnoreCase("data")) {
				if (command.length > 2) {
					String s = "";
					for (int i = 2; i < command.length; i++) {
						s += command[i] + " ";
					}
					s = s.trim();
					if (quest.setData(s)) {
						player.sendMessage("Quest data set to " + s);
					} else {
						player.sendMessage("Invalid data.");
					}
				} else if (command.length == 1 && quest.data != null) {
					player.sendMessage("Quest " + quest.id + " data: " + quest.data);
				} else {
					player.sendMessage("Use /qadmin data [quest data] to set the quest data");
				}
			
			// completion txt
			} else if (command[1].equalsIgnoreCase("completetext")) {
				if (command.length > 2) {
					if (command[2].equalsIgnoreCase("-delete") && command.length == 3) {
						quest.completionText = "";
						player.sendMessage("Quest completion text cleared.");
					} else {
						String s = "";
						for (int i = 2; i < command.length; i++) {
							s = s + command[i] + " ";
						}
						if (quest.completionText == null) {
							quest.completionText = s;
						} else {
							quest.completionText += s;
						}
						player.sendMessage("Quest completion text appended.");
					}
				} else if (quest.completionText != null) {
					player.sendMessage("Quest " + quest.id + " completion text: ");	
					String [] descLines = quest.completionText.split("@");
					for (String s : descLines) {
						player.sendMessage(s);
					}
				} else {
					player.sendMessage("Use /qadmin completetext [completion text] to append.");
					player.sendMessage("Use /qadmin completetext -delete to start over.");
				}
				
			// save the quest
			} else if (command[1].equalsIgnoreCase("save")) {
				if (quest.save(player)) {
					Craftizens.newQuests.remove(player.getName());
					if (Craftizens.newQuests.size() == 0) {
						Craftizens.newQuests = null;
					}
					player.sendMessage("Quest saved.");
				} else {
					player.sendMessage("Quest failed to save.");
				}
			}
			
		} else {
			player.sendMessage("Use /qadmin new [questid] to start creating a new quest.");
			player.sendMessage("Use /qadmin load [questid] to modify an existing quest.");
			player.sendMessage("Use /qadmin list to list all quests.");
		}
	}
	
	public void onArmSwing(Player player) {
		if (Craftizens.INTERACT_ANYTHING || player.getItemInHand() == Craftizens.INTERACT_ITEM || player.getItemInHand() == Craftizens.INTERACT_ITEM_2) {
			if (Craftizens.DEBUG) log.info("Player " + player.getName() + " trying to check quest");
			for (Craftizen npc : Craftizens.npcs) {
				if (Craftizens.DEBUG) log.info("--checking npc " + npc.getName());
				if (isLookingAtAndInRange(player, npc.getX(), npc.getY(), npc.getZ(), Craftizens.INTERACT_RANGE)) {
					if (Craftizens.DEBUG) log.info("--npc " + npc.getName() + " is in range! Interacting...");
					npc.interact(player);
					break;
				}
			}
		} else if (Craftizens.activeQuests.containsKey(player.getName())) {
			for (Quest q : Craftizens.activeQuests.get(player.getName())) {
				q.onArmSwing(player);
			}
		}
	}
	
	public boolean onBlockDestroy(Player player, Block block) {
		if (Craftizens.activeQuests.containsKey(player.getName())) {
			for (Quest q : Craftizens.activeQuests.get(player.getName())) {
				q.onBlockDestroy(player, block);
			}
		}
		return false;
	}
	
	public boolean onBlockCreate(Player player, Block placed, Block clicked, int itemInHand) {
		if (itemInHand == Craftizens.QADMIN_BOUNDARY_MARKER && Craftizens.newQuests != null && Craftizens.newQuests.containsKey(player.getName())) {
			QuestInfo quest = Craftizens.newQuests.get(player.getName());
			String coord = clicked.getX() + "," + clicked.getZ();
			if (quest.location == null || quest.location.equals("")) {
				quest.location = coord;
			} else {
				quest.location += ":" + coord;
			}
			player.sendMessage("Coord " + coord + " added to quest " + quest.id + ".");
			
		} else if (Craftizens.activeQuests.containsKey(player.getName())) {
			for (Quest q : Craftizens.activeQuests.get(player.getName())) {
				q.onBlockCreate(player, placed, clicked, itemInHand);
			}
		}
		return false;
	}
	
	public void onPlayerMove(Player player, Location from, Location to) {
		if (Craftizens.activeQuests != null && Craftizens.activeQuests.containsKey(player.getName())) {
			for (Quest q : Craftizens.activeQuests.get(player.getName())) {
				q.onPlayerMove(player, from, to);
			}
		}
	}
	
	public static boolean isLookingAtAndInRange(Player player, double x, double y, double z, double range) {
		return inRange(player, x, y, z, range) && lookingAt(player, x, y, z);
	}
	
	public static boolean inRange(Player player, double x, double y, double z, double range) {
		return Math.pow(player.getX()-x,2) + Math.pow(player.getY()-y,2) + Math.pow(player.getZ()-z,2) <= range*range;
	}
	
	public static boolean lookingAt(Player player, double x, double y, double z) {
		double dx = Math.abs(player.getX() - x);
		double dz = Math.abs(player.getZ() - z);
		
		double angle = Math.toDegrees(Math.atan(dx/dz));
		
		if (x <= player.getX() && z <= player.getZ()) {
			angle = 180 - angle;
		} else if (x <= player.getX() && z >= player.getZ()) {
//			angle = angle;
		} else if (x >= player.getX() && z >= player.getZ()) {
			angle = 360 - angle;
		} else {
			angle = 180 + angle;
		}
		
		double pRot = player.getRotation() % 360;
		if (pRot < 0) pRot += 360D;
		
		if (Math.abs(pRot - angle) < Craftizens.INTERACT_ANGLE_VARIATION) {
			return true;
		} else {
			return false;
		}
	}
	
	public void onLogin(Player player) {
		loadActiveQuests(player);
	}
	
	public static void loadActiveQuests(Player player) {
		ArrayList<Quest> quests = new ArrayList<Quest>();
		
		HashMap<QuestInfo,String> questInfo = Craftizens.data.getActiveQuests(player);
		for (QuestInfo i : questInfo.keySet()) {
			Quest q = i.createQuest(player, false);
			q.loadProgress(questInfo.get(i));
			quests.add(q);
		}

		Craftizens.activeQuests.put(player.getName(), quests);	
	}
	
	public void onDisconnect(Player player) {
		// stop tracking player by npcs
		for (Craftizen npc : Craftizens.npcs) {
			npc.untrack(player);
		}
		
		// save active quests
		if (Craftizens.activeQuests.containsKey(player.getName())) {
			for (Quest q : Craftizens.activeQuests.get(player.getName())) {
				q.saveProgress();
			}
			Craftizens.activeQuests.remove(player.getName());
		}
	}
}
