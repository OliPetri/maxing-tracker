package com.maxingtime;

/** A fixed planning benchmark. Never changes in response to measured XP/hr. */
public final class TrainingMethod
{
    public static final TrainingMethod CUSTOM = new TrainingMethod("custom", "Custom XP/hr", 0, 1,
        "Enter your own expected XP/hr. Enter or leave the field to save.", "");
    public final String id, name, notes, source;
    public final long rate;
    public final int level;
    TrainingMethod(String id, String name, long rate, int level, String notes, String source)
    {
        this.id = id; this.name = name; this.rate = rate;
        this.level = level; this.notes = notes; this.source = source;
    }
    public boolean isCustom() { return "custom".equals(id); }
    @Override public String toString() { return name; }
}
