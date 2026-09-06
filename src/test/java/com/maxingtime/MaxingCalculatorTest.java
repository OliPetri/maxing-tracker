package com.maxingtime;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.Skill;
import org.junit.Test;
import static org.junit.Assert.*;
public class MaxingCalculatorTest
{
    @Test public void targetAndOver99()
    {
        assertEquals(13034431, MaxingCalculator.TARGET_XP);
        assertEquals(0, MaxingCalculator.remaining(200000000));
        assertEquals(0, MaxingCalculator.hours(200000000, 0), 0);
    }
    @Test public void exampleAndUnknownRate()
    {
        assertEquals(8934431, MaxingCalculator.remaining(4100000));
        assertEquals(8934431.0 / 65000, MaxingCalculator.hours(4100000, 65000), 0.000001);
        assertTrue(Double.isNaN(MaxingCalculator.hours(0, 0)));
    }
    @Test public void subtotalDoesNotPretendToBeComplete()
    {
        Map<Skill, Integer> xp = new EnumMap<>(Skill.class);
        Map<Skill, Long> rates = new EnumMap<>(Skill.class);
        xp.put(Skill.AGILITY, 12969431); rates.put(Skill.AGILITY, 65000L);
        xp.put(Skill.MINING, 0); xp.put(Skill.ATTACK, 200000000);
        MaxingCalculator.Summary s = MaxingCalculator.summarize(xp, rates);
        assertEquals(1, s.missing); assertEquals(1, s.completed); assertEquals(1, s.hours, 0);
    }
    @Test public void displayRoundsOnlyAfterSum()
    {
        assertEquals("1h 0m", MaxingCalculator.duration(1));
        assertEquals("0h 1m", MaxingCalculator.duration(0.00001));
        assertEquals("0h 0m", MaxingCalculator.duration(0));
    }
    @Test public void rateParsing()
    {
        assertEquals(0, XpRateStore.parse("")); assertEquals(65000, XpRateStore.parse("65000"));
        for (String bad : new String[]{"-1", "65,000", "NaN", "1.5", "1000000001", "999999999999999999999"})
        {
            try { XpRateStore.parse(bad); fail(bad); } catch (IllegalArgumentException expected) { }
        }
    }
    @org.junit.Test public void passiveSkillsOnlyExcludeTimeAndMissingRates()
    {
        java.util.Map<net.runelite.api.Skill, Integer> xp = new java.util.EnumMap<>(net.runelite.api.Skill.class);
        xp.put(net.runelite.api.Skill.SLAYER, MaxingCalculator.TARGET_XP - 1000000);
        xp.put(net.runelite.api.Skill.MAGIC, MaxingCalculator.TARGET_XP - 500000);
        xp.put(net.runelite.api.Skill.ATTACK, MaxingCalculator.TARGET_XP + 500000);
        java.util.Map<net.runelite.api.Skill, Long> rates = new java.util.EnumMap<>(net.runelite.api.Skill.class);
        rates.put(net.runelite.api.Skill.SLAYER, 50000L);
        MaxingCalculator.Summary summary = MaxingCalculator.summarize(xp, rates,
            java.util.EnumSet.of(net.runelite.api.Skill.MAGIC, net.runelite.api.Skill.ATTACK));
        org.junit.Assert.assertEquals(20, summary.hours, 0.0001);
        org.junit.Assert.assertEquals(0, summary.missing);
        org.junit.Assert.assertEquals(1, summary.ignored);
        org.junit.Assert.assertEquals(1, summary.completed);
        org.junit.Assert.assertEquals(1500000, summary.remaining);
        org.junit.Assert.assertEquals(1, MaxingCalculator.summarize(xp, rates).missing);
    }
}
