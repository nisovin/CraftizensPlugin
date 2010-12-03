
import java.util.ArrayList;

public class FetchBlockQuest extends Quest {
	
	final int SPAWN_RANGE = 30;

	ArrayList<String> coords;
	int [] types;
	boolean [] complete;
	int [] x;
	int [] y;
	int [] z;
	
	boolean firstStep;

	public FetchBlockQuest(QuestInfo info, Player player, boolean newQuest) {
		super(info, player, newQuest);
		

		String [] data = info.data.split(":");
		
		coords = new ArrayList<String>();
		types = new int [data.length];
		complete = new boolean [data.length];
		x = new int [data.length];
		y = new int [data.length];
		z = new int [data.length];
		
		for (int i = 0; i < data.length; i++) {
			String [] block = data[i].split(" ");
			types[i] = Integer.parseInt(block[0]);
			coords.add(block[1]);
			complete[i] = false;
			String [] xyz = block[1].split(",");
			x[i] = Integer.parseInt(xyz[0]);
			y[i] = Integer.parseInt(xyz[1]);
			z[i] = Integer.parseInt(xyz[2]);
		}
		
		firstStep = true;
		
	}
	
	public boolean isComplete() {
		Inventory inv = player.getInventory();
		for (int i = 0; i < complete.length; i++) {
			if (!complete[i] || !inv.hasItem(types[i],1,999)) {
				return false;
			}
		}
		return true;
	}
	
	public void complete() {
		Inventory inv = player.getInventory();
		for (int i = 0; i < types.length; i++) {
			inv.removeItem(new Item(types[i], 1));
		}
		inv.updateInventory();
		super.complete();
	}
	
	public String getProgress() {
		if (isComplete()) {
			return "Complete!";
		} else {
			return "not complete.";
		}
	}
	
	public void loadProgress(String s) {
		if (s != null) {
			String [] prog = s.split(",");
			for (int i = 0; i < prog.length; i++) {
				if (prog[i].equals("0")) {
					complete[i] = false;
				} else {
					complete[i] = true;
				}
			}
		} else {
			saveProgress();
		}
	}
	
	public void saveProgress() {
		String s = "";
		for (int i = 0; i < complete.length; i++) {
			if (complete[i]) {
				s += "1,";
			} else {
				s += "0,";
			}
		}
		s = s.substring(0,s.length()-1);
		Craftizens.data.saveQuestProgress(player, this, s);
	}
	
	public void onArmSwing(Player player) {
		System.out.println(player.getName());
		for (int i = 0; i < coords.size(); i++) {
			if (!complete[i] && CraftizensListener.isLookingAtAndInRange(player, x[i]+.5, y[i], z[i]+.5, 3)) {
				sendFakeBlockPacket(player, x[i], y[i], z[i], 0);
				etc.getServer().dropItem(x[i]+.5, y[i], z[i]+.5, types[i]);
				complete[i] = true;
			}
		}
	}
	
	public void onPlayerMove(Player player, Location from, Location to) {
		for (int i = 0; i < coords.size(); i++) {
			if (!complete[i] && (firstStep || Math.pow(from.x-x[i],2) + Math.pow(from.z-z[i],2) > SPAWN_RANGE*SPAWN_RANGE) && (Math.pow(to.x-x[i],2) + Math.pow(to.z-z[i],2) <= SPAWN_RANGE*SPAWN_RANGE)) {
				sendFakeBlockPacket(player, x[i], y[i], z[i], types[i]);
			}
		}
		if (firstStep) firstStep = false;
	}
	
	public void sendFakeBlockPacket(Player player, int x, int y, int z, int type) {
		fm packet = new fm();
		packet.a = x;
		packet.b = y;
		packet.c = z;
		packet.d = (byte)type;
		packet.e = (byte)0;
		player.getUser().a.b(packet);
	}

}
