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
    
    public void processStorage()
    {
        try
        {
            File storageFile = new File(plugin.getDataFolder() + File.separator + "storage.db");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + storageFile.getAbsolutePath());
            if(this.connection != null)
            {
                if(!storageFile.exists())
                {
                    plugin.vlog.info("Cannot find database file, Generating now...");
                    final String fsql = "CREATE TABLE IF NOT EXISTS forumverifiedadmins(adminname text PRIMARY KEY, forumname text NOT NULL, lastlogin integer NOT NULL);";
                    this.connection.createStatement().execute(fsql);
                    final String dsql = "CREATE TABLE IF NOT EXISTS discordverifiedadmins(adminname text PRIMARY KEY, discordid text NOT NULL, lastlogin integer NOT NULL);";
                    this.connection.createStatement().execute(dsql);
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
    
    public boolean hasAlreadyLinkedForumAccount(String adminName)
    {
        try
        {
            final String sql = "SELECT adminname from forumverifiedadmins WHERE adminname = ?;";
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
    
    public boolean hasAlreadyLinkedDiscordAccount(String adminName)
    {
        try
        {
            final String sql = "SELECT adminname from discordverifiedadmins WHERE adminname = ?;";
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
    
    public void addForumAccountToStorage(Admin admin, String forumUsername)
    {
        try
        {
            final String sql = "INSERT INTO forumverifiedadmins(adminname, forumname, lastlogin) VALUES(?, ?, ?);";
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
    
    public void addDiscordAccountToStorage(Admin admin, String discordId)
    {
        try
        {
            final String sql = "INSERT INTO discordverifiedadmins(adminname, discordid, lastlogin) VALUES(?, ?, ?);";
            final PreparedStatement ps = this.connection.prepareStatement(sql);
            ps.setString(1, admin.getName());
            ps.setString(2, discordId);
            ps.setLong(3, admin.getLastLogin().getTime());
            ps.executeUpdate();
            ps.close();
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
        }
    }
    
    public void deleteForumAccountFromStorage(String adminName)
    {
        try
        {
            String sql = "DELETE FROM forumverifiedadmins WHERE adminname = ?;";
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
    
    public void deleteDiscordAccountFromStorage(String adminName)
    {
        try
        {
            String sql = "DELETE FROM discordverifiedadmins WHERE adminname = ?;";
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
            final String sql = "SELECT forumname FROM forumverifiedadmins WHERE adminname = ?;";
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
    
    public String getDiscordId(Admin admin)
    {
        try
        {
            final String sql = "SELECT forumname FROM discordverifiedadmins WHERE adminname = ?;";
            final PreparedStatement ps = this.connection.prepareStatement(sql);
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
                String fsql = "SELECT lastlogin FROM forumverifiedadmins WHERE adminname = ?;";
                PreparedStatement fps = this.connection.prepareStatement(fsql);
                fps.setString(1, admin.getName());
                ResultSet fset = fps.executeQuery();
                if(fset.next())
                {
                    long lastLogin = fset.getLong("lastlogin");
                    final long lastLoginHours = TimeUnit.HOURS.convert(new Date().getTime() - lastLogin, TimeUnit.MILLISECONDS);
                    if(lastLoginHours > 40)
                    {
                        plugin.vlog.info(admin.getName() + " has been purged from the database.");
                        deleteForumAccountFromStorage(admin.getName());
                        fps.close();
                    }
                    fps.close();
                }
                fps.close();
                
                String dsql = "SELECT lastlogin FROM discordverifiedadmins WHERE adminname = ?;";
                PreparedStatement dps = this.connection.prepareStatement(dsql);
                dps.setString(1, admin.getName());
                ResultSet dset = dps.executeQuery();
                if(dset.next())
                {
                    long lastLogin = dset.getLong("lastlogin");
                    final long lastLoginHours = TimeUnit.HOURS.convert(new Date().getTime() - lastLogin, TimeUnit.MILLISECONDS);
                    if(lastLoginHours > 40)
                    {
                        plugin.vlog.info(admin.getName() + " has been purged from the database.");
                        deleteDiscordAccountFromStorage(admin.getName());
                        dps.close();
                    }
                    dps.close();
                }
                dps.close();
            }
            
            final String fsql = "SELECT adminname FROM forumverifiedadmins;";
            final PreparedStatement fps = this.connection.prepareStatement(fsql);
            ResultSet fset = fps.executeQuery();
            while(fset.next())
            {
                String adminName = fset.getString("adminname");
                Admin admin = plugin.tfm.al.getEntryByName(adminName);
                if(admin == null)
                {
                    deleteForumAccountFromStorage(adminName);
                }
            }
            fps.close();
            
            final String dsql = "SELECT adminname FROM discordverifiedadmins;";
            final PreparedStatement dps = this.connection.prepareStatement(dsql);
            ResultSet dset = dps.executeQuery();
            while(dset.next())
            {
                String adminName = dset.getString("adminname");
                Admin admin = plugin.tfm.al.getEntryByName(adminName);
                if(admin == null)
                {
                    deleteForumAccountFromStorage(adminName);
                }
            }
            dps.close();
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
        }
    }
    
    private void updateForumLastLogin(Admin admin)
    {
        try
        {
            final String fsql = "UPDATE forumverifiedadmins SET lastlogin = ? WHERE adminname = ?;";
            final PreparedStatement fps = this.connection.prepareStatement(fsql);
            fps.setLong(1, admin.getLastLogin().getTime());
            fps.setString(2, admin.getName());
            fps.executeUpdate();
            fps.close();
        }
        catch(SQLException e)
        {
            plugin.vlog.severe(e.getMessage());
        }
    }
    
    private void updateDiscordLastLogin(Admin admin)
    {
        try
        {
            final String dsql = "UPDATE discordverifiedadmins SET lastlogin = ? WHERE adminname = ?;";
            final PreparedStatement dps = this.connection.prepareStatement(dsql);
            dps.setLong(1, admin.getLastLogin().getTime());
            dps.setString(2, admin.getName());
            dps.executeUpdate();
            dps.close();
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
            if(this.hasAlreadyLinkedDiscordAccount(player.getName()))
            {
                updateDiscordLastLogin(plugin.tfm.al.getAdmin(player));
            }
            else if(this.hasAlreadyLinkedForumAccount(player.getName()))
            {
                updateForumLastLogin(plugin.tfm.al.getAdmin(player));
            }
        }
    }
}
