package com.commodore.verifyme.command;

import com.commodore.verifyme.VerifyMe;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Command_verifyme implements CommandExecutor
{
    private VerifyMe plugin;
    
    public Command_verifyme(VerifyMe plugin)
    {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        sender.sendMessage(ChatColor.GREEN + "VerifyMe is a plugin to verify admins on the TotalFreedom Minecraft server.");
        sender.sendMessage(ChatColor.GREEN + "It was solely created and developed by Commodore64x.");
        sender.sendMessage(ChatColor.GREEN + "Forum and Discord verification is available by doing /forum and /discord respectively.");
        return true;
    }
}