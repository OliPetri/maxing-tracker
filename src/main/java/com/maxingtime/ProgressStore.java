package com.maxingtime;

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfile;

/** Baseline is stored in the explicit character/game-mode profile, not a shared rate profile. */
public class ProgressStore
{
    private static final String KEY = "maxingProgressStartV1";
    private static final String SKILLS = Arrays.stream(Skill.values())
        .filter(s -> !"OVERALL".equals(s.name())).map(Skill::name).sorted().collect(Collectors.joining(","));
    @Inject private ConfigManager configManager;

    public String profileFor(long accountHash)
    {
        if (accountHash == -1) { return null; }
        String key = configManager.getRSProfileKey();
        if (key == null) { return null; }
        for (RuneScapeProfile profile : configManager.getRSProfiles())
        {
            if (key.equals(profile.getKey()) && accountHash == profile.getAccountHash()) { return key; }
        }
        return null;
    }

    public Long get(String profile)
    {
        return profile == null ? null : decode(configManager.getConfiguration(MaxingTimeConfig.GROUP, profile, KEY));
    }

    public void start(String profile, long remaining)
    {
        if (profile == null) { throw new IllegalArgumentException("Character not ready"); }
        configManager.setConfiguration(MaxingTimeConfig.GROUP, profile, KEY, encode(remaining));
    }

    static String encode(long remaining)
    {
        if (remaining < 0 || remaining > (long) Skill.values().length * MaxingCalculator.TARGET_XP)
        { throw new IllegalArgumentException("Invalid baseline"); }
        return SKILLS + ":" + remaining;
    }

    static Long decode(String value)
    {
        if (value == null || !value.startsWith(SKILLS + ":")) { return null; }
        try
        {
            long result = Long.parseLong(value.substring(SKILLS.length() + 1));
            encode(result);
            return result;
        }
        catch (IllegalArgumentException invalid) { return null; }
    }
}
