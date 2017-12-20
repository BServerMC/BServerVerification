package com.commodore.verifyme.util;

import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.protocol.ResponseProcessCookies;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

class SilentHtmlUnitDriver extends HtmlUnitDriver
{
    SilentHtmlUnitDriver(boolean debug)
    {
        super();
        if(!debug)
        {
            Logger.getLogger(ResponseProcessCookies.class.getName()).setLevel(Level.OFF);
            this.getWebClient().setCssErrorHandler(new SilentCssErrorHandler());
        }
    }
}
