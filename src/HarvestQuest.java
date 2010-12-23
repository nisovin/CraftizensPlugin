
import java.util.ArrayList;

public class HarvestQuest extends Quest {

	ArrayList<Integer> types;
	String [] names;
	int [] quantities;
	int [] progress;

	public HarvestQuest(QuestInfo info, Player player, boolean newQuest) {
		super(info, player, newQuest);
		
		// get harvest info data: format: item name,id,quantity:item name,id,quantity,etc
		String [] data = info.getData().split(":");
		types = new ArrayList<Integer>();
		names = new String [data.length];
		quantities = new int [data.length];
		progress = new int [data.length];
		for (int i = 0; i < data.length; i++) {
			String [] item = data[i].split(",");
			types.add(Integer.parseInt(item[1]));
			names[i] = item[0];
			quantities[i] = Integer.parseInt(item[2]);
			progress[i] = 0;
		}
		
	}
	
	public boolean isComplete() {
		for (int i = 0; i < types.size(); i++) {
			if (progress[i] < quantities[i]) {
				return false;
			} else if (countItems(types.get(i)) < quantities[i]) {
				return false;
			}
		}
		return true;
	}
	
	public void complete() {
		Inventory inv = player.getInventory();
		for (int i = 0; i < types.size(); i++) {
			inv.removeItem(types.get(i), quantities[i]);
		}
		inv.update();
		super.complete();
	}
	
	public String getProgress() {
		String prog = "";
		if (isComplete()) {
			prog += "Complete!";
		} else if (types.size() == 1) {
			int amt = countItems(types.get(0));
			if (progress[0] < amt) amt = progress[0];
			if (amt > quantities[0]) amt = quantities[0];
			prog += amt + "/" + quantities[0] + " " + names[0];
		} else {
			for (int i = 0; i < progress.length; i++) {
				int amt = countItems(types.get(i));
				if (progress[i] < amt) amt = progress[i];
				if (amt > quantities[i]) amt = quantities[i];
				prog += "@" + progress[i] + "/" + quantities[i] + " " + names[i];
			}
		}
		return prog;
	}
	
	public int countItems(int itemId) {
		Inventory inv = player.getInventory();
		int amt = 0;
		for (int i = 0; i < inv.getContents().length; i++) {
			Item item = inv.getItemFromSlot(i);
			if (item != null && item.getItemId() == itemId) {
				amt += item.getAmount();
			}
		}
		return amt;
	}
	
	public void loadProgress(String s) {
		if (s != null) {
			String [] prog = s.split(",");
			for (int i = 0; i < prog.length; i++) {
				progress[i] = Integer.parseInt(prog[i]);
			}
		} else {
			saveProgress();
		}
	}
	
	public void saveProgress() {
		String s = "";
		for (int i = 0; i < progress.length; i++) {
			s += progress[i] + ",";
		}
		s = s.substring(0,s.length()-1);
		Craftizens.data.saveQuestProgress(player, this, s);
	}
	
	public boolean onBlockDestroy(Player player, Block block) {
		if (types.contains(block.getType()) && block.getStatus() == 3 && (boundary == null || boundary.contains(block.getX(),block.getZ()))) {
			progress[types.indexOf(block.getType())] += 1;
		}
		return false;
	}

}
