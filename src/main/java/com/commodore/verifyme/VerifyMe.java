package com.commodore.verifyme;

import com.commodore.verifyme.bridge.TFMBridge;
import com.commodore.verifyme.command.Command_discord;
import com.commodore.verifyme.util.DiscordUtils;
import com.commodore.verifyme.util.ForumUtils;
import com.commodore.verifyme.util.StorageUtils;
import com.commodore.verifyme.command.Command_forum;
import com.commodore.verifyme.util.VLog;
import java.io.File;
import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import org.bukkit.plugin.java.JavaPlugin;

public class VerifyMe extends JavaPlugin
{
    public DiscordUtils dutils;
    public ForumUtils futils;
    public StorageUtils sutils;
    public VLog vlog;
    public TotalFreedomMod tfm;
    
    @Override
    public void onEnable()
    {
        // Logging
        this.vlog = new VLog(this);
        
        // Config
        this.createConfig();
        
        // Commands
        this.getCommand("forum").setExecutor(new Command_forum(this));
        this.getCommand("discord").setExecutor(new Command_discord(this));
        
        // Classes
        this.futils = new ForumUtils(this);
        this.dutils = new DiscordUtils(this);
        this.sutils = new StorageUtils(this);
        
        // Bridges
        this.tfm = new TFMBridge(this).start();
        
        // Storage
        this.sutils.processStorage();
        this.sutils.processInactiveAdmins();
        
        // Discord and Forum Verification
        this.dutils.start();
        this.futils.start();
        
        vlog.info("VerifyMe v1.0 enabled.");
    }
    
    @Override
    public void onDisable()
    {
        dutils.LINK_CODES.clear();
        dutils.VERIFY_CODES.clear();
        futils.LINK_CODES.clear();
        futils.VERIFY_CODES.clear();
        vlog.info("VerifyMe v1.0 disabled.");
    }
    
    private void createConfig()
    {
        new File(getDataFolder().getAbsolutePath()).mkdirs();
        File configFile = new File(getDataFolder() + File.separator + "config.yml");
        if(!configFile.exists())
        {
            vlog.info("Cannot find config.yml, Generating now...");
            vlog.info("Config generated!");
            saveDefaultConfig();
        }
        if(getConfig().getString("BotName").isEmpty() || getConfig().getString("ForumUsername").isEmpty() || getConfig().getString("ForumPassword").isEmpty() || getConfig().getString("ForumURL").isEmpty())
        {
            vlog.warning("You have not filled out the config! This will cause issues in operation.");
        }
    }
    
}
