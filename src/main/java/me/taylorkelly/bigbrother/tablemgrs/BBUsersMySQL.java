/**
 * 
 */
package me.taylorkelly.bigbrother.tablemgrs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import me.taylorkelly.bigbrother.BBLogging;
import me.taylorkelly.bigbrother.BBPlayerInfo;
import me.taylorkelly.bigbrother.datasource.ConnectionManager;

/**
 * @author N3X15
 *
 */
public class BBUsersMySQL extends BBUsersTable {
    public final int revision = 1;
    public String toString() {
        return "BBUsers MySQL Driver r"+Integer.valueOf(revision);
    }
    
    /* (non-Javadoc)
     * @see me.taylorkelly.bigbrother.tablemgrs.DBTable#onLoad()
     */
    @Override
    protected void onLoad() {
    }
    
    /* (non-Javadoc)
     * @see me.taylorkelly.bigbrother.tablemgrs.DBTable#getCreateSyntax()
     */
    @Override
    public String getCreateSyntax() {
        return "CREATE TABLE `"+getTableName()+"` ("
        + "`id` INT NOT NULL AUTO_INCREMENT," 
        + "`name` varchar(32) NOT NULL DEFAULT 'Player'," 
        + "`flags` INT NOT NULL DEFAULT '0',"
        + "PRIMARY KEY (`id`),"
        + "UNIQUE(`name`));"; //Engine doesn't matter, really.
    }

    @Override
    public BBPlayerInfo getUserFromDB(String name) {

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionManager.getConnection();
            String sql = "SELECT id,name,flags FROM "+getTableName()+" WHERE `name`=?";
            BBLogging.debug(sql);
            ps = conn.prepareStatement(sql);
            ps.setString(1,name.toLowerCase());
            rs=ps.executeQuery();
            conn.commit();
            
            if(!rs.next())
                return null;
            
            return new BBPlayerInfo(rs.getInt("id"), rs.getString("name"), rs.getInt("flags"));
            
        } catch (SQLException e) {
            BBLogging.severe("Error trying to find the user `"+name+"`.", e);
        } finally {
            ConnectionManager.cleanup( "BBUsersMySQL.getUserFromDB(string)",conn, ps, rs );
        }
        return null;
    }

    @Override
    protected void do_addOrUpdatePlayer(BBPlayerInfo pi) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionManager.getConnection();
            if(pi.getNew() && getUserFromDB(pi.getName())==null) {
                ps = conn.prepareStatement("INSERT INTO "+getTableName()+" (name,flags) VALUES (?,?) ON DUPLICATE KEY UPDATE flags=VALUES(flags)");
                ps.setString(1,pi.getName());
                ps.setInt(2,pi.getFlags());
            } else {
                ps = conn.prepareStatement("UPDATE "+getTableName()+" SET flags = ? WHERE id=?");
                ps.setInt(1, pi.getFlags());
                ps.setInt(2, pi.getID());
            }
            BBLogging.debug(ps.toString());
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            BBLogging.severe("Can't update the user `"+pi.getName()+"`.", e);
        } finally {
            ConnectionManager.cleanup( "BBUsersMySQL.do_addOrUpdatePlayer",conn, ps, null );
        }
    }

    @Override
    public BBPlayerInfo getUserFromDB(int id) {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            String sql = "SELECT id,name,flags FROM " + getTableName() + " WHERE `id`=?;";
            conn = ConnectionManager.getConnection();
            BBLogging.debug(sql);
            ps = conn.prepareStatement(sql);
            ps.setInt(1,id);
            rs=ps.executeQuery();
            conn.commit();
            if(!rs.next())
                return null;
            
            return new BBPlayerInfo(rs.getInt("id"), rs.getString("name"), rs.getInt("flags"));
            
        } catch (SQLException e) {
            BBLogging.severe("Can't find user #"+id+".", e);
        } finally {
            ConnectionManager.cleanup( "BBUsersMySQL.getUserFromDB(int)",conn, ps, rs );
        }
        return null;
    }

    @Override
    public void importRecords() {
        String bbdata = BBDataTable.getInstance().getTableName();
        BBLogging.info("Importing users into new table!");

        BBLogging.info(" * Stage 1/4: Rename the old player column to PlayerName");
        if(!executeUpdate("importRecords(mysql) - Rename old player column",
                "ALTER TABLE "+bbdata+" CHANGE player playerName varchar(32) NOT NULL DEFAULT 'Player'"))
            return;

        BBLogging.info(" * Stage 2/4: Add player column with the new integer format (WILL TAKE A LONG TIME).");
        if(!executeUpdate("importRecords(mysql) - Add player column",
                "ALTER TABLE "+bbdata+" ADD COLUMN player INT UNSIGNED NOT NULL AUTO_INCREMENT;"))
            return;
        

        BBLogging.info(" * Stage 3/4: Convert player name -> player ID in bbdata while adding users to bbusers (WILL TAKE A LONG TIME).");
        {
            Connection conn = null;
            ResultSet rs = null;
            PreparedStatement ps = null;
            try {
                conn = ConnectionManager.getConnection();
                ps = conn.prepareStatement("SELECT DISTINCT playerName FROM "+bbdata);
                rs=ps.executeQuery();
                
                while(rs.next()) {
                    BBPlayerInfo pi = getUser(rs.getString("playerName"));

                    String desc = String.format("Player %s -> %d",pi.getName(),pi.getID());
                    BBLogging.info("Converting "+desc+"...");
                    executeUpdate(desc,
                            "UPDATE "+bbdata+" SET `player`=? WHERE LOWER(playerName)=LOWER('?')", new Object[]{
                                pi.getID(),
                                pi.getName()
                            }
                    );
                }
                
            } catch (SQLException e) {
                BBLogging.severe("Can't import old user records.", e);
                return;
            } finally {
                ConnectionManager.cleanup( "BBUsersMySQL.getUserFromDB(int)",conn, ps, rs );
            }
        }
        

        BBLogging.info(" * Stage 4/4: Drop playerName column.");
        if(!executeUpdate("importRecords(mysql) - Add player column",
                "ALTER TABLE "+bbdata+" DROP COLUMN playerName;"))
            return;
        
    }

    @Override
    protected void loadCache() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionManager.getConnection();
            String sql = "SELECT id,name,flags FROM "+getTableName();
            BBLogging.debug(sql);
            ps = conn.prepareStatement(sql);
            rs=ps.executeQuery();
            
            while(rs.next()){
                BBPlayerInfo pi = new BBPlayerInfo(rs.getInt("id"), rs.getString("name"), rs.getInt("flags"));
                this.knownPlayers.put(pi.getID(), pi);
                this.knownNames.put(pi.getName(),pi.getID());
            }
        } catch (SQLException e) {
            BBLogging.severe("Error trying to load the user/name cache.", e);
        } finally {
            ConnectionManager.cleanup( "BBUsersMySQL.getUserFromDB(string)",conn, ps, rs );
        }
    }
}
