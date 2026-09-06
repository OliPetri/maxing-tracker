package com.maxingtime;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.runelite.api.Skill;

public final class TrainingMethods
{
    private static final Map<Skill, List<TrainingMethod>> METHODS = load();
    private TrainingMethods() { }
    public static List<TrainingMethod> forSkill(Skill skill) { return METHODS.get(skill); }
    public static TrainingMethod selected(Skill skill, String id)
    {
        return forSkill(skill).stream().filter(m -> m.id.equals(id)).findFirst().orElse(TrainingMethod.CUSTOM);
    }
    private static Map<Skill, List<TrainingMethod>> load()
    {
        Map<Skill, List<TrainingMethod>> result = new EnumMap<>(Skill.class);
        for (Skill skill : Skill.values()) { result.put(skill, new ArrayList<>()); }
        try (InputStream stream = TrainingMethods.class.getResourceAsStream("training-methods.tsv"))
        {
            if (stream == null) { throw new IllegalStateException("Missing training method catalogue"); }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (line.startsWith("#") || line.isEmpty()) { continue; }
                    String[] f = line.split("\t", -1);
                    if (f.length != 7) { throw new IllegalStateException("Invalid method row"); }
                    Skill skill;
                    try { skill = Skill.valueOf(f[0]); }
                    catch (IllegalArgumentException absentInOlderApi) { continue; }
                    long rate = XpRateStore.parse(f[3]); int level = Integer.parseInt(f[4]);
                    if (rate == 0 || level < 1 || level > 99 || !f[6].startsWith("https://oldschool.runescape.wiki/w/"))
                    { throw new IllegalStateException("Invalid training benchmark"); }
                    if (result.get(skill).stream().anyMatch(m -> m.id.equals(f[1])))
                    { throw new IllegalStateException("Duplicate method ID"); }
                    result.get(skill).add(new TrainingMethod(f[1], f[2], rate, level, f[5], f[6]));
                }
            }
        }
        catch (IOException ex) { throw new IllegalStateException("Cannot read training methods", ex); }
        result.replaceAll((skill, list) -> {
            list.sort(Comparator.comparingLong(m -> m.rate));
            list.add(0, TrainingMethod.CUSTOM);
            return Collections.unmodifiableList(list);
        });
        return Collections.unmodifiableMap(result);
    }
}
