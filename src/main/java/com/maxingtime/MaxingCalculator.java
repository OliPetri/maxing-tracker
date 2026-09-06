package com.maxingtime;

import java.util.Map;
import java.util.Set;
import java.util.Collections;
import net.runelite.api.Experience;
import net.runelite.api.Skill;

public final class MaxingCalculator
{
    public static final int TARGET_XP = Experience.getXpForLevel(99);
    private MaxingCalculator() { }

    public static long remaining(int xp) { return Math.max(0L, TARGET_XP - (long) Math.max(0, xp)); }

    /** Progress uses only XP still needed to 99, never the account total XP. */
    public static long totalRemaining(Map<Skill, Integer> xp)
    {
        return xp.values().stream().mapToLong(MaxingCalculator::remaining).sum();
    }

    public static double progressPercent(long startingRemaining, long currentRemaining)
    {
        if (startingRemaining < 0 || currentRemaining < 0) { throw new IllegalArgumentException("Negative XP"); }
        if (startingRemaining == 0) { return currentRemaining == 0 ? 100 : 0; }
        return Math.max(0, Math.min(100, 100.0 * (startingRemaining - currentRemaining) / startingRemaining));
    }

    public static double overallPercent(Map<Skill, Integer> xp)
    {
        return xp.isEmpty() ? 0 : progressPercent((long) xp.size() * TARGET_XP, totalRemaining(xp));
    }

    public static double hours(int xp, long rate)
    {
        long remaining = remaining(xp);
        return remaining == 0 ? 0 : rate <= 0 ? Double.NaN : remaining / (double) rate;
    }

    public static Summary summarize(Map<Skill, Integer> xp, Map<Skill, Long> rates)
    {
        return summarize(xp, rates, Collections.emptySet());
    }

    /** Ignored skills still contribute to XP/progress, but never to hours or missing rates. */
    public static Summary summarize(Map<Skill, Integer> xp, Map<Skill, Long> rates, Set<Skill> passive)
    {
        double hours = 0;
        int missing = 0, completed = 0, ignored = 0;
        long remaining = 0;
        for (Map.Entry<Skill, Integer> entry : xp.entrySet())
        {
            long left = remaining(entry.getValue());
            remaining += left;
            if (left == 0) { completed++; }
            if (left > 0 && passive.contains(entry.getKey())) { ignored++; continue; }
            double time = hours(entry.getValue(), rates.getOrDefault(entry.getKey(), 0L));
            if (Double.isNaN(time)) { missing++; } else { hours += time; }
        }
        return new Summary(hours, missing, completed, remaining, ignored);
    }

    public static String duration(double hours)
    {
        if (!Double.isFinite(hours)) { return "Set XP/hr"; }
        long minutes = (long) Math.ceil(hours * 60);
        return (minutes / 60) + "h " + (minutes % 60) + "m";
    }

    public static final class Summary
    {
        public final double hours;
        public final int missing, completed, ignored;
        public final long remaining;
        Summary(double hours, int missing, int completed, long remaining, int ignored)
        {
            this.hours = hours; this.missing = missing;
            this.completed = completed; this.remaining = remaining; this.ignored = ignored;
        }
    }
}
