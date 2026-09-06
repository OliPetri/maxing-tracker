package com.maxingtime;
import net.runelite.api.Skill;
import org.junit.Test;
import static org.junit.Assert.*;
public class TrainingMethodsTest
{
    @Test public void everySkillHasSourcedSortedPresets()
    {
        for (Skill skill : Skill.values())
        {
            if ("OVERALL".equals(skill.name())) { continue; }
            assertTrue(skill.name(), TrainingMethods.forSkill(skill).size() > 1);
            assertTrue(TrainingMethods.forSkill(skill).get(0).isCustom());
            long previous = 0;
            for (TrainingMethod method : TrainingMethods.forSkill(skill))
            {
                assertTrue(method.rate >= previous); previous = method.rate;
                if (!method.isCustom()) { assertTrue(method.source.startsWith("https://oldschool.runescape.wiki/w/")); }
            }
        }
    }
    @Test public void afkAndIronmanChoicesAreAvailable()
    {
        assertEquals(30000, TrainingMethods.selected(Skill.MINING, "stars-90").rate);
        assertEquals(60000, TrainingMethods.selected(Skill.SAILING, "salvage-fremennik").rate);
        assertEquals(198000, TrainingMethods.selected(Skill.SMITHING, "foundry-mith-addy").rate);
        assertFalse(TrainingMethods.selected(Skill.HERBLORE, "mixology").isCustom());
        assertFalse(TrainingMethods.selected(Skill.CRAFTING, "charter-lens").isCustom());
    }

    @Test public void agilityAndCustomSailingExamples()
    {
        assertEquals(98500, TrainingMethods.selected(Skill.AGILITY, "sepulchre-5").rate);
        assertEquals(87, TrainingMethods.selected(Skill.AGILITY, "sepulchre-5").level);
        assertEquals("20h 0m", MaxingCalculator.duration(MaxingCalculator.hours(MaxingCalculator.TARGET_XP - 1000000, 50000)));
    }
    @Test public void expandedCatalogueRetainsRequestedAlternatives()
    {
        assertEquals(120000, TrainingMethods.selected(Skill.HUNTER, "stymphikes-82").rate);
        assertEquals(451143, TrainingMethods.selected(Skill.FIREMAKING, "fireline-magic").rate);
        assertEquals(232750, TrainingMethods.selected(Skill.FIREMAKING, "campfire-redwood").rate);
        assertEquals(77, TrainingMethods.selected(Skill.AGILITY, "sepulchre-4").level);
        assertTrue(TrainingMethods.selected(Skill.AGILITY, "sepulchre-5").notes.contains("84 Thieving"));
        assertEquals(60000, TrainingMethods.selected(Skill.AGILITY, "prif").rate);
        assertEquals(50000, TrainingMethods.selected(Skill.AGILITY, "werewolf").rate);
        assertTrue(TrainingMethods.selected(Skill.COOKING, "karambwan-1tick-80").rate
            > TrainingMethods.selected(Skill.COOKING, "karambwan-normal-80").rate);
        assertEquals(100000, TrainingMethods.selected(Skill.FLETCHING, "maple-long").rate);
        assertEquals(218750, TrainingMethods.selected(Skill.HERBLORE, "prayer").rate);
        assertEquals(356250, TrainingMethods.selected(Skill.HERBLORE, "super-restore").rate);
        assertFalse(TrainingMethods.selected(Skill.CRAFTING, "golem-goat").isCustom());
        assertFalse(TrainingMethods.selected(Skill.HUNTER, "goats-telegrab").isCustom());
        assertFalse(TrainingMethods.selected(Skill.HUNTER, "tecu-80").isCustom());
        assertFalse(TrainingMethods.selected(Skill.RUNECRAFT, "souls").isCustom());
        assertFalse(TrainingMethods.selected(Skill.RUNECRAFT, "lava").isCustom());
        assertFalse(TrainingMethods.selected(Skill.THIEVING, "rogues").isCustom());
        assertTrue(TrainingMethods.selected(Skill.WOODCUTTING, "bloodwood-afk").rate
            < TrainingMethods.selected(Skill.WOODCUTTING, "bloodwood").rate);
        assertTrue(TrainingMethods.selected(Skill.FISHING, "leechfin-cut").rate
            < TrainingMethods.selected(Skill.FISHING, "leechfin").rate);
    }
    @Test public void foundryRiftAndPreMaxLeechfinBenchmarks()
    {
        assertEquals(253110, TrainingMethods.selected(Skill.SMITHING, "foundry-rune-addy").rate);
        for (int level : new int[]{40, 50, 75, 85}) {
            for (String mode : new String[]{"combo", "no-combo"}) {
                TrainingMethod method = TrainingMethods.selected(Skill.RUNECRAFT, "gotr-" + mode + "-" + level);
                assertFalse(method.isCustom());
                assertEquals(level, method.level);
                assertTrue(method.notes.contains("not verified"));
            }
        }
        assertEquals(120900, TrainingMethods.selected(Skill.FISHING, "leechfin").rate);
        assertEquals(72540, TrainingMethods.selected(Skill.FISHING, "leechfin-cut").rate);
        for (TrainingMethod method : TrainingMethods.forSkill(Skill.FISHING)) {
            if (method.id.startsWith("leechfin")) { assertEquals(90, method.level); }
        }
    }
}
