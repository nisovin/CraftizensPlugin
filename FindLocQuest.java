public class FindLocQuest extends Quest {
	
	private boolean complete;
	
	public FindLocQuest(QuestInfo info, Player player, boolean newQuest) {
		super(info, player, newQuest);
		
		complete = false;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public String getProgress() {
		if (complete) {
			return "Complete!";
		} else {
			return "not complete.";
		}
	}
	
	public void loadProgress(String s) {
		if (s == null || s.equals("0")) {
			complete = false;
		} else {
			complete = true;
		}
	}
	
	public void saveProgress() {
		Craftizens.data.saveQuestProgress(player, this, complete?"1":"0");
	}
	
	public void onPlayerMove(Player player, Location from, Location to) {
		if (!complete && boundary.contains(to.x,to.z)) {
			complete = true;
			player.sendMessage(Craftizens.TEXT_COLOR + "You have found " + info.getData() + ".");
		}
	}
	
}
