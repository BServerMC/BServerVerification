package com.commodore.verifyme;

import com.commodore.verifyme.bridge.TFMBridge;
import com.commodore.verifyme.command.Command_discord;
import com.commodore.verifyme.command.Command_verifyme;
import com.commodore.verifyme.util.DiscordUtils;
import com.commodore.verifyme.util.ForumUtils;
import com.commodore.verifyme.util.StorageUtils;
import com.commodore.verifyme.command.Command_forum;
import com.commodore.verifyme.util.VLog;
import java.io.File;
import java.util.Random;
import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import org.bukkit.plugin.java.JavaPlugin;

public class VerifyMe extends JavaPlugin
{
    public ForumUtils futils;
    public DiscordUtils dutils;
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
        this.getCommand("verifyme").setExecutor(new Command_verifyme(this));
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
        
        // Forum and Discord Verification
        this.futils.start();
        this.dutils.start();
        
        vlog.info("VerifyMe v1.2 enabled.");
    }
    
    @Override
    public void onDisable()
    {
        if(dutils.enabled)
        {
            for(Object listener : dutils.bot.getRegisteredListeners())
            {
                dutils.bot.removeEventListener(listener);
            }
        }
        dutils.LINK_CODES.clear();
        dutils.VERIFY_CODES.clear();
        dutils.enabled = false;
        
        futils.LINK_CODES.clear();
        futils.VERIFY_CODES.clear();
        futils.enabled = false;
        
        sutils.closeConnection();
        vlog.info("VerifyMe v1.2 disabled.");
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
        validateConfig();
    }
    
    private void validateConfig()
    {
        if(getConfig().getBoolean("ForumVerification"))
        {
            if(getConfig().getString("ForumUsername").isEmpty() || getConfig().getString("ForumPassword").isEmpty() || getConfig().getString("ForumURL").isEmpty())
            {
                vlog.warning("You have not filled out the forum verification config properly! This will cause issues in operation.");
            }
        }
        if(getConfig().getBoolean("DiscordVerification"))
        {
            if(getConfig().getString("DiscordBotToken").isEmpty())
            {
                vlog.warning("You have not filled out the discord verification config properly! This will cause issues in operation.");
            }
        }
    }
    
    public String generateToken()
    {
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < 6; i++)
        {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
