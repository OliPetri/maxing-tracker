package com.maxingtime;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@PluginDescriptor(name = "Maxing tracker", description = "Estimate training time to 99 and max",
    tags = {"xp", "skills", "max", "planning"})
public class MaxingTimePlugin extends Plugin
{
    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private ClientToolbar toolbar;
    @Inject private XpRateStore rates;
    @Inject private ItemManager itemManager;
    @Inject private ProgressStore progressStore;
    private volatile MaxingTimePanel panel;
    private NavigationButton navigation;

    @Provides MaxingTimeConfig provideConfig(ConfigManager manager)
    {
        return manager.getConfig(MaxingTimeConfig.class);
    }

    @Override protected void startUp() throws Exception
    {
        // RuneLite starts and stops plugins on the Swing event dispatch thread.
        panel = new MaxingTimePanel(rates, this::startTracking, this::resetTracking);
        itemManager.getImage(ItemID.MAX_CAPE).addTo(panel.getCapeLabel());
        navigation = NavigationButton.builder().tooltip("Maxing tracker").icon(icon())
            .priority(7).panel(panel).build();
        toolbar.addNavigation(navigation);
        clientThread.invokeLater(this::refresh);
    }

    @Override protected void shutDown()
    {
        MaxingTimePanel old = panel;
        panel = null;
        if (navigation != null) { toolbar.removeNavigation(navigation); navigation = null; }
        if (old != null) { SwingUtilities.invokeLater(old::dispose); }
    }

    @Subscribe public void onGameTick(GameTick event) { refresh(); }
    @Subscribe public void onGameStateChanged(GameStateChanged event) { refresh(); }

    private void refresh()
    {
        MaxingTimePanel target = panel;
        if (target == null) { return; }
        Map<Skill, Integer> xp = new EnumMap<>(Skill.class);
        boolean loggedIn = client.getGameState() == GameState.LOGGED_IN;
        if (loggedIn)
        {
            for (Skill skill : Skill.values())
            {
                if (!"OVERALL".equals(skill.name())) { xp.put(skill, client.getSkillExperience(skill)); }
            }
        }
        String profile = loggedIn ? progressStore.profileFor(client.getAccountHash()) : null;
        Long baseline = progressStore.get(profile);
        SwingUtilities.invokeLater(() -> {
            if (panel == target) { target.update(xp, loggedIn, profile, baseline); }
        });
    }

    private void startTracking(String expectedProfile)
    {
        updateTracking(expectedProfile, false);
    }

    private void resetTracking(String expectedProfile)
    {
        updateTracking(expectedProfile, true);
    }

    private void updateTracking(String expectedProfile, boolean reset)
    {
        MaxingTimePanel expectedPanel = panel;
        clientThread.invokeLater(() -> {
            if (panel != expectedPanel || panel == null || client.getGameState() != GameState.LOGGED_IN) { return; }
            String profile = progressStore.profileFor(client.getAccountHash());
            if (profile == null || !profile.equals(expectedProfile)) { return; }
            Long existing = progressStore.get(profile);
            if (reset ? existing == null : existing != null) { return; }
            // Read at the click action on the client thread, not from an older Swing snapshot.
            long remaining = 0;
            for (Skill skill : Skill.values())
            {
                if (!"OVERALL".equals(skill.name())) { remaining += MaxingCalculator.remaining(client.getSkillExperience(skill)); }
            }
            progressStore.start(profile, remaining);
            refresh();
        });
    }

    private static BufferedImage icon()
    {
        BufferedImage image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(236, 183, 74));
        g.fillRect(3, 15, 4, 6); g.fillRect(10, 9, 4, 12); g.fillRect(17, 3, 4, 18);
        g.dispose();
        return image;
    }
}
