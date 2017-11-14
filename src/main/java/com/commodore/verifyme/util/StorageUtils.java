package com.commodore.verifyme.util;

import com.commodore.verifyme.VerifyMe;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class StorageUtils
{
    private VerifyMe plugin;
    private Connection connection;
    
    public StorageUtils(VerifyMe plugin)
    {
        this.plugin = plugin;
    }
    
    public void processStorage()
    {
        try
        {
            File storageFile = new File(plugin.getDataFolder() + File.separator + "storage.db");
            String url = "jdbc:sqlite:" + storageFile.getAbsolutePath();
            if(!storageFile.exists())
            {
                this.connection = DriverManager.getConnection(url);
                if(this.connection != null)
                {
                    plugin.vlog.info("Cannot find database file, Generating now...");
                    this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS forumverifiedadmins(adminname text PRIMARY KEY, forumname text NOT NULL, lastlogin integer NOT NULL);");
                    this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS discordverifiedadmins(adminname text PRIMARY KEY, discordid text NOT NULL, lastlogin integer NOT NULL);");
                    plugin.vlog.info("Storage generated!");
                }
            }
            else
            {
                this.connection = DriverManager.getConnection(url);
                if(this.connection != null)
                {
                    plugin.vlog.info("Found database file, Loading now...");
                    plugin.vlog.info("Storage loaded!");
                }
            }
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
        }
    }
    
    public boolean hasAlreadyLinkedAccount(String adminName, LinkedAccountType type)
    {
        String sql;
        switch(type)
        {
            case DISCORD:
                sql = "SELECT adminname from discordverifiedadmins WHERE adminname = ?;";
                break;
            
            case FORUM:
                sql = "SELECT adminname from forumverifiedadmins WHERE adminname = ?;";
                break;
            default:
                return false;
        }
        try
        {
            final PreparedStatement ps = this.connection.prepareStatement(sql);
            ps.setString(1, adminName);
            ResultSet set = ps.executeQuery();
            if(set.next())
            {
                boolean hasAlreadyLinkedAccount = !set.getString("adminname").isEmpty();
                ps.close();
                return hasAlreadyLinkedAccount;
            }
            return false;
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
            return false;
        }
    }
    
    public void addAccountToStorage(Admin admin, String data, LinkedAccountType type)
    {
        String sql;
        switch(type)
        {
            case DISCORD:
                sql = "INSERT INTO discordverifiedadmins(adminname, discordid, lastlogin) VALUES(?, ?, ?);";
                break;
            
            case FORUM:
                sql = "INSERT INTO forumverifiedadmins(adminname, forumname, lastlogin) VALUES(?, ?, ?);";
                break;
            default:
                return;
        }
        try
        {
            final PreparedStatement ps = this.connection.prepareStatement(sql);
            ps.setString(1, admin.getName());
            ps.setString(2, data);
            ps.setLong(3, admin.getLastLogin().getTime());
            ps.executeUpdate();
            ps.close();
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
        }
    }
    
    public void deleteAccountFromStorage(String adminName, LinkedAccountType type)
    {
        String sql;
        switch(type)
        {
            case DISCORD:
                sql = "DELETE FROM discordverifiedadmins WHERE adminname = ?;";
                break;
            
            case FORUM:
                sql = "DELETE FROM forumverifiedadmins WHERE adminname = ?;";
                break;
            default:
                return;
        }
        try
        {
            final PreparedStatement ps = this.connection.prepareStatement(sql);
            ps.setString(1, adminName);
            ps.executeUpdate();
            ps.close();
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
        }
    }
    
    public String getForumUsername(Admin admin)
    {
        try
        {
            final PreparedStatement ps = this.connection.prepareStatement("SELECT forumname FROM forumverifiedadmins WHERE adminname = ?;");
            ps.setString(1, admin.getName());
            ResultSet set = ps.executeQuery();
            if(set.next())
            {
                String forumName = set.getString("forumname");
                ps.close();
                return forumName;
            }
            ps.close();
            return null;
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
            return null;
        }
    }
    
    public String getDiscordId(Admin admin)
    {
        try
        {
            final PreparedStatement ps = this.connection.prepareStatement("SELECT discordid FROM discordverifiedadmins WHERE adminname = ?;");
            ps.setString(1, admin.getName());
            ResultSet set = ps.executeQuery();
            if(set.next())
            {
                String discordId = set.getString("discordid");
                ps.close();
                return discordId;
            }
            ps.close();
            return null;
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
            return null;
        }
    }
    
    public void processInactiveAdmins()
    {
        try
        {
            for(Admin admin : plugin.tfm.al.getAllAdmins().values())
            {
                for(LinkedAccountType type : LinkedAccountType.values())
                {
                    String sql;
                    switch(type)
                    {
                        case DISCORD:
                            sql = "SELECT lastlogin FROM discordverifiedadmins WHERE adminname = ?;";
                            break;
                        
                        case FORUM:
                            sql = "SELECT lastlogin FROM forumverifiedadmins WHERE adminname = ?;";
                            break;
                        default:
                            return;
                    }
                    
                    PreparedStatement ps = this.connection.prepareStatement(sql);
                    ps.setString(1, admin.getName());
                    ResultSet set = ps.executeQuery();
                    if(set.next())
                    {
                        long lastLogin = set.getLong("lastlogin");
                        final long lastLoginHours = TimeUnit.HOURS.convert(new Date().getTime() - lastLogin, TimeUnit.MILLISECONDS);
                        if(lastLoginHours > ConfigEntry.ADMINLIST_CLEAN_THESHOLD_HOURS.getInteger())
                        {
                            plugin.vlog.info(admin.getName() + " has been purged from the " + (type == LinkedAccountType.DISCORD ? "discord database." : "forum database."));
                            this.deleteAccountFromStorage(admin.getName(), type);
                            ps.close();
                        }
                        ps.close();
                    }
                    ps.close();
                }
            }
            
            for(LinkedAccountType type : LinkedAccountType.values())
            {
                String sql;
                switch(type)
                {
                    case DISCORD:
                        sql = "SELECT adminname FROM discordverifiedadmins;";
                        break;
                    
                    case FORUM:
                        sql = "SELECT adminname FROM forumverifiedadmins;";
                        break;
                    default:
                        return;
                }
                final PreparedStatement ps = this.connection.prepareStatement(sql);
                ResultSet set = ps.executeQuery();
                while(set.next())
                {
                    String adminName = set.getString("adminname");
                    Admin admin = plugin.tfm.al.getEntryByName(adminName);
                    if(admin == null)
                    {
                        deleteAccountFromStorage(adminName, type);
                        plugin.vlog.info(adminName + " has been purged from the " + (type == LinkedAccountType.DISCORD ? "discord database." : "forum database."));
                    }
                }
                ps.close();
            }
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
        }
    }
    
    
    private void updateLastLogin(Admin admin, LinkedAccountType type)
    {
        String sql;
        switch(type)
        {
            case DISCORD:
                sql = "UPDATE discordverifiedadmins SET lastlogin = ? WHERE adminname = ?;";
                break;
            
            case FORUM:
                sql = "UPDATE forumverifiedadmins SET lastlogin = ? WHERE adminname = ?;";
                break;
            default:
                return;
        }
        try
        {
            final PreparedStatement ps = this.connection.prepareStatement(sql);
            ps.setLong(1, admin.getLastLogin().getTime());
            ps.setString(2, admin.getName());
            ps.executeUpdate();
            ps.close();
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
        }
    }
    
    public void closeConnection()
    {
        try
        {
            this.connection.close();
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if(plugin.tfm.al.isAdmin(player))
        {
            for(LinkedAccountType type : LinkedAccountType.values())
            {
                if(this.hasAlreadyLinkedAccount(player.getName(), type))
                {
                    this.updateLastLogin(plugin.tfm.al.getAdmin(player), type);
                }
            }
        }
    }
}
