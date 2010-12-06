import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.*;

public class Craftizens extends Plugin {
	static final Logger log = Logger.getLogger("Minecraft");
	
	public static boolean DEBUG = true;
	public static String NPC_PREFIX = "§e";
	public static String NPC_SUFFIX = " (NPC)";
	public static String TEXT_COLOR = Colors.Yellow;
	public static int INTERACT_ITEM = 340;
	public static int INTERACT_ITEM_2 = 340;
	public static int INTERACT_RANGE = 2;
	public static int INTERACT_ANGLE_VARIATION = 25;
	public static int QADMIN_BOUNDARY_MARKER = 340;
	public static boolean QUESTS_ENABLED = true;
	
	public static CraftizenDataSource data;
	public static HashSet<Craftizen> npcs;
	public static HashMap<String,Object> pendingQuests;
	public static HashMap<String,ArrayList<Quest>> activeQuests;
	public static HashMap<String,QuestInfo> newQuests;
	
	private static CraftizenTicker ticker;
	private static CraftizensListener listener;
	
	public void initialize() {
		listener = new CraftizensListener();
		etc.getLoader().addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.ARM_SWING, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_DESTROYED, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_CREATED, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.PLAYER_MOVE, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.LOGIN, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.DISCONNECT, listener, this, PluginListener.Priority.MEDIUM);
	}
	
	public void enable() {
		// load properties
		PropertiesFile props = new PropertiesFile("craftizens.properties");
		DEBUG = props.getBoolean("debug-mode",false);
		NPC_PREFIX = props.getString("npc-name-prefix","§e");
		NPC_SUFFIX = props.getString("npc-name-suffix"," (NPC)");
		TEXT_COLOR = "§" + props.getString("quest-text-color","e");
		INTERACT_ITEM = props.getInt("npc-interact-item",340);
		INTERACT_ITEM_2 = props.getInt("npc-interact-item-2",340);
		INTERACT_RANGE = props.getInt("npc-interact-range",2);
		INTERACT_ANGLE_VARIATION = props.getInt("npc-interact-angle-variation",25);
		QADMIN_BOUNDARY_MARKER = props.getInt("qadmin-boundary-marker",340);
		QUESTS_ENABLED = props.getBoolean("quests-enabled",true);
	
		data = new CraftizenSQLDataSource();
	
		Craftizen.getPlayerList();		
		npcs = data.loadCraftizens();
		
		ticker = new CraftizenTicker(2000);
		Thread t = new Thread(ticker);
		t.start();
		
		// load quests
		pendingQuests = new HashMap<String,Object>();
		activeQuests = new HashMap<String,ArrayList<Quest>>();
		for (Player p : etc.getServer().getPlayerList()) {
			CraftizensListener.loadActiveQuests(p);
		}
		
		log.info("Craftizens v0.7 loaded successfully!");
	}
	
	public void disable() {
		ticker.stop();
		ticker = null;
		
		for (Craftizen npc : npcs) {
			npc.delete();
		}
		npcs = null;
		
		// save quest progress
		for (String s : activeQuests.keySet()) {
			for (Quest q : activeQuests.get(s)) {
				if (q != null) {
					q.saveProgress();
				}
			}
		}
		
		pendingQuests = null;
		activeQuests = null;
		newQuests = null;
		
		data = null;
	}
	
}
