
import java.util.ArrayList;

public class BuildQuest extends Quest {

	ArrayList<Integer> types;
	String [] names;
	int [] quantities;
	int [] progress;

	public BuildQuest(QuestInfo info, Player player, boolean newQuest) {
		super(info, player, newQuest);
		
		// get build info data: format: item name,id,quantity:item name,id,quantity,etc
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
			} 
		}
		return true;
	}
	
	public String getProgress() {
		String prog = "";
		if (isComplete()) {
			prog += "Complete!";
		} else if (types.size() == 1) {
			prog += (progress[0]<quantities[0]?progress[0]:quantities[0]) + "/" + quantities[0] + " " + names[0];
		} else {
			for (int i = 0; i < progress.length; i++) {
				prog += "@" + (progress[i]<quantities[i]?progress[i]:quantities[i]) + "/" + quantities[i] + " " + names[i];
			}
		}
		return prog;
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
	
	boolean onBlockCreate(Player player, Block placed, Block clicked, int itemInHand) {
		if (types.contains(placed.getType()) && clicked.getType() == 2 && (boundary == null || boundary.contains(placed.getX(),placed.getZ()))) {
			progress[types.indexOf(placed.getType())] += 1;
		}
		return false;
	}

}
