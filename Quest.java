import java.awt.Polygon;

public abstract class Quest {

	protected QuestInfo info;
	protected Player player;
	protected Polygon boundary;
	
	public Quest(QuestInfo info, Player player, boolean newQuest) {
		this.info = info;
		this.player = player;
		
		// get boundary
		if (info.location != null && !info.location.equals("")) {
			String [] points = info.location.split(":");
			int [] x = new int [points.length];
			int [] z = new int [points.length];
			for (int i = 0; i < points.length; i++) {
				String [] coords = points[i].split(",");
				x[i] = Integer.parseInt(coords[0]);
				z[i] = Integer.parseInt(coords[1]);
			}
			boundary = new Polygon(x,z,points.length);
		}
		
		if (newQuest) {
			giveItemsProvided();
		}
	}
	
	private void giveItemsProvided() {
		if (info.itemsProvided != null) {
			for (Integer i : info.itemsProvided.keySet()) {
				player.giveItem(i, info.itemsProvided.get(i));
			}
		}
	}
	
	protected void giveRewards() {
		if (info.rewards != null) {
			for (Integer i : info.rewards.keySet()) {
				player.giveItem(i, info.rewards.get(i));
			}
		}
	}
	
	protected void complete() {
		giveRewards();
		String [] text;
		if (info.completionText != null && !info.completionText.equals("")) {
			text = info.completionText.split("@");
		} else {
			text = new String [] {"Quest completed!"};
		}
		for (String s : text) {
			String temp = s;			
			while (temp.length() > 60) {
				int lastSpace = temp.substring(0,60).lastIndexOf(' ');
				player.sendMessage(Craftizens.TEXT_COLOR + temp.substring(0,lastSpace));
				temp = temp.substring(lastSpace+1);
			}
			player.sendMessage(Craftizens.TEXT_COLOR + temp);
		}
	}
	
	public boolean compass() {
		if (boundary != null) {
			int [] x = boundary.xpoints;
			int [] y = boundary.ypoints;
			int i, j;
			int n = x.length;
			
			// first calculate area
			double area = 0;			
			for (i = 0; i < n; i++) {
				j = (i+1) % n;
				area += ((x[i]*y[j]) - (x[j]*y[i]));
			}
			area /= 2;
			
			// get cx and cy
			int cx = 0, cy = 0;
			for (i = 0; i < n; i++) {
				j = (i+1) % n;
				cx += ((x[i]+x[j]) * (x[i]*y[j] - x[j]*y[i]));
				cy += ((y[i]+y[j]) * (x[i]*y[j] - x[j]*y[i]));
			}
			cx /= (6*area);
			cy /= (6*area);
			
			player.getUser().a.b(new co((int)cx, (int)player.getY(), (int)cy));
			
			return true;
		} else { 
			return false;
		}
	}
	
	abstract boolean isComplete();
	
	abstract String getProgress();
	
	abstract void loadProgress(String s);
	
	abstract void saveProgress();
	
	void show(Player player) {
		info.show(player);
	}
	
	boolean onBlockCreate(Player player, Block placed, Block clicked, int itemInHand) {
		return false;
	}
	
	boolean onBlockDestroy(Player player, Block block) {
		return false;
	}
	
	void onPlayerMove(Player player, Location from, Location to) {
		return;
	}
	
	void onArmSwing(Player player) {
		return;
	}
	
	String getId() {
		return info.getId();
	}
	
	String getName() {
		return info.getName();
	}
	
	String getTurnInNpc() {
		return info.turnIn;
	}

}
