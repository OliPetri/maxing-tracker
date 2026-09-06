package com.maxingtime;

import java.awt.Component;
import java.awt.Container;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import net.runelite.api.Skill;
import org.junit.Test;
import static org.junit.Assert.*;

public class MaxingProgressTest
{
    private static Map<Skill, Integer> account(int agilityRemaining)
    {
        Map<Skill, Integer> xp = new EnumMap<>(Skill.class);
        for (Skill skill : Skill.values())
        {
            if (!"OVERALL".equals(skill.name())) { xp.put(skill, MaxingCalculator.TARGET_XP); }
        }
        xp.put(Skill.AGILITY, MaxingCalculator.TARGET_XP - agilityRemaining);
        return xp;
    }

    @Test public void millionXpBaselineAdvancesByTenPercent()
    {
        assertEquals(0, MaxingCalculator.progressPercent(1000000, 1000000), 0);
        assertEquals(10, MaxingCalculator.progressPercent(1000000, 900000), 0);
        assertEquals(100, MaxingCalculator.progressPercent(1000000, 0), 0);
        assertEquals(100, MaxingCalculator.progressPercent(0, 0), 0);
        assertEquals(0, MaxingCalculator.progressPercent(100, 200), 0);
    }

    @Test public void overallUsesCappedSkillXpAndNeverTotalXp()
    {
        Map<Skill, Integer> xp = account(1000000);
        double expected = 100.0 * (1 - 1000000.0 / (xp.size() * (long) MaxingCalculator.TARGET_XP));
        assertEquals(expected, MaxingCalculator.overallPercent(xp), 0.0000001);
        xp.put(Skill.ATTACK, 200000000);
        assertEquals(expected, MaxingCalculator.overallPercent(xp), 0.0000001);
        assertEquals(1000000, MaxingCalculator.totalRemaining(xp));
        xp.replaceAll((skill, value) -> 0);
        assertEquals(0, MaxingCalculator.overallPercent(xp), 0);
        xp.replaceAll((skill, value) -> MaxingCalculator.TARGET_XP);
        assertEquals(100, MaxingCalculator.overallPercent(xp), 0);
    }

    @Test public void baselineSerializationAndInvalidData()
    {
        assertEquals(Long.valueOf(1000000), ProgressStore.decode(ProgressStore.encode(1000000)));
        assertEquals(Long.valueOf(0), ProgressStore.decode(ProgressStore.encode(0)));
        assertNull(ProgressStore.decode(null));
        assertNull(ProgressStore.decode("old-skill-list:1000000"));
        assertNull(ProgressStore.decode(ProgressStore.encode(1).replace(":1", ":-1")));
        assertNull(ProgressStore.decode(ProgressStore.encode(1).replace(":1", ":bad")));
    }

    @Test public void buttonAndBarsFollowTheCharacterAndSurviveReopen() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            AtomicReference<String> clicked = new AtomicReference<>();
            MaxingTimePanelTest.MemoryStore rates = new MaxingTimePanelTest.MemoryStore();
            MaxingTimePanel panel = new MaxingTimePanel(rates, clicked::set);
            JButton button = (JButton) find(panel, "Track my maxing progress");
            assertFalse(button.isEnabled());
            panel.update(account(1000000), true, "character-a", null);
            assertTrue(button.isEnabled()); button.doClick(); assertEquals("character-a", clicked.get());
            panel.update(account(900000), true, "character-a", 1000000L);
            JProgressBar tracked = (JProgressBar) find(panel, "Progress since tracking started");
            assertEquals("10.00%", tracked.getString()); assertFalse(button.isEnabled());
            panel.update(account(0), false, null, null);
            assertEquals("Log in to load progress", tracked.getString());
            panel.update(account(500000), true, "character-b", null);
            assertEquals("Not started", tracked.getString()); assertTrue(button.isEnabled());
            MaxingTimePanel reopened = new MaxingTimePanel(rates, clicked::set);
            reopened.update(account(800000), true, "character-a", 1000000L);
            assertEquals("20.00%", ((JProgressBar) find(reopened, "Progress since tracking started")).getString());
            reopened.update(account(1), true, "character-a", 1000000L);
            assertEquals("99.99%", ((JProgressBar) find(reopened, "Progress since tracking started")).getString());
            reopened.update(account(0), true, "character-a", 1000000L);
            assertEquals("100.00%", ((JProgressBar) find(reopened, "Progress since tracking started")).getString());
            assertEquals("100.00%", ((JProgressBar) find(reopened, "Overall maxing progress")).getString());
        });
    }

    @Test public void resetStartsAtCurrentXpWithoutChangingOverall() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            AtomicReference<String> reset = new AtomicReference<>();
            MaxingTimePanel panel = new MaxingTimePanel(new MaxingTimePanelTest.MemoryStore(), key -> { }, reset::set);
            JButton button = (JButton) find(panel, "Reset tracking");
            assertFalse(button.isEnabled());
            panel.update(account(900000), true, "character-a", null);
            assertFalse(button.isEnabled());
            panel.update(account(900000), true, "character-a", 1000000L);
            JProgressBar overall = (JProgressBar) find(panel, "Overall maxing progress");
            String before = overall.getString();
            assertTrue(button.isEnabled()); button.doClick();
            assertEquals("character-a", reset.get());
            // The client-thread action persists the freshly read remaining XP, then sends this snapshot.
            panel.update(account(900000), true, "character-a", 900000L);
            assertEquals("0.00%", ((JProgressBar) find(panel, "Progress since tracking started")).getString());
            assertEquals(before, overall.getString());
            panel.update(account(810000), true, "character-a", 900000L);
            assertEquals("10.00%", ((JProgressBar) find(panel, "Progress since tracking started")).getString());
            panel.update(account(810000), false, null, null);
            assertFalse(button.isEnabled());
        });
    }

    @Test public void searchFiltersSkillsWithoutChangingProgress() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            MaxingTimePanel panel = new MaxingTimePanel(new MaxingTimePanelTest.MemoryStore());
            panel.update(account(1000000), true, "character-a", 2000000L);
            String before = ((JProgressBar) find(panel, "Overall maxing progress")).getString();
            JTextField search = (JTextField) find(panel, "Search skills");
            search.setText("MINING");
            assertFalse(find(panel, "AGILITY").isVisible());
            assertFalse(find(panel, "MINING").isVisible()); // already 99
            search.setText("agil"); assertTrue(find(panel, "AGILITY").isVisible());
            assertEquals(before, ((JProgressBar) find(panel, "Overall maxing progress")).getString());
            search.setText(""); assertTrue(find(panel, "AGILITY").isVisible());
        });
    }

    private static Component find(Container root, String name)
    {
        for (Component child : root.getComponents())
        {
            if (name.equals(child.getName()) || (child instanceof JButton && name.equals(((JButton) child).getText()))) { return child; }
            if (child instanceof Container)
            {
                Component result = find((Container) child, name); if (result != null) { return result; }
            }
        }
        return null;
    }
}
