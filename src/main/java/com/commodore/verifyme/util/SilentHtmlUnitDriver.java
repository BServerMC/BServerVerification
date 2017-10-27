package com.commodore.verifyme.util;

import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.protocol.ResponseProcessCookies;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class SilentHtmlUnitDriver extends HtmlUnitDriver
{
    public SilentHtmlUnitDriver()
    {
        super();
        Logger.getLogger(ResponseProcessCookies.class.getName()).setLevel(Level.OFF);
        this.getWebClient().setCssErrorHandler(new SilentCssErrorHandler());
    }
}
