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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class StorageUtils
{
    private VerifyMe plugin;
    public Connection connection;
    
    public StorageUtils(VerifyMe plugin)
    {
        this.plugin = plugin;
    }
    
    public void createStorage()
    {
        try
        {
            File storageFile = new File(plugin.getDataFolder() + File.separator + "storage.db");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + storageFile.getAbsolutePath());
            if(this.connection != null)
            {
                if(!storageFile.exists())
                {
                    final String sql = "CREATE TABLE IF NOT EXISTS verifiedadmins(adminname text PRIMARY KEY, forumname text NOT NULL, lastlogin integer NOT NULL);";
                    this.connection.createStatement().execute(sql);
                    plugin.vlog.info("Cannot find database file, Generating now...");
                    plugin.vlog.info("Storage generated!");
                }
                else
                {
                    plugin.vlog.info("Found database file, Loading now...");
                    plugin.vlog.info("Storage loaded!");
                }
            }
            else
            {
                plugin.vlog.warning("A connection to the database failed to be created.");
            }
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
        }
    }
    
    public boolean hasAlreadyLinkedAccount(String adminName)
    {
        try
        {
            final String sql = "SELECT adminname from verifiedadmins WHERE adminname = ?;";
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
    
    public void addToStorage(Admin admin, String forumUsername)
    {
        try
        {
            final String sql = "INSERT INTO verifiedadmins(adminname, forumname, lastlogin) VALUES(?, ?, ?);";
            final PreparedStatement ps = this.connection.prepareStatement(sql);
            ps.setString(1, admin.getName());
            ps.setString(2, forumUsername);
            ps.setLong(3, admin.getLastLogin().getTime());
            ps.executeUpdate();
            ps.close();
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
        }
    }
    
    public void deleteFromStorage(String adminName)
    {
        try
        {
            String sql = "DELETE FROM verifiedadmins WHERE adminname = ?;";
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
            final String sql = "SELECT forumname FROM verifiedadmins WHERE adminname = ?;";
            final PreparedStatement ps = this.connection.prepareStatement(sql);
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
    
    public void processInactiveAdmins()
    {
        try
        {
            for(Admin admin : plugin.tfm.al.getAllAdmins().values())
            {
                String sql = "SELECT lastlogin FROM verifiedadmins WHERE adminname = ?;";
                PreparedStatement ps = this.connection.prepareStatement(sql);
                ps.setString(1, admin.getName());
                ResultSet set = ps.executeQuery();
                if(set.next())
                {
                    long lastLogin = set.getLong("lastlogin");
                    final long lastLoginHours = TimeUnit.HOURS.convert(new Date().getTime() - lastLogin, TimeUnit.MILLISECONDS);
                    if(lastLoginHours > 40)
                    {
                        plugin.vlog.info(admin.getName() + " has been purged from the database.");
                        deleteFromStorage(admin.getName());
                        ps.close();
                    }
                    ps.close();
                }
                ps.close();
            }
            
            final String sql = "SELECT adminname FROM verifiedadmins;";
            final PreparedStatement newPs = this.connection.prepareStatement(sql);
            ResultSet set = newPs.executeQuery();
            while(set.next())
            {
                String adminName = set.getString("adminname");
                Admin admin = plugin.tfm.al.getEntryByName(adminName);
                if(admin == null)
                {
                    deleteFromStorage(adminName);
                }
            }
            newPs.close();
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
        }
    }
    
    private void updateLastLogin(Admin admin)
    {
        try
        {
            final String sql = "UPDATE verifiedadmins SET lastlogin = ? WHERE adminname = ?;";
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
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if(plugin.tfm.al.isAdmin(player))
        {
            updateLastLogin(plugin.tfm.al.getAdmin(player));
        }
    }
}
