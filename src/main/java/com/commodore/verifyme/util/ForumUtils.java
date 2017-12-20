package com.commodore.verifyme.util;

import com.commodore.verifyme.VerifyMe;
import java.util.HashMap;
import java.util.List;
import me.totalfreedom.totalfreedommod.admin.Admin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class ForumUtils
{
    public HashMap<Admin, String> LINK_CODES = new HashMap<>();
    public HashMap<Admin, String> VERIFY_CODES = new HashMap<>();
    
    public SilentHtmlUnitDriver bot;
    public String botName;
    public boolean enabled = false;
    
    private VerifyMe plugin;
    
    public ForumUtils(VerifyMe plugin)
    {
        this.plugin = plugin;
    }
    
    public void start()
    {
        this.enabled = plugin.getConfig().getBoolean("ForumVerification")
                       && !plugin.getConfig().getString("ForumUsername").isEmpty()
                       && !plugin.getConfig().getString("ForumPassword").isEmpty()
                       && !plugin.getConfig().getString("ForumURL").isEmpty();
        if(this.enabled)
        {
            try
            {
                new BukkitRunnable()
                {
                    
                    @Override
                    public void run()
                    {
                        bot = new SilentHtmlUnitDriver(plugin.getConfig().getBoolean("DebugMessages"));
                        bot.get(plugin.getConfig().getString("ForumURL"));
                        bot.findElement(By.className("login")).click();
                        bot.findElement(By.cssSelector("input[name='email']")).sendKeys(plugin.getConfig().getString("ForumUsername"));
                        bot.findElement(By.cssSelector("input[name='password']")).sendKeys(plugin.getConfig().getString("ForumPassword"));
                        bot.findElement(By.cssSelector("input[name='continue']")).click();
                        bot.findElement(By.cssSelector("input[type='submit']")).click();
                        botName = bot.findElement(By.id("welcome")).getText();
                        bot.close();
                        plugin.vlog.info("The VerifyMe Forum Verification System was enabled.");
                    }
                }.runTaskAsynchronously(plugin);
            }
            catch(NoSuchElementException e)
            {
                this.bot.close();
                plugin.vlog.warning("An invalid VerifyMe Forum Verification Bot username or password was specified, the VerifyMe Forum Verification System will be unavailable.");
                this.enabled = false;
            }
        }
    }
    
    private boolean doesElementExist(WebElement driver, By by)
    {
        try
        {
            driver.findElement(by);
        }
        catch(NoSuchElementException e)
        {
            return false;
        }
        return true;
    }
    
    private void sendPM(final String forumUsername, final String subject, final String message)
    {
        SilentHtmlUnitDriver driver = new SilentHtmlUnitDriver(plugin.getConfig().getBoolean("DebugMessages"));
        driver.get(plugin.getConfig().getString("ForumURL"));
        driver.findElement(By.className("login")).click();
        driver.findElement(By.cssSelector("input[name='email']")).sendKeys(plugin.getConfig().getString("ForumUsername"));
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys(plugin.getConfig().getString("ForumPassword"));
        driver.findElement(By.cssSelector("input[name='continue']")).click();
        driver.findElement(By.cssSelector("input[type='submit']")).click();
        
        driver.findElement(By.cssSelector("a[href='/conversations']")).click();
        driver.findElement(By.cssSelector("a[href='/conversation/new']")).click();
        driver.findElement(By.cssSelector("input[name='subject']")).sendKeys(subject);
        driver.findElement(By.cssSelector("input[name='recipients_all']")).sendKeys(forumUsername);
        driver.findElement(By.cssSelector("textarea[name='message']")).sendKeys(message);
        driver.findElement(By.cssSelector("input[name='post']")).click();
        driver.close();
    }
    
    private boolean findNewPM(Player player)
    {
        SilentHtmlUnitDriver driver = new SilentHtmlUnitDriver(plugin.getConfig().getBoolean("DebugMessages"));
        driver.get(plugin.getConfig().getString("ForumURL"));
        driver.findElement(By.className("login")).click();
        driver.findElement(By.cssSelector("input[name='email']")).sendKeys(plugin.getConfig().getString("ForumUsername"));
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys(plugin.getConfig().getString("ForumPassword"));
        driver.findElement(By.cssSelector("input[name='continue']")).click();
        driver.findElement(By.cssSelector("input[type='submit']")).click();
        
        driver.findElement(By.cssSelector("a[href='/conversations']")).click();
        List<WebElement> conversations = driver.findElements(By.className("conversation"));
        for(int i = 0; i < conversations.size(); i++)
        {
            if(doesElementExist(conversations.get(i).findElement(By.className("icon")), By.cssSelector("img[src='//storage.proboards.com/forum/images/icons/message-new.png']")))
            {
                conversations.get(i).findElement(By.className("conversation-link")).click();
                
                String msg = driver.findElement(By.className("item")).findElement(By.className("message")).getText().trim();
                String forumUsername = driver.findElement(By.className("mini-profile")).findElement(By.tagName("a")).getAttribute("title").trim().replace("@", "");
                boolean isStaff = doesElementExist(driver.findElement(By.className("info")), By.tagName("h2"));
                
                Admin admin = plugin.tfm.al.getEntryByName(player.getName());
                if(!LINK_CODES.get(admin).equals(msg))
                {
                    driver.navigate().back();
                    conversations = driver.findElements(By.className("conversation"));
                    continue;
                }
                if(!isStaff)
                {
                    player.sendMessage(ChatColor.RED + "The specified forum account is not supered!");
                    driver.navigate().back();
                    conversations = driver.findElements(By.className("conversation"));
                    continue;
                }
                if(!msg.matches("[0-9][0-9][0-9][0-9][0-9][0-9]"))
                {
                    player.sendMessage(ChatColor.RED + "The specified token is presented in an invalid format.");
                    driver.navigate().back();
                    conversations = driver.findElements(By.className("conversation"));
                    continue;
                }
                
                plugin.vlog.info(admin.getName() + " has linked their forum account.");
                LINK_CODES.remove(admin);
                plugin.sutils.addAccountToStorage(admin, forumUsername, LinkedAccountType.FORUM);
                player.sendMessage(ChatColor.GREEN + "Your forum account has been successfully linked to your ingame account.");
                driver.close();
                return true;
            }
        }
        driver.close();
        return false;
    }
    
    public void sendNewPmTask(String forumUsername, String subject, String message)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                plugin.futils.sendPM(forumUsername, subject, message);
            }
        }.runTaskAsynchronously(plugin);
    }
    
    public void findNewPmTask(final Player player)
    {
        new BukkitRunnable()
        {
            int tries = 0;
            
            @Override
            public void run()
            {
                tries++;
                if(plugin.futils.findNewPM(player) || tries >= 40 || !player.isOnline() || player == null)
                {
                    tries = 0;
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 10 * 20L);
    }
}
