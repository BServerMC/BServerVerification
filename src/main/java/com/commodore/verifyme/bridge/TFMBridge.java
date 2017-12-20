package com.commodore.verifyme.bridge;

import com.commodore.verifyme.VerifyMe;
import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import org.bukkit.plugin.PluginManager;

public class TFMBridge
{
    private VerifyMe plugin;
    
    public TFMBridge(VerifyMe plugin)
    {
        this.plugin = plugin;
    }
    
    public TotalFreedomMod start()
    {
        PluginManager manager = plugin.getServer().getPluginManager();
        if(manager.getPlugin("TotalFreedomMod").isEnabled())
        {
            return TotalFreedomMod.plugin();
        }
        else
        {
            plugin.vlog.warning("The TotalFreedomMod is not enabled! This plugin will not work as expected.");
            return null;
        }
    }
}