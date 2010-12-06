import java.util.HashMap;

public class QuestInfo {

	public String id;
	public String type;
	public String name;
	public String desc;
	
	public String pickUp;
	public String turnIn;
	public String prereq;
	
	public String rankReq;
	public String rankReward;
	
	public String itemsProvidedStr;
	public HashMap<Integer,Integer> itemsProvided = new HashMap<Integer, Integer>();
	public String rewardsStr;
	public HashMap<Integer,Integer> rewards = new HashMap<Integer, Integer>();
	
	public String location;
	public String data;
	public String completionText;

	public boolean allowCompass;
	
	public QuestInfo(String id) {
		this.id = id;
		this.desc = "";
	}
	
	public QuestInfo(String id, String type, String name, String desc, String pickUp, String turnIn, String prereq, String itemsProvided, String rewards, String location, String data, String completionText, String rankReq, String rankReward) {
		// get normal data
		this.id = id;
		this.type = type;
		this.name = name;
		this.desc = desc;
		
		this.pickUp = pickUp;
		this.turnIn = turnIn;
		this.prereq = prereq;
		
		// set up items provided
		if (itemsProvided != null && !itemsProvided.equals("")) {
			// format: items desc string:id quantity,id quantity,id quantity,etc
			this.itemsProvidedStr = itemsProvided;
			String itemdata [] = itemsProvided.split(":");
			String [] items = itemdata[1].split(",");
			this.itemsProvided = new HashMap<Integer,Integer>();
			for (String i : items) {
				String [] item = i.split(" ");
				this.itemsProvided.put(Integer.parseInt(item[0]),Integer.parseInt(item[1]));
			}
			
		} else {
			this.itemsProvidedStr = "";
			this.itemsProvided = null;
		}
		
		// set up rewards
		if (rewards != null && !rewards.equals("")) {
			// format: items desc string:id quantity,id quantity,id quantity,etc
			this.rewardsStr = rewards;
			String itemdata [] = rewards.split(":");
			String [] items = itemdata[1].split(",");
			this.rewards = new HashMap<Integer,Integer>();
			for (String i : items) {
				String [] item = i.split(" ");
				this.rewards.put(Integer.parseInt(item[0]),Integer.parseInt(item[1]));
			}
		} else {
			this.rewardsStr = "";
			this.rewards = null;
		}
		
		this.location = location;
		this.data = data;
		this.completionText = completionText;
		this.rankReq = rankReq;
		this.rankReward = rankReward;
	}
	
	public void show(Player player) {
		player.sendMessage(" ");
		player.sendMessage(Craftizens.TEXT_COLOR + "Quest: " + name);
		player.sendMessage(" ");
		String [] descLines = desc.split("@");
		for (String s : descLines) {
			String temp = s;
			while (temp.length() > 60) {
				int lastSpace = temp.substring(0,60).lastIndexOf(' ');
				player.sendMessage(Craftizens.TEXT_COLOR + temp.substring(0,lastSpace));
				temp = temp.substring(lastSpace+1);
			}
			player.sendMessage(Craftizens.TEXT_COLOR + temp);
		}
		if (!itemsProvidedStr.equals("")) {
			player.sendMessage(Craftizens.TEXT_COLOR + "   Provided: " + itemsProvidedStr.split(":")[0]);
		}
		if (!rewardsStr.equals("")) {
			player.sendMessage(Craftizens.TEXT_COLOR + "   Reward: " + rewardsStr.split(":")[0]);
		}
		player.sendMessage(Craftizens.TEXT_COLOR + "Type '/quest accept' to accept this quest.");
	}
	
	public Quest createQuest(Player player, boolean newQuest) {
		Quest q = null;
		
		if (type.equals("harvest")) {
			q = new HarvestQuest(this, player, newQuest);
		} else if (type.equals("build")) {
			q = new BuildQuest(this, player, newQuest);
		} else if (type.equals("gather")) {
			q = new GatherQuest(this, player, newQuest);
		} else if (type.equals("findloc")) {
			q = new FindLocQuest(this, player, newQuest);
		} else if (type.equals("fetchblock")) {
			q = new FetchBlockQuest(this, player, newQuest);
		}
		
		return q;
	}
	
	public boolean setId(String id) {
		if (id.matches("^[a-zA-Z0-9]+$")) {
			this.id = id;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean setType(String t) {
		if (!t.equals("harvest") && !t.equals("build") && !t.equals("gather") && !t.equals("findloc") && !t.equals("fetchblock")) {
			return false;
		} else {
			type = t;
			return true;
		}
	}
	
	public boolean setName(String n) {
		if (n.matches("^[a-zA-Z0-9 \\-]+$")) {
			this.name = n;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean setPickUp(String npc) {
		if (npc.matches("^[a-zA-Z0-9]+$")) {
			this.pickUp = npc;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean setTurnIn(String npc) {
		if (npc.matches("^[a-zA-Z0-9]+$")) {
			this.turnIn = npc;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean setPrereq(String q) {
		if (q.matches("^[a-zA-Z0-9]+$")) {
			this.prereq = q;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @param rankReq the rankReq to set
	 */
	public boolean setRankReq(String rankReq) {
		if (rankReq.matches("^[a-zA-Z0-9]+$")) {
			this.rankReq = rankReq;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @param rankReward the rankReward to set
	 */
	public boolean setRankReward(String rankReward) {
		if (rankReward.matches("^[a-zA-Z0-9]+$")) {
			etc.getInstance();
			Group g = etc.getDataSource().getGroup(rankReward);
			if (g != null) {
				this.rankReward = rankReward;
				return true;
			}
			return false;
		} else {
			return false;
		}
	}

	public boolean setItemsProvided(String i) {
		if (i.matches("[a-zA-Z0-9\\- ]+:([0-9]+ [0-9]+,)*[0-9]+ [0-9]+")) {
			this.itemsProvidedStr = i;
			return true;
		} else {
			return false;
		}		
	}
	
	public boolean setRewards(String i) {
		if (i.matches("[a-zA-Z0-9\\- ]+:([0-9]+ [0-9]+,)*[0-9]+ [0-9]+")) {
			this.rewardsStr = i;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean setData(String d) {
		if (type == null || type.equals("")) {
			return false;
		} else {
			String regex;
			if (type.equals("gather") || type.equals("harvest") || type.equals("build")) {
				regex = "([a-zA-Z0-9 ]+,[0-9]+,[0-9]+:)*[a-zA-Z0-9 ]+,[0-9]+,[0-9]+";
			} else if (type.equals("findloc")) {
				regex = ".+";
			} else if (type.equals("fetchblock")) {
				regex = "([0-9]+ [0-9]+,[0-9]+,[0-9]+:)*[0-9]+ [0-9]+,[0-9]+,[0-9]+";
			} else {
				return false;
			}
			if (d.matches(regex)) {
				data = d;
				return true;
			} else {
				return false;
			}
		}
	}
	
	public boolean save(Player p) {
		if (id == null || id.equals("")) {
			p.sendMessage("Missing id");
			return false;
		} else if (name == null || name.equals("")) {
			p.sendMessage("Missing name");
			return false;
		} else if (type == null || type.equals("")) {
			p.sendMessage("Missing type");
			return false;
		} else if (desc == null || desc.equals("")) {
			p.sendMessage("Missing desc");
			return false;
		} else if (pickUp == null || pickUp.equals("")) {
			p.sendMessage("Missing start npc");
			return false;
		} else if (turnIn == null || turnIn.equals("")) {
			p.sendMessage("Missing end npc");
			return false;
		} else {
			Craftizens.data.saveQuest(this);
			return true;
		}		
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public String getItemsProvidedStr() {
		return itemsProvidedStr;
	}
	
	public String getRewardsStr() {
		return rewardsStr;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String getData() {
		return data;
	}
}
