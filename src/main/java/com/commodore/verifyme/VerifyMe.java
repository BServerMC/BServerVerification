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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class VerifyMe extends JavaPlugin
{
    public ForumUtils futils;
    public DiscordUtils dutils;
    public StorageUtils sutils;
    public VLog vlog;
    public TotalFreedomMod tfm;
    public PluginDescriptionFile pdf;
    
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
        
        this.pdf = this.getDescription();
        
        vlog.info(String.format("VerifyMe v%s enabled.", pdf.getVersion()));
    }
    
    @Override
    public void onDisable()
    {
        if(dutils.enabled)
        {
            dutils.bot.getRegisteredListeners().forEach(listener -> dutils.bot.removeEventListener(listener));
        }
        dutils.LINK_CODES.clear();
        dutils.VERIFY_CODES.clear();
        dutils.enabled = false;
        
        futils.LINK_CODES.clear();
        futils.VERIFY_CODES.clear();
        futils.enabled = false;
        
        sutils.closeConnection();
        vlog.info(String.format("VerifyMe v%s disabled.", pdf.getVersion()));
    }
    
    private void createConfig()
    {
        new File(getDataFolder().getAbsolutePath()).mkdirs();
        if(!new File(getDataFolder() + File.separator + "config.yml").exists())
        {
            vlog.info("Cannot find config.yml, Generating now...");
            vlog.info("Config generated!");
            saveDefaultConfig();
        }
        validateConfig();
    }
    
    private void validateConfig()
    {
        if(getConfig().getBoolean("ForumVerification") && (getConfig().getString("ForumUsername").isEmpty() || getConfig().getString("ForumPassword").isEmpty() || getConfig().getString("ForumURL").isEmpty()))
        {
            vlog.warning("You have not filled out the forum verification config properly! This will cause issues in operation.");
        }
        if(getConfig().getBoolean("DiscordVerification") && getConfig().getString("DiscordBotToken").isEmpty())
        {
            vlog.warning("You have not filled out the discord verification config properly! This will cause issues in operation.");
        }
    }
    
    public String generateToken()
    {
        Random random = new Random();
        return IntStream.range(0, 6).mapToObj(i -> String.valueOf(random.nextInt(10))).collect(Collectors.joining());
    }
}