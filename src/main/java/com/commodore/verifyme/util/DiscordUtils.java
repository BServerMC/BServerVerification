package com.commodore.verifyme.util;

import com.commodore.verifyme.VerifyMe;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.security.auth.login.LoginException;
import me.totalfreedom.totalfreedommod.admin.Admin;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class DiscordUtils extends ListenerAdapter
{
    public HashMap<Admin, String> LINK_CODES = new HashMap<>();
    public HashMap<Admin, String> VERIFY_CODES = new HashMap<>();
    
    public JDA bot = null;
    public boolean enabled = false;
    
    private VerifyMe plugin;
    
    public DiscordUtils(VerifyMe plugin)
    {
        this.plugin = plugin;
    }
    
    public void start()
    {
        this.enabled = plugin.getConfig().getBoolean("DiscordVerification")
                  && !plugin.getConfig().getString("DiscordBotToken").isEmpty();
        if(this.enabled)
        {
            try
            {
                bot = new JDABuilder(AccountType.BOT)
                        .setToken(plugin.getConfig().getString("DiscordBotToken"))
                        .addEventListener(this)
                        .buildBlocking();
                plugin.vlog.info("The VerifyMe Discord Verification System was enabled.");
            }
            catch(LoginException e)
            {
                plugin.vlog.warning("An invalid VerifyMe Discord Verification Bot token was specified, the VerifyMe Discord Verification System will be unavailable.");
                this.enabled = false;
            }
            catch(IllegalArgumentException | InterruptedException e)
            {
                plugin.vlog.warning("The VerifyMe Discord Verification System failed to start due to an error querying the server.");
                this.enabled = false;
            }
            catch(RateLimitedException ex)
            {
                plugin.vlog.warning("The VerifyMe Discord Verification System failed to start due to rate-limiting.");
                this.enabled = false;
            }
        }
    }
    
    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event)
    {
        String token = event.getMessage().getRawContent();
        if(!event.getAuthor().getId().equals(this.bot.getSelfUser().getId()) && token.matches("[0-9][0-9][0-9][0-9][0-9][0-9]"))
        {
            Set set = this.LINK_CODES.entrySet();
            for(Object linkEntry : set)
            {
                Map.Entry me = (Map.Entry) linkEntry;
                if(me.getValue().toString().equals(token))
                {
                    Admin admin = (Admin) me.getKey();
                    LINK_CODES.remove(admin);
                    plugin.sutils.addAccountToStorage(admin, event.getAuthor().getId(), LinkedAccountType.DISCORD);
                    event.getChannel().sendMessage("Your discord account has been successfully linked to your ingame account.").queue();
                    break;
                }
            }
        }
    }
}
