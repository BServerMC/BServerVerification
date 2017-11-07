package com.commodore.verifyme.util;

import com.commodore.verifyme.VerifyMe;
import java.util.HashMap;
import java.util.Iterator;
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
        enabled = plugin.getConfig().getBoolean("DiscordVerification") && !plugin.getConfig().getString("DiscordBotToken").isEmpty();
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
            }
            catch(IllegalArgumentException | InterruptedException e)
            {
                plugin.vlog.warning("The VerifyMe Discord Verification System failed to start due to an error querying the server.");
            }
            catch(RateLimitedException ex)
            {
                plugin.vlog.warning("The VerifyMe Discord Verification System failed to start due to rate-limiting.");
            }
        }
        else if(plugin.getConfig().getString("DiscordBotToken").isEmpty())
        {
            plugin.vlog.warning("No VerifyMe Discord Verification Bot token was specified, the VerifyMe Discord Verification System will be unavailable.");
        }
    }
    
    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event)
    {
        String token = event.getMessage().getRawContent();
        if(!event.getAuthor().getId().equals(this.bot.getSelfUser().getId()) && token.matches("[0-9][0-9][0-9][0-9][0-9][0-9]"))
        {
            Set set = this.LINK_CODES.entrySet();
            Iterator i = set.iterator();
            while(i.hasNext())
            {
                Map.Entry me = (Map.Entry) i.next();
                if(me.getValue().toString().equals(token))
                {
                    Admin admin = (Admin) me.getKey();
                    LINK_CODES.remove(admin);
                    plugin.sutils.addDiscordAccountToStorage(admin, event.getAuthor().getId());
                    event.getChannel().sendMessage("Your discord account has been successfully linked to your ingame account.").queue();
                    break;
                }
            }
        }
    }
    
    public String generateToken()
    {
        StringBuilder token = new StringBuilder();
        Random random = new Random();
        for(int i = 0;i < 6;i++)
        {
            token.append(random.nextInt(10));
        }
        return token.toString();
    }
}
