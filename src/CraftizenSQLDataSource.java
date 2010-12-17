import java.sql.*;
import java.util.logging.*;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;

public class CraftizenSQLDataSource extends CraftizenDataSource {
	static final Logger log = Logger.getLogger("Minecraft");
    static protected Connection connection = null;

    static public Connection getSQLConnection() throws SQLException
    {
        if (connection == null) {
            if (Craftizens.DATA_SOURCE_DRIVER_NAME.isEmpty()) {
                connection = etc.getSQLConnection();
            } else {
                try {
                    Class.forName(Craftizens.DATA_SOURCE_DRIVER_NAME);
                } catch (ClassNotFoundException ex) {
                    log.log(Level.SEVERE, null, ex);
                    return null;
                }
                connection = DriverManager.getConnection(Craftizens.DATA_SOURCE_CONNECTION_URL, Craftizens.DATA_SOURCE_USERNAME, Craftizens.DATA_SOURCE_PASSWORD);
            }
        } else {
            if (connection.isClosed()) {
                connection = null;
                log.info("[" + Craftizens.NAME + "] SQL-Connection got closed; Reconnecting!");
                return getSQLConnection();
            } else if (!connection.isValid(0)) {
                connection = null;
                log.info("[" + Craftizens.NAME + "] SQL-Connection got invalid; Reconnecting!");
                return getSQLConnection();
            }
        }
        return connection;
    }

	public void sample() {
		synchronized (dbLock) {		
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				query = conn.prepareStatement("SELECT * FROM");
				results = query.executeQuery();
				while (results.next()) {
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,"DB Error",e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
		}	
	}

	public HashSet<Craftizen> loadCraftizens() {
		synchronized (craftizenLock) {
			HashSet<Craftizen> npcs = new HashSet<Craftizen>();
		
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				query = conn.prepareStatement("SELECT * FROM craftizens");
				results = query.executeQuery();
				while (results.next()) {
					Craftizen c = new Craftizen(
							results.getString("npc_id"),
							results.getString("npc_name"),
							results.getDouble("posx"),
							results.getDouble("posy"),
							results.getDouble("posz"),
							results.getFloat("rotation"),
							results.getFloat("pitch"),
							results.getInt("item_in_hand")
						);
					npcs.add(c);
				}
				results.close();
				query.close();
				query = conn.prepareStatement("SELECT * FROM craftizens_dialog WHERE npc_id = ?");
				for (Craftizen c : npcs) {
					ArrayList<String> dialog = new ArrayList<String>();
					query.setString(1,c.getId());
					results = query.executeQuery();
					while (results.next()) {
						dialog.add(results.getString("dialog_text"));
					}
					results.close();
					c.setDialog(dialog);
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,"SQL error loading craftizen list",e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
			
			return npcs;
		}
	}
	
	public void saveCraftizen(Craftizen c) {
		synchronized (craftizenLock) {	
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				
				// check if npc exists already
				boolean exists = false;
				query = conn.prepareStatement("SELECT * FROM craftizens WHERE npc_id = ?");
				query.setString(1, c.getId());
				results = query.executeQuery();
				if (results.getFetchSize() == 1) {
					exists = true;
				}
				results.close();
				query.close();
				
				if (exists) {
					// update
					query = conn.prepareStatement("UPDATE craftizens SET npc_name = ?, posx = ?, posy = ?, posz = ?, rotation = ?, pitch = ?, item_in_hand = ? WHERE npc_id = ?");
					query.setString(1, c.getName());
					query.setDouble(2, c.getX());
					query.setDouble(3, c.getY());
					query.setDouble(4, c.getZ());
					query.setFloat(5, c.getRotation());
					query.setFloat(6, c.getPitch());
					query.setInt(7, c.getItemInHand());
					query.setString(8, c.getId());
					query.executeUpdate();
				} else {
					// insert
					query = conn.prepareStatement("INSERT INTO craftizens (npc_id, npc_name, posx, posy, posz, rotation, pitch, item_in_hand) VALUES (?,?,?,?,?,?,?,?)");
					query.setString(1, c.getId());
					query.setString(2, c.getName());
					query.setDouble(3, c.getX());
					query.setDouble(4, c.getY());
					query.setDouble(5, c.getZ());
					query.setFloat(6, c.getRotation());
					query.setFloat(7, c.getPitch());
					query.setInt(8, c.getItemInHand());
					query.executeUpdate();
				}
				
			} catch (SQLException e) {
				log.log(Level.SEVERE,"SQL error saving craftizen "+c.getId(),e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
		}	
	}
	
	public void addCraftizenDialog(String npcid, String dialogid, String dialog) {
		synchronized (craftizenLock) {		
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				query = conn.prepareStatement("INSERT INTO craftizens_dialog (npc_id, dialog_id, dialog_text) VALUES (?,?,?)");
				query.setString(1,npcid);
				query.setString(2,dialogid);
				query.setString(3,dialog);
				query.executeUpdate();
			} catch (SQLException e) {
				log.log(Level.SEVERE,"SQL error adding dialog for craftizen "+npcid,e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
		}		
	}
	
	public void deleteCraftizen(String id) {
		synchronized (craftizenLock) {		
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				query = conn.prepareStatement("DELETE FROM craftizens WHERE npc_id = ?");
				query.setString(1,id);
				query.executeUpdate();
			} catch (SQLException e) {
				log.log(Level.SEVERE,"SQL error deleting craftizen "+id,e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
		}	
	}
	
	public ArrayList<String> getQuestList() {
		synchronized (questLock) {
			ArrayList<String> quests = null;
			
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				query = conn.prepareStatement("SELECT id FROM quests ORDER BY id");
				results = query.executeQuery();
				while (results.next()) {
					if (quests == null) quests = new ArrayList<String>();
					quests.add(results.getString("id"));
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,"DB Error",e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
			
			return quests;
		}		
		
	}
	
	public QuestInfo loadQuestInfo(String id) {
		synchronized (questLock) {
			QuestInfo quest = null;
			
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				query = conn.prepareStatement("SELECT * FROM quests WHERE id = ?");
				query.setString(1,id);
				results = query.executeQuery();
				if (results.next()) {
					quest = new QuestInfo(
                        results.getString("id"),
                        results.getString("quest_type"),
                        results.getString("quest_name"),
                        results.getString("quest_desc"),
                        results.getString("start_npc"),
                        results.getString("end_npc"),
                        results.getString("prereq"),
                        results.getString("items_provided"),
                        results.getString("rewards"),
                        results.getString("location"),
                        results.getString("data"),
                        results.getString("completion_text"),
                        results.getString("rankreq"),
                        results.getString("rankreward"),
                        results.getString("cost"),
                        results.getString("prize")
                    );
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,"DB Error loading quest info "+id,e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
			
			return quest;
		}		
	}
	
	public ArrayList<QuestInfo> getAvailableQuests(Craftizen c, Player p) {
		synchronized (questLock) {
			ArrayList<QuestInfo> quests = new ArrayList<QuestInfo>();
			
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				query = conn.prepareStatement(
						"SELECT q.* " +
						"FROM craftizens AS c " +
						"JOIN quests AS q ON q.start_npc = c.npc_id " +
						"LEFT JOIN quests_completed AS qc ON qc.player_name = ? AND qc.quest_id = q.prereq " +
						"LEFT JOIN quests_completed AS qc2 ON qc2.player_name = ? AND qc2.quest_id = q.id " +
						"LEFT JOIN quests_active AS qa ON qa.player_name = ? AND qa.quest_id = q.id " +
						"WHERE c.npc_id = ? AND (q.prereq IS NULL OR qc.date_completed IS NOT NULL) " +
						"AND qc2.quest_id IS NULL and qa.quest_id IS NULL " +
						"AND (q.rankreq IS NULL OR q.rankreq = ?) "
					);
				query.setString(1, p.getName().toLowerCase());
				query.setString(2, p.getName().toLowerCase());
				query.setString(3, p.getName().toLowerCase());
				query.setString(4, c.getId());
				
				String[] groups = p.getGroups();
				if (groups.length > 0) {
					if(groups[0] == null || groups[0].equals("")) {
						query.setString(5, "");
					} else {
						query.setString(5, groups[0]);
					}
				} else {
					query.setString(5, "");
				}
				
				results = query.executeQuery();
				while (results.next()) {
					QuestInfo q = new QuestInfo(
							results.getString("id"),
							results.getString("quest_type"),
							results.getString("quest_name"),
							results.getString("quest_desc"),
							results.getString("start_npc"),
							results.getString("end_npc"),
							results.getString("prereq"),
							results.getString("items_provided"),
							results.getString("rewards"),
							results.getString("location"),
							results.getString("data"),
							results.getString("completion_text"),
							results.getString("rankreq"),
							results.getString("rankreward"),
							results.getString("cost"),
							results.getString("prize")
						);
					quests.add(q);
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,"DB Error fetching available quests: " + c.getId() + "," + p.getName(),e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
			
			return quests;
		}	
	}
	
	public HashMap<QuestInfo,String> getActiveQuests(Player p) {
		synchronized (questLock) {
			HashMap<QuestInfo,String> quests = new HashMap<QuestInfo,String>();
			
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				query = conn.prepareStatement(
						"SELECT q.*, qa.progress " +
						"FROM quests_active AS qa " +
						"JOIN quests AS q ON q.id = qa.quest_id " +
						"WHERE qa.player_name = ? "
					);
				query.setString(1, p.getName().toLowerCase());
				results = query.executeQuery();
				while (results.next()) {
					QuestInfo q = new QuestInfo(
                        results.getString("id"),
                        results.getString("quest_type"),
                        results.getString("quest_name"),
                        results.getString("quest_desc"),
                        results.getString("start_npc"),
                        results.getString("end_npc"),
                        results.getString("prereq"),
                        results.getString("items_provided"),
                        results.getString("rewards"),
                        results.getString("location"),
                        results.getString("data"),
                        results.getString("completion_text"),
                        results.getString("rankreq"),
                        results.getString("rankreward"),
                        results.getString("cost"),
                        results.getString("prize")
                    );
					String s = results.getString("progress");
					quests.put(q,s);
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,"DB Error fetching active quests: " + p.getName(),e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
			
			return quests;
		}	
	}

	public void saveActiveQuest(Player player, Quest quest) {
		synchronized (questLock) {		
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				query = conn.prepareStatement("INSERT INTO quests_active (player_name, quest_id) VALUES (?,?)");
				query.setString(1, player.getName().toLowerCase());
				query.setString(2, quest.getId());
				query.executeUpdate();
			} catch (SQLException e) {
				log.log(Level.SEVERE,"DB Error saving active quest: " + player.getName() + "," + quest.getId(),e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
		}		
	}

	public void saveQuestProgress(Player player, Quest quest, String progress) {
		synchronized (questLock) {		
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				query = conn.prepareStatement("UPDATE quests_active SET progress = ? WHERE player_name = ? AND quest_id = ?");
				query.setString(1, progress);
				query.setString(2, player.getName().toLowerCase());
				query.setString(3, quest.getId());
				query.executeUpdate();
			} catch (SQLException e) {
				log.log(Level.SEVERE,"DB Error saving quest progress: " + player.getName() + "," + quest.getId(),e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
		}		
	}
	
	public void dropActiveQuest(Player player, Quest quest) {
		synchronized (questLock) {		
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				query = conn.prepareStatement("DELETE FROM quests_active where player_name = ? AND quest_id = ?");
				query.setString(1, player.getName().toLowerCase());
				query.setString(2, quest.getId());
				query.executeUpdate();
			} catch (SQLException e) {
				log.log(Level.SEVERE,"DB Error dropping quest: " + player.getName() + "," + quest.getId(),e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
		}	
	}

	public void saveCompletedQuest(Player player, Quest quest) {
		synchronized (questLock) {		
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				query = conn.prepareStatement("DELETE FROM quests_active WHERE player_name = ? AND quest_id = ?");
				query.setString(1, player.getName().toLowerCase());
				query.setString(2, quest.getId());
				query.executeUpdate();
				query.close();
				
				query = conn.prepareStatement("INSERT INTO quests_completed (player_name, quest_id, date_completed) VALUES (?,?,CURRENT_TIME)");
				query.setString(1, player.getName().toLowerCase());
				query.setString(2, quest.getId());
				query.executeUpdate();
			} catch (SQLException e) {
				log.log(Level.SEVERE,"DB Error saving completed quest: " + player.getName() + "," + quest.getId(),e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
		}	
		
	}
	
	public void saveQuest(QuestInfo quest) {
		synchronized (questLock) {		
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				
				boolean exists = false;
				query = conn.prepareStatement("SELECT * FROM quests WHERE id = ?");
				query.setString(1, quest.id);
				results = query.executeQuery();
				if (results.next()) {
					exists = true;
				}
				results.close();
				query.close();
				
				if (exists) {
					query = conn.prepareStatement("UPDATE quests SET " +
							"quest_name = ?, quest_type = ?, quest_desc = ?, " +
							"start_npc = ?, end_npc = ?, prereq = ?, " +
							"items_provided = ?, rewards = ?, " +
							"location = ?, data = ?, completion_text = ?, " +
							"rankreq = ?, rankreward = ?, " +
							"cost = ?, prize = ? " +
							"WHERE id = ?");
					query.setString(1,quest.name);
					query.setString(2,quest.type);
					query.setString(3,quest.desc);
					query.setString(4,quest.pickUp);
					query.setString(5,quest.turnIn);
					query.setString(6,quest.prereq);
					query.setString(7,quest.itemsProvidedStr);
					query.setString(8,quest.rewardsStr);
					query.setString(9,quest.location);
					query.setString(10,quest.data);
					query.setString(11,quest.completionText);
					query.setString(12,quest.rankReq);
					query.setString(13,quest.rankReward);
					query.setString(14,(new Integer(quest.cost)).toString());
					query.setString(15,(new Integer(quest.prize)).toString());
					query.setString(16,quest.id);
					query.executeUpdate();
				} else {
					query = conn.prepareStatement("INSERT INTO quests (id, quest_name, quest_type, quest_desc, start_npc, end_npc, prereq, items_provided, rewards, location, data, completion_text, rankreq, rankreward, cost, prize) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
					query.setString(1,quest.id);
					query.setString(2,quest.name);
					query.setString(3,quest.type);
					query.setString(4,quest.desc);
					query.setString(5,quest.pickUp);
					query.setString(6,quest.turnIn);
					query.setString(7,quest.prereq);
					query.setString(8,quest.itemsProvidedStr);
					query.setString(9,quest.rewardsStr);
					query.setString(10,quest.location);
					query.setString(11,quest.data);
					query.setString(12,quest.completionText);
					query.setString(13,quest.rankReq);
					query.setString(14,quest.rankReward);
					query.setString(15,(new Integer(quest.cost)).toString());
					query.setString(16,(new Integer(quest.prize)).toString());
					query.executeUpdate();					
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,"DB Error saving quest: " + quest.getId(),e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
		}	
		
	}

	public void deleteQuest(String questid) {
		synchronized (questLock) {		
			Connection conn = null;
			PreparedStatement query = null;
			ResultSet results = null;
			try {
				conn = getSQLConnection();
				query = conn.prepareStatement("DELETE FROM quests_active WHERE quest_id = ?");
				query.setString(1, questid);
				query.executeUpdate();
				query.close();
				
				query = conn.prepareStatement("DELETE FROM quests_completed WHERE quest_id = ?");
				query.setString(1, questid);
				query.executeUpdate();
				query.close();
				
				query = conn.prepareStatement("DELETE FROM quests WHERE id = ?");
				query.setString(1, questid);
				query.executeUpdate();
				query.close();
				
			} catch (SQLException e) {
				log.log(Level.SEVERE,"DB Error deleting quest: " + questid,e);
			} finally {
				try {
					if (results != null) results.close();
					if (query != null) query.close();
					if (conn != null) conn.close();
				} catch (SQLException e) {
				}
			}
		}	
		
	}
}
