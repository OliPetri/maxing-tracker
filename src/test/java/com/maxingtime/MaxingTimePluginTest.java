package com.maxingtime;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
public class MaxingTimePluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(MaxingTimePlugin.class);
        RuneLite.main(args);
    }
}
