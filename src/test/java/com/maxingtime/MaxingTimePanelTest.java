package com.maxingtime;

import java.awt.Component;
import java.awt.Container;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.*;
import net.runelite.api.Skill;
import org.junit.Test;
import static org.junit.Assert.*;

public class MaxingTimePanelTest
{
    @Test public void editingAndLogout() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            MemoryStore store = new MemoryStore();
            Map<Skill, Long> saved = store.rates;
            MaxingTimePanel panel = new MaxingTimePanel(store);
            Map<Skill, Integer> xp = new EnumMap<>(Skill.class);
            xp.put(Skill.AGILITY, MaxingCalculator.TARGET_XP - 65000);
            panel.update(xp, true);
            assertTrue(contains(panel, "Missing XP/hr: 1 skills"));
            JTextField input = field(panel, "Agility XP per hour");
            assertNotNull(input);
            assertFalse(hasSlider(panel));
            input.setText("65000"); input.postActionEvent();
            assertEquals(Long.valueOf(65000), saved.get(Skill.AGILITY));
            assertTrue(contains(panel, "1h 0m"));
            input.setText("bad"); input.postActionEvent();
            assertEquals(Long.valueOf(65000), saved.get(Skill.AGILITY));
            assertTrue(contains(panel, "Use 0–1000000000, digits only"));
            panel.update(new EnumMap<>(Skill.class), false);
            assertTrue(contains(panel, "Log in to load your XP"));
            assertFalse(contains(panel, "1h 0m"));
            panel.dispose();
            input.setText("123"); input.postActionEvent();
            assertEquals(Long.valueOf(65000), saved.get(Skill.AGILITY));
        });
    }
    @Test public void completedSkillsAreAlwaysHidden() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            MaxingTimePanel panel = new MaxingTimePanel(new MemoryStore());
            Map<Skill, Integer> xp = new EnumMap<>(Skill.class);
            xp.put(Skill.ATTACK, 13508551);
            xp.put(Skill.DEFENCE, 13399337);
            xp.put(Skill.AGILITY, MaxingCalculator.TARGET_XP - 1);
            panel.update(xp, true);
            assertFalse(row(panel, Skill.ATTACK).isVisible());
            assertFalse(row(panel, Skill.DEFENCE).isVisible());
            assertTrue(row(panel, Skill.AGILITY).isVisible());
            xp.put(Skill.AGILITY, MaxingCalculator.TARGET_XP);
            panel.update(xp, true);
            assertFalse(row(panel, Skill.AGILITY).isVisible());
            assertTrue(contains(panel, "0h 0m"));
        });
    }

    @Test public void presetsPreserveCustomAcrossReopen() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            MemoryStore store = new MemoryStore(); store.set(Skill.AGILITY, 50000);
            MaxingTimePanel panel = new MaxingTimePanel(store);
            Map<Skill, Integer> xp = new EnumMap<>(Skill.class);
            xp.put(Skill.AGILITY, MaxingCalculator.TARGET_XP - 985000);
            panel.update(xp, true);
            JComboBox<?> choice = combo(row(panel, Skill.AGILITY));
            TrainingMethod method = TrainingMethods.selected(Skill.AGILITY, "sepulchre-5");
            choice.setSelectedItem(method);
            assertEquals(98500, store.effectiveRate(Skill.AGILITY));
            assertEquals(50000, store.get(Skill.AGILITY));
            assertTrue(contains(panel, "10h 0m"));
            assertFalse(field(panel, "Agility XP per hour").isEditable());
            panel.update(xp, true);
            assertEquals(98500, store.effectiveRate(Skill.AGILITY));
            MaxingTimePanel reopened = new MaxingTimePanel(store);
            assertSame(method, combo(row(reopened, Skill.AGILITY)).getSelectedItem());
            combo(row(reopened, Skill.AGILITY)).setSelectedIndex(0);
            assertEquals("custom", store.getMethod(Skill.AGILITY));
            assertEquals("50000", field(reopened, "Agility XP per hour").getText());
            assertTrue(field(reopened, "Agility XP per hour").isEditable());
            store.setMethod(Skill.AGILITY, "removed-future-method");
            reopened.update(xp, true);
            assertEquals(50000, store.effectiveRate(Skill.AGILITY));
        });
    }

    @Test public void passiveTogglePersistsWithoutChangingXpOrRates() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            MemoryStore store = new MemoryStore();
            store.set(Skill.SLAYER, 50000); store.set(Skill.MAGIC, 100000);
            Map<Skill, Integer> xp = new EnumMap<>(Skill.class);
            xp.put(Skill.SLAYER, MaxingCalculator.TARGET_XP - 1000000);
            xp.put(Skill.MAGIC, MaxingCalculator.TARGET_XP - 1000000);
            MaxingTimePanel panel = new MaxingTimePanel(store);
            panel.update(xp, true, "character", 3000000L);
            assertTrue(contains(panel, "30h 0m"));
            java.util.List<Integer> bars = barValues(panel);
            passiveBox(row(panel, Skill.MAGIC)).doClick();
            assertTrue(contains(panel, "20h 0m"));
            assertTrue(store.isPassive(Skill.MAGIC));
            assertEquals(100000, store.get(Skill.MAGIC));
            assertEquals(bars, barValues(panel));
            assertTrue(row(panel, Skill.MAGIC).isVisible());
            MaxingTimePanel reopened = new MaxingTimePanel(store);
            reopened.update(xp, true, "character", 3000000L);
            assertTrue(passiveBox(row(reopened, Skill.MAGIC)).isSelected());
            passiveBox(row(reopened, Skill.MAGIC)).doClick();
            assertTrue(contains(reopened, "30h 0m"));
            passiveBox(row(reopened, Skill.MAGIC)).doClick();
            passiveBox(row(reopened, Skill.SLAYER)).doClick();
            assertTrue(contains(reopened, "No active skills selected"));
            reopened.dispose();
            passiveBox(row(reopened, Skill.MAGIC)).doClick();
            assertTrue(store.isPassive(Skill.MAGIC));
        });
    }

    private static JCheckBox passiveBox(Container row)
    {
        for (Component c : row.getComponents()) { if (c instanceof JCheckBox) { return (JCheckBox) c; } }
        throw new AssertionError("Missing passive checkbox");
    }

    private static java.util.List<Integer> barValues(Container root)
    {
        java.util.List<Integer> values = new java.util.ArrayList<>();
        for (Component c : root.getComponents()) {
            if (c instanceof JProgressBar) { values.add(((JProgressBar)c).getValue()); }
            if (c instanceof Container) { values.addAll(barValues((Container)c)); }
        }
        return values;
    }

    static class MemoryStore extends XpRateStore
    {
        final java.util.Set<Skill> passive = java.util.EnumSet.noneOf(Skill.class);
        @Override public boolean isPassive(Skill skill) { return passive.contains(skill); }
        @Override public void setPassive(Skill skill, boolean value) { if (value) { passive.add(skill); } else { passive.remove(skill); } }
        final Map<Skill, Long> rates = new EnumMap<>(Skill.class);
        final Map<Skill, String> methods = new EnumMap<>(Skill.class);
        @Override public long get(Skill skill) { return rates.getOrDefault(skill, 0L); }
        @Override public void set(Skill skill, long value) { rates.put(skill, value); }
        @Override public String getMethod(Skill skill) { return methods.getOrDefault(skill, "custom"); }
        @Override public void setMethod(Skill skill, String method) { methods.put(skill, method); }
    }
    private static Container row(Container panel, Skill skill)
    {
        for (Component child : panel.getComponents())
        { if (skill.name().equals(child.getName())) { return (Container) child; } }
        throw new AssertionError("Missing row " + skill);
    }
    private static JComboBox<?> combo(Container row)
    {
        for (Component child : row.getComponents())
        { if (child instanceof JComboBox) { return (JComboBox<?>) child; } }
        throw new AssertionError("Missing combo");
    }
    private static boolean hasSlider(Container root)
    {
        for (Component child : root.getComponents())
        {
            if (child instanceof JSlider) { return true; }
            if (child instanceof Container && hasSlider((Container) child)) { return true; }
        }
        return false;
    }

    private static boolean contains(Container root, String text)
    {
        for (Component child : root.getComponents())
        {
            if (child instanceof JLabel && text.equals(((JLabel) child).getText())) { return true; }
            if (child instanceof Container && contains((Container) child, text)) { return true; }
        }
        return false;
    }
    private static JTextField field(Container root, String name)
    {
        for (Component child : root.getComponents())
        {
            if (child instanceof JTextField && name.equals(child.getAccessibleContext().getAccessibleName()))
            { return (JTextField) child; }
            if (child instanceof Container)
            {
                JTextField found = field((Container) child, name);
                if (found != null) { return found; }
            }
        }
        return null;
    }
}
