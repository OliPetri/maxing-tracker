package com.maxingtime;

import java.util.Locale;
import javax.inject.Inject;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;

/** Rates belong to the selected RuneLite configuration profile (shared across characters). */
public class XpRateStore
{
    @Inject private ConfigManager configManager;
    public static final long MAX_RATE = 1_000_000_000L;

    private String key(Skill skill) { return "rate_" + skill.name().toLowerCase(Locale.ROOT); }

    public long get(Skill skill)
    {
        try { return parse(configManager.getConfiguration(MaxingTimeConfig.GROUP, key(skill))); }
        catch (IllegalArgumentException ex) { return 0; }
    }

    public void set(Skill skill, long rate)
    {
        if (rate < 0 || rate > MAX_RATE) { throw new IllegalArgumentException("Invalid XP/hr"); }
        configManager.setConfiguration(MaxingTimeConfig.GROUP, key(skill), rate);
    }

    public String getMethod(Skill skill)
    {
        String value = configManager.getConfiguration(MaxingTimeConfig.GROUP, "method_" + key(skill));
        return value == null ? "custom" : value;
    }

    public void setMethod(Skill skill, String method)
    {
        configManager.setConfiguration(MaxingTimeConfig.GROUP, "method_" + key(skill), method);
    }

    public boolean isPassive(Skill skill)
    {
        return Boolean.parseBoolean(configManager.getConfiguration(MaxingTimeConfig.GROUP, "passive_" + key(skill)));
    }

    public void setPassive(Skill skill, boolean passive)
    {
        configManager.setConfiguration(MaxingTimeConfig.GROUP, "passive_" + key(skill), passive);
    }

    public long effectiveRate(Skill skill)
    {
        TrainingMethod method = TrainingMethods.selected(skill, getMethod(skill));
        return method.isCustom() ? get(skill) : method.rate;
    }

    public static long parse(String text)
    {
        if (text == null || text.trim().isEmpty()) { return 0; }
        String value = text.trim();
        if (!value.matches("[0-9]+")) { throw new IllegalArgumentException("Use whole numbers without separators"); }
        long rate = Long.parseLong(value);
        if (rate > MAX_RATE) { throw new IllegalArgumentException("XP/hr is too large"); }
        return rate;
    }
}
