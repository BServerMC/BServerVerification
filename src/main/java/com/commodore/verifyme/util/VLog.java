package com.commodore.verifyme.util;

import com.commodore.verifyme.VerifyMe;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VLog
{
    private Logger vlog;
    
    public VLog(VerifyMe plugin)
    {
        this.vlog = plugin.getLogger();
    }
    
    private void log(Level level, String message)
    {
        this.vlog.log(level, message);
    }
    
    public void info(String message)
    {
        log(Level.INFO, message);
    }
    
    public void warning(String message)
    {
        log(Level.WARNING, message);
    }
    
    public void severe(String message)
    {
        log(Level.SEVERE, message);
    }
}
