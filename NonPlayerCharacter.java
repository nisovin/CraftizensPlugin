import net.minecraft.server.MinecraftServer;
import java.util.List;

public abstract class NonPlayerCharacter {
    public static List<?> players;
    
    private es user;
    private gv handler; 
    
    public NonPlayerCharacter(String name, double x, double y, double z, float rotation, float pitch, int itemInHand) {
        if (players == null) getPlayerList();
    
        MinecraftServer s = etc.getServer().getMCServer();
        
        user = new es(s, s.e, name, new ju(s.e));
        teleportTo(x,y,z,rotation,pitch);
        if (itemInHand > 0) {
            setItemInHand(itemInHand);
        }
    
        handler = new gv(user, 512,  1 , true );
    }
    
    public void delete() {
        for (Object player : players) {
            ((es)player).a.b(new dh(handler.a.g));
        }
    }
    
    public void untrack(Player player) {
    	if (handler.q.contains(player.getUser())) {
    		handler.q.remove(player.getUser());
    	}
    }
    
    public void broadcastPosition() {
        handler.b(players); 
    }
    
    public void broadcastMovement() {
        handler.a(players);
    }
    
    public String getName() {
        return user.at;
    }
    
    public void setName(String name) {
        user.at = name;
    }
    
    public double getX() {
        return user.m;
    }
    
    public void setX(double x) {
        user.m = x;
    }
    
    public double getY() {
        return user.n;
    }
    
    public void setY(double y) {
        user.n = y;
    }
    
    public double getZ() {
        return user.o;
    }
    
    public void setZ(double z) {
        user.o = z;
    }
    
    public float getRotation() {
        return user.v;
    }
    
    public void setRotation(float rot) {
        user.v = rot;
    }
    
    public float getPitch() {
        return user.w;
    }
    
    public void setPitch(float pitch) {
        user.w = pitch;
    }
    
    public int getItemInHand() {
        return user.am.a[0].c;
    }
    
    public void setItemInHand(int type) {
        user.am.a[0] = new hm(type);
    }
    
    public void teleportTo(double x, double y, double z, float rotation, float pitch) {
        user.b(x,y,z,rotation,pitch);
    }
    
    public static void getPlayerList() {
        players = etc.getServer().getMCServer().f.b; 
    }
}
