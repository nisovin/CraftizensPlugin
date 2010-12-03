import net.minecraft.server.MinecraftServer;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.*;
import java.util.Random;
import java.awt.Polygon;

public class Craftizen extends NonPlayerCharacter {
	static final Logger log = Logger.getLogger("Minecraft");
	
	private static Random random = new Random();
	
	public String id;
	public String name;
	public int itemInHand;
	private double speed;
	private ArrayList<String> dialog;
	
	RouteType routeType;
	Polygon route;
	int [] routeX;
	int [] routeZ;
	
	private int ctr = 10;
	
	public Craftizen(String id, String name, double x, double y, double z, float rotation, float pitch, int itemInHand) {
		super(Craftizens.NPC_PREFIX + name + Craftizens.NPC_SUFFIX, x, y, z, rotation, pitch, itemInHand);
		
		this.id = id;
		this.name = name;
		this.itemInHand = itemInHand;
		speed = 1;
		
		
	}
	
	public static Craftizen getCraftizen(String npcid) {
		for (Craftizen c : Craftizens.npcs) {
			if (c.getId().equals(npcid)) {
				return c;
			}
		}
		return null;
	}
	
	public void interact(Player player) {
		if (Craftizens.DEBUG) log.info("NPC " + id + " interacting with " + player.getName() + "...");
		
		if (Craftizens.QUESTS_ENABLED) {
			// check for turn-ins
			if (Craftizens.DEBUG) log.info("--checking for turn-ins...");
			if (Craftizens.activeQuests.containsKey(player.getName())) {
				ArrayList<Quest> quests = Craftizens.activeQuests.get(player.getName());
				for (Quest q : quests) {
					if (q.getTurnInNpc().equals(id) && q.isComplete()) {
						q.complete();
						Craftizens.data.saveCompletedQuest(player, q);
						quests.remove(q);
						return;
					}
				}
			}
		
			// get quests
			if (Craftizens.DEBUG) log.info("--checking for available quests...");
			ArrayList<QuestInfo> quests = Craftizens.data.getAvailableQuests(this, player);
			if (quests != null && quests.size() > 0) {
				if (Craftizens.DEBUG) log.info("--found " + quests.size() + "quests");
				if (quests.size() == 1) {
					// only one quest - display it
					QuestInfo q = quests.get(0);
					q.show(player);
					Craftizens.pendingQuests.put(player.getName(), q);
				} else {
					// multiple quests - show list
					player.sendMessage(Craftizens.TEXT_COLOR + "Available quests:");
					for (int i = 0; i < quests.size(); i++) {
						QuestInfo q = quests.get(i);
						player.sendMessage(Craftizens.TEXT_COLOR + "   " + (i+1) + ": " + q.getName());
					}
					player.sendMessage(Craftizens.TEXT_COLOR + "To view a quest type: /quest view #");
					Craftizens.pendingQuests.put(player.getName(), quests);
				}
			
				return;			
			}
		}
		
		// get random statement
		String d = getRandomDialog();
		if (d != null) {
			String [] stmt = d.split("@");
			stmt[0] = name + " says, " + stmt[0];
			for (String s : stmt) {
				player.sendMessage(Craftizens.TEXT_COLOR + s);
			}
		}
		
	}
	
	public static void delete(String s) {		
		Craftizen found = null;
		for (Craftizen npc : Craftizens.npcs) {
			if (npc.getId().equals(s)) {
				found = npc;
			}
		}
		if (found != null) {
			found.delete();
			Craftizens.npcs.remove(found);
		}
	}
	
	public void tick(int interval) {
		// send spawns TODO: only send when necessary
		if (++ctr > 10) {
			broadcastPosition();
			ctr = 0;
		}
		//broadcastMovement(); // send movements
		
		// TODO: send carry block when necessary
		/*for (Object user : Craftians.players) {
			((ea)user).a.b(new fv(handler.a.c, 1));
		}*/
		
		// motion
		//handler.a.b(handler.a.l+((double)interval/1000D*speed),handler.a.m,handler.a.n,handler.a.r,handler.a.s);
	}
	
	public void setDialog(ArrayList<String> d) {
		this.dialog = d;
	}
	
	public String getRandomDialog() {
		if (dialog != null && dialog.size() > 0) {
			return dialog.get(random.nextInt(dialog.size()));
		} else {	
			return null;
		}
	}
	
	public void addDialog(String d) {
		if (dialog == null) {
			dialog = new ArrayList<String>();
		}
		dialog.add(d);
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public enum RouteType {
		NONE, WANDER, PATROL
	}
}
