
import java.util.ArrayList;

public class GatherQuest extends Quest {

	ArrayList<Integer> types;
	String [] names;
	int [] quantities;

	public GatherQuest(QuestInfo info, Player player, boolean newQuest) {
		super(info, player, newQuest);
		
		// get harvest info data: format: item name,id,quantity:item name,id,quantity,etc
		String [] data = info.getData().split(":");
		types = new ArrayList<Integer>();
		names = new String [data.length];
		quantities = new int [data.length];
		for (int i = 0; i < data.length; i++) {
			String [] item = data[i].split(",");
			types.add(Integer.parseInt(item[1]));
			names[i] = item[0];
			quantities[i] = Integer.parseInt(item[2]);
		}
		
	}
	
	public boolean isComplete() {
		for (int i = 0; i < types.size(); i++) {
			if (countItems(types.get(i)) < quantities[i]) {
				return false;
			}
		}
		return true;
	}
	
	public int countItems(int itemId) {
		Inventory inv = player.getInventory();
		int amt = 0;
		for (int i = 0; i < inv.getArray().length; i++) {
			Item item = inv.getItemFromSlot(i);
			if (item != null && item.getItemId() == itemId) {
				amt += item.getAmount();
			}
		}
		return amt;
	}
	
	public void complete() {
		Inventory inv = player.getInventory();
		for (int i = 0; i < types.size(); i++) {
			inv.removeItem(new Item(types.get(i), quantities[i]));
		}
		inv.updateInventory();
		super.complete();
	}
	
	public String getProgress() {
		String prog = "";
		if (isComplete()) {
			prog += "Complete!";
		} else if (types.size() == 1) {
			int amt = countItems(types.get(0));
			prog += (amt>quantities[0]?quantities[0]:amt) + "/" + quantities[0] + " " + names[0];
		} else {
			for (int i = 0; i < types.size(); i++) {
				int amt = countItems(types.get(i));
				prog += "@" + (amt>quantities[i]?quantities[i]:amt) + "/" + quantities[i] + " " + names[i];
			}
		}
		return prog;
	}
	
	public void loadProgress(String s) {
		// no progress to load
	}
	
	public void saveProgress() {
		// no need to save progress
	}

}
