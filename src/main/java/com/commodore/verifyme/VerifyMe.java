package com.commodore.verifyme;

import com.commodore.verifyme.bridge.TFMBridge;
import com.commodore.verifyme.util.StorageUtils;
import com.commodore.verifyme.util.Utils;
import com.commodore.verifyme.command.Command_verifyme;
import com.commodore.verifyme.util.VLog;
import java.io.File;
import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import org.bukkit.plugin.java.JavaPlugin;

public class VerifyMe extends JavaPlugin
{
    public Utils utils;
    public StorageUtils sutils;
    public VLog vlog;
    public TotalFreedomMod tfm;
    
    @Override
    public void onEnable()
    {
        // Logging
        this.vlog = new VLog(this);
        
        // Config
        createConfig();
        new File(getDataFolder().getAbsolutePath()).mkdirs();
        
        // Commands
        this.getCommand("verifyme").setExecutor(new Command_verifyme(this));
        
        // Classes
        this.utils = new Utils(this);
        this.sutils = new StorageUtils(this);
        
        // Bridges
        this.tfm = new TFMBridge(this).start();
        
        // Storage
        this.sutils.createStorage();
        this.sutils.processInactiveAdmins();
        
        vlog.info("VerifyMe v1.0 enabled.");
    }
    
    @Override
    public void onDisable()
    {
        utils.LINK_CODES.clear();
        utils.VERIFY_CODES.clear();
        vlog.info("VerifyMe v1.0 disabled.");
    }
    
    private void createConfig()
    {
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
