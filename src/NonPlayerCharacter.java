import net.minecraft.server.MinecraftServer;
import java.util.List;

public abstract class NonPlayerCharacter {
    public static List<?> players;
    
    // XXX: VARIABLE TYPE ON AN UPDATE
    private fi user;
    // XXX: VARIABLE TYPE ON AN UPDATE
    private hr handler; 
    
    public NonPlayerCharacter(String name, double x, double y, double z, float rotation, float pitch, int itemInHand) {
        if (players == null) getPlayerList();
    
        MinecraftServer s = etc.getServer().getMCServer();

        // XXX: VARIABLE TYPE ON AN UPDATE
        user = new fi(s, s.e, name, new kw(s.e));
        teleportTo(x,y,z,rotation,pitch);
        if (itemInHand > 0) {
            setItemInHand(itemInHand);
        } else {
            setItemInHand(0);
        }
    
        // XXX: VARIABLE TYPE ON AN UPDATE
        handler = new hr(user, 512,  1 , true );
    }
    
    public void delete() {
        for (Object player : players) {
            // XXX: VARIABLE TYPE ON AN UPDATE
            ((fi)player).a.b(new dv(handler.a.g));
        }
    }
    
    public void untrack(Player player) {
        // XXX: VARIABLE TYPE ON AN UPDATE
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
        // XXX: VARIABLE TYPE ON AN UPDATE
        return user.aw;
    }
    
    public void setName(String name) {
        // XXX: VARIABLE TYPE ON AN UPDATE
        user.aw = name;
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
        // XXX: VARIABLE TYPE ON AN UPDATE
    	if (user.an.a[0] == null) return 0;
        return user.an.a[0].c;
    }
    
    public void setItemInHand(int type) {
        // XXX: VARIABLE TYPE ON AN UPDATE
        user.an.a[0] = new il(type);
    }
    
    public void teleportTo(double x, double y, double z, float rotation, float pitch) {
        user.b(x,y,z,rotation,pitch);
    }
    
    public static void getPlayerList() {
        players = etc.getServer().getMCServer().f.b; 
    }
}
