package com.maxingtime;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicProgressBarUI;
import net.runelite.client.ui.FontManager;
import net.runelite.api.Experience;
import net.runelite.api.Skill;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.LinkBrowser;

/** EDT-only view of client-thread snapshots. Rates are always chosen by the user. */
public class MaxingTimePanel extends PluginPanel
{
    private static final Color BACKGROUND = new Color(38, 38, 38);
    private static final Color CARD = new Color(31, 31, 31);
    private static final Color BORDER = new Color(67, 70, 70);
    private static final Color GOLD = new Color(221, 176, 68);
    private static final Color TEXT = new Color(226, 226, 220);
    private final JTextField search = new JTextField();
    private final JLabel empty = new JLabel();
    private final JLabel cape = new JLabel();
    private final TrackerSkillIcons skillIcons = new TrackerSkillIcons();
    private final XpRateStore store;
    private final Map<Skill, Row> rows = new EnumMap<>(Skill.class);
    private final JLabel total = new JLabel("Log in to load your XP");
    private final JLabel detail = new JLabel(" ");
    private final JLabel passiveDetail = label("");
    private final JLabel progress = new JLabel(" ");
    private final NumberFormat numbers = NumberFormat.getIntegerInstance(Locale.UK);
    private Map<Skill, Integer> xp = new EnumMap<>(Skill.class);
    private boolean loggedIn, disposed;
    private String characterProfile;
    private Long baseline;
    private final JProgressBar overallBar = progressBar("Overall maxing progress");
    private final JProgressBar trackedBar = progressBar("Progress since tracking started");
    private final JLabel trackedDetail = new JLabel();
    private final JButton trackButton = new JButton("Track my maxing progress");
    private final Consumer<String> startTracking;
    private final Consumer<String> resetTracking;
    private final JButton resetButton = new JButton("Reset tracking");


    public MaxingTimePanel(XpRateStore store)
    {
        this(store, profile -> { });
    }

    public MaxingTimePanel(XpRateStore store, Consumer<String> startTracking)
    {
        this(store, startTracking, profile -> { });
    }

    public MaxingTimePanel(XpRateStore store, Consumer<String> startTracking, Consumer<String> resetTracking)
    {
        this.store = store;
        this.startTracking = startTracking;
        this.resetTracking = resetTracking;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        for (JLabel text : new JLabel[]{total, detail, progress, trackedDetail}) {
            text.setAlignmentX(Component.LEFT_ALIGNMENT); text.setForeground(TEXT); text.setFont(FontManager.getRunescapeSmallFont());
        }
        JPanel header = card();
        JPanel heading = new JPanel(new BorderLayout(2, 0)); heading.setOpaque(false);
        JLabel title = label("MAXING TRACKER"); title.setFont(FontManager.getRunescapeBoldFont());
        cape.setPreferredSize(new Dimension(30, 32));
        cape.setToolTipText("Max cape"); cape.getAccessibleContext().setAccessibleName("Max cape");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.add(title, BorderLayout.CENTER); heading.add(cape, BorderLayout.EAST);
        header.add(heading);
        header.add(label(html("Your journey to 99 in every skill.")));
        header.add(Box.createVerticalStrut(9));
        header.add(label("Estimated time to max"));
        total.setForeground(new Color(239, 224, 180)); total.setFont(FontManager.getRunescapeBoldFont());
        header.add(total); header.add(detail); header.add(progress); header.add(passiveDetail);
        passiveDetail.setForeground(GOLD);
        passiveDetail.setToolTipText("Manually ignored time. XP still counts toward max; passive training may not cover all remaining XP.");
        add(header); gap();
        JPanel overall = card(); overall.add(label("Overall · 0 XP to max")); overall.add(Box.createVerticalStrut(5)); overall.add(overallBar);
        add(overall); gap();
        JPanel tracked = card(); tracked.add(label("Since tracking started")); tracked.add(Box.createVerticalStrut(5)); tracked.add(trackedBar);
        tracked.add(Box.createVerticalStrut(5)); tracked.add(trackedDetail); add(tracked); gap();
        JPanel actions = new JPanel(new BorderLayout(4, 0)); actions.setOpaque(false);
        styleButton(trackButton); styleButton(resetButton);
        trackButton.setText("<html>Track my maxing<br>progress</html>");
        trackButton.setName("Track my maxing progress");
        trackButton.getAccessibleContext().setAccessibleName("Track my maxing progress");
        resetButton.setText("Reset"); resetButton.setName("Reset tracking");
        resetButton.setToolTipText("Restart tracking from current XP. Overall progress is unchanged.");
        actions.add(trackButton, BorderLayout.CENTER); actions.add(resetButton, BorderLayout.EAST);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT); add(actions); gap();
        resetButton.addActionListener(event -> {
            if (!disposed && loggedIn && characterProfile != null && baseline != null && !xp.isEmpty())
            { resetTracking.accept(characterProfile); }
        });
        trackButton.addActionListener(event -> {
            if (!disposed && loggedIn && characterProfile != null && baseline == null && !xp.isEmpty())
            { startTracking.accept(characterProfile); }
        });
        search.setName("Search skills"); search.getAccessibleContext().setAccessibleName("Search skills");
        search.setToolTipText("Search skills by name");
        styleInput(search);
        JPanel searchBox = new JPanel(new BorderLayout(5, 0)); searchBox.setOpaque(false);
        searchBox.add(label("Search"), BorderLayout.WEST); searchBox.add(search, BorderLayout.CENTER);
        searchBox.setAlignmentX(Component.LEFT_ALIGNMENT); searchBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 27));
        add(searchBox); add(Box.createVerticalStrut(5)); add(label("Level 99 skills hidden")); gap();
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterRows(); }
            public void removeUpdate(DocumentEvent e) { filterRows(); }
            public void changedUpdate(DocumentEvent e) { filterRows(); }
        });
        List<Skill> skills = new ArrayList<>(Arrays.asList(Skill.values()));
        skills.removeIf(skill -> "OVERALL".equals(skill.name()));
        skills.sort(Comparator.comparing(Skill::getName));
        for (Skill skill : skills)
        {
            Row row = new Row(skill); rows.put(skill, row); add(row);
        }
        add(empty);
        render();
    }

    public void update(Map<Skill, Integer> snapshot, boolean loggedIn)
    {
        update(snapshot, loggedIn, null, null);
    }

    public void update(Map<Skill, Integer> snapshot, boolean loggedIn, String characterProfile, Long baseline)
    {
        this.characterProfile = characterProfile;
        this.baseline = baseline;
        this.xp = new EnumMap<>(Skill.class); this.xp.putAll(snapshot);
        this.loggedIn = loggedIn;
        render();
    }

    /** ItemManager loads the actual game sprite asynchronously. */
    public JLabel getCapeLabel() { return cape; }

    private void gap() { add(Box.createVerticalStrut(7)); }
    private static JLabel label(String text)
    {
        JLabel label = new JLabel(text); label.setForeground(TEXT);
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setAlignmentX(Component.LEFT_ALIGNMENT); return label;
    }
    private static JPanel card()
    {
        JPanel panel = new JPanel(); panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT); panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(7, 7, 7, 7)));
        return panel;
    }
    private static void styleButton(JButton button)
    {
        button.setBackground(new Color(46, 49, 49)); button.setForeground(TEXT);
        button.setFont(FontManager.getRunescapeSmallFont()); button.setFocusPainted(false);
        button.setMargin(new Insets(5, 4, 5, 4));
    }
    private static void styleInput(JTextField input)
    {
        input.setBackground(CARD); input.setForeground(TEXT); input.setCaretColor(TEXT);
        input.setFont(FontManager.getRunescapeSmallFont());
        input.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(4, 5, 4, 5)));
    }
    private void filterRows()
    {
        String query = search.getText().trim().toLowerCase(Locale.ROOT);
        int visible = 0;
        for (Map.Entry<Skill, Row> entry : rows.entrySet())
        {
            Integer current = xp.get(entry.getKey());
            boolean completed = loggedIn && current != null && MaxingCalculator.remaining(current) == 0;
            boolean show = !completed && entry.getKey().getName().toLowerCase(Locale.ROOT).contains(query);
            entry.getValue().setVisible(show); if (show) { visible++; }
        }
        empty.setForeground(TEXT); empty.setText(query.isEmpty() ? "All skills are level 99!" : "No matching skills");
        empty.setVisible(visible == 0);
        revalidate(); repaint();
    }

    public void dispose() { disposed = true; }

    private static String html(String text)
    {
        return "<html><body style='width: 140px'>" + text.replace("&", "&amp;")
            .replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>") + "</body></html>";
    }

    private void render()
    {
        Map<Skill, Long> rates = new EnumMap<>(Skill.class);
        Set<Skill> passive = EnumSet.noneOf(Skill.class);
        for (Map.Entry<Skill, Row> entry : rows.entrySet())
        {
            Skill skill = entry.getKey(); Row row = entry.getValue();
            long rate = store.effectiveRate(skill); rates.put(skill, rate);
            if (store.isPassive(skill)) { passive.add(skill); }
            row.sync(rate);
            Integer current = xp.get(skill);
            boolean available = loggedIn && current != null;
            long left = available ? MaxingCalculator.remaining(current) : 0;

            row.current.setText(available ? "Current XP: " + numbers.format(current) : "Current XP: —");
            row.remaining.setText(available ? numbers.format(left) + " XP to 99" : "XP to 99: —");
            row.time.setText(available && (rate > 0 || left == 0) ? MaxingCalculator.duration(MaxingCalculator.hours(current, rate)) : "—");
            if (passive.contains(skill)) {
                row.time.setToolTipText("Standalone estimate: " + (available ? MaxingCalculator.duration(MaxingCalculator.hours(current, rate)) : "Log in") + ". Excluded from total.");
                row.time.setText("Passive");
            } else { row.time.setToolTipText(null); }
            TrainingMethod method = TrainingMethods.selected(skill, store.getMethod(skill));
            boolean below = available && !method.isCustom() && Experience.getLevelForXp(current) < method.level;
            row.benchmark.setText(html(method.isCustom() ? "Your saved training rate" : "Benchmark level: " + method.level + (below ? " (above yours)" : "")));
            row.benchmark.setForeground(GOLD); row.benchmark.setVisible(below);
        }
        if (!loggedIn)
        {
            total.setText("Log in to load your XP"); detail.setText(" "); progress.setText(" "); passiveDetail.setVisible(false);
        }
        else
        {
            MaxingCalculator.Summary sum = MaxingCalculator.summarize(xp, rates, passive);
            total.setText(sum.ignored > 0 && sum.ignored + sum.completed == xp.size() ? "No active skills selected" : MaxingCalculator.duration(sum.hours));
            passiveDetail.setText(sum.ignored + " passive skills: time excluded");
            passiveDetail.setVisible(sum.ignored > 0);
            total.setToolTipText(sum.missing == 0 ? "Estimated time to max" : "Known subtotal; some rates are missing");
            detail.setText(sum.missing == 0 ? (sum.ignored == 0 ? "All unfinished skills have a rate" : "Active skills have a rate") : "Missing XP/hr: " + sum.missing + " skills");
            progress.setText((xp.size() - sum.completed) + " skills remaining");
            progress.setToolTipText(numbers.format(sum.remaining) + " XP left");
        }
        renderProgress();
        filterRows();
    }

    private static JProgressBar progressBar(String name)
    {
        JProgressBar bar = new JProgressBar(0, 10000);
        bar.setName(name); bar.getAccessibleContext().setAccessibleName(name);
        bar.setStringPainted(true); bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        bar.setPreferredSize(new Dimension(205, 22));
        bar.setForeground(name.startsWith("Overall") ? new Color(109, 153, 62) : GOLD);
        bar.setBackground(new Color(43, 43, 43));
        bar.setBorder(BorderFactory.createLineBorder(BORDER));
        bar.setFont(FontManager.getRunescapeSmallFont());
        bar.setUI(new BasicProgressBarUI() {
            @Override protected Color getSelectionForeground() { return TEXT; }
            @Override protected Color getSelectionBackground() { return TEXT; }
        });
        return bar;
    }

    private static void showPercent(JProgressBar bar, double percent)
    {
        // Truncate display so 100% is reserved for zero remaining XP.
        int hundredths = (int) Math.floor(percent * 100);
        bar.setValue(hundredths);
        bar.setString(String.format(Locale.UK, "%.2f%%", hundredths / 100.0));
    }

    private void renderProgress()
    {
        boolean available = loggedIn && !xp.isEmpty();
        trackButton.setEnabled(available && characterProfile != null && baseline == null);
        resetButton.setEnabled(available && characterProfile != null && baseline != null);
        if (!available)
        {
            overallBar.setValue(0); overallBar.setString("Log in to load XP");
            trackedBar.setValue(0); trackedBar.setString("Log in to load progress");
            trackedDetail.setText(" ");
            return;
        }
        long remaining = MaxingCalculator.totalRemaining(xp);
        showPercent(overallBar, MaxingCalculator.overallPercent(xp));
        if (baseline == null)
        {
            trackedBar.setValue(0); trackedBar.setString("Not started");
            trackedDetail.setText(characterProfile == null ? html("Waiting for character profile…") : html("Start from your current XP."));
        }
        else
        {
            showPercent(trackedBar, MaxingCalculator.progressPercent(baseline, remaining));
            long earned = Math.max(0, Math.min(baseline, baseline - remaining));
            trackedDetail.setText(html(numbers.format(earned) + " / " + numbers.format(baseline) + " XP gained towards max"));
        }
    }

    private final class Row extends JPanel
    {
        private final Skill skill;
        private final List<TrainingMethod> methods;
        private final JComboBox<TrainingMethod> choice;
        private final JLabel methodName = new JLabel();
        private final JLabel benchmark = new JLabel();
        private final JButton wiki = new JButton("Wiki / conditions");
        private final JTextField input = new JTextField(9);
        private final JLabel rateLabel = label("");
        private final JPanel editor = new JPanel(new BorderLayout(4, 0));
        private final JLabel current = new JLabel("Current XP: —");
        private final JLabel remaining = new JLabel("XP to 99: —");
        private final JLabel time = new JLabel("Time: —");
        private final JCheckBox passive = new JCheckBox("Passive / ignore time");
        private final JLabel error = new JLabel(" ");
        private boolean invalid, syncing;

        Row(Skill skill)
        {
            this.skill = skill;
            setName(skill.name());
            methods = TrainingMethods.forSkill(skill);
            choice = new JComboBox<>(methods.toArray(new TrainingMethod[0]));
            choice.setPrototypeDisplayValue(TrainingMethod.CUSTOM);
            choice.setMaximumRowCount(8);
            choice.setRenderer(new TrainingMethodRenderer());
            choice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            choice.setPreferredSize(new Dimension(180, 48));
            choice.setAlignmentX(Component.LEFT_ALIGNMENT);
            choice.getAccessibleContext().setAccessibleName(skill.getName() + " training method");
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT); setBackground(CARD);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 7, 0, BACKGROUND),
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(7, 7, 7, 7))));
            JPanel heading = new JPanel(new BorderLayout(8, 0)); heading.setOpaque(false);
            JLabel icon = new JLabel(new ImageIcon(skillIcons.get(skill)));
            icon.setPreferredSize(new Dimension(30, 34)); icon.setToolTipText(skill.getName());
            heading.add(icon, BorderLayout.WEST);
            JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            JLabel name = label(skill.getName()); name.setFont(FontManager.getRunescapeBoldFont());
            remaining.setFont(FontManager.getRunescapeSmallFont()); remaining.setForeground(TEXT);
            text.add(name); text.add(remaining); heading.add(text, BorderLayout.CENTER);
            heading.setAlignmentX(Component.LEFT_ALIGNMENT); add(heading); add(Box.createVerticalStrut(6));
            choice.setBackground(new Color(46, 49, 49)); choice.setForeground(TEXT);
            choice.setFont(FontManager.getRunescapeSmallFont());
            TrackerComboBoxUI.install(choice);
            choice.setFont(FontManager.getRunescapeSmallFont());
            add(choice); add(benchmark); add(Box.createVerticalStrut(6));
            JPanel bottom = new JPanel(new BorderLayout(4, 0)); bottom.setOpaque(false);
            editor.setOpaque(false); styleInput(input); input.setColumns(5); input.setMinimumSize(new Dimension(40, 24)); input.setPreferredSize(new Dimension(65, 24));
            editor.add(input, BorderLayout.CENTER); editor.add(label("XP/hr"), BorderLayout.EAST);
            JPanel rateBox = new JPanel(new BorderLayout()); rateBox.setOpaque(false);
            rateBox.add(editor, BorderLayout.CENTER); rateBox.add(rateLabel, BorderLayout.SOUTH);
            bottom.add(rateBox, BorderLayout.CENTER);
            time.setFont(FontManager.getRunescapeSmallFont()); time.setForeground(TEXT);
            bottom.add(time, BorderLayout.EAST); bottom.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(bottom); add(error);
            passive.setIcon(new TrackerCheckIcon());
            passive.setSelectedIcon(new TrackerCheckIcon());
            passive.setName(skill.getName() + " passive");
            passive.getAccessibleContext().setAccessibleName(skill.getName() + " passive / ignore time");
            passive.setOpaque(false); passive.setForeground(TEXT); passive.setFont(FontManager.getRunescapeSmallFont());
            passive.setAlignmentX(Component.LEFT_ALIGNMENT);
            passive.setToolTipText("Exclude this skill's hours. XP and both progress bars still count it. Rates are kept.");
            passive.addActionListener(event -> {
                if (!syncing && !disposed) { store.setPassive(skill, passive.isSelected()); render(); }
            });
            add(Box.createVerticalStrut(4)); add(passive);
            input.setMaximumSize(new Dimension(100, 26));
            choice.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent event) {
                    if (SwingUtilities.isRightMouseButton(event)) {
                        TrainingMethod method = TrainingMethods.selected(skill, store.getMethod(skill));
                        if (!method.isCustom()) { LinkBrowser.browse(method.source); }
                    }
                }
            });
            error.setForeground(new Color(255, 145, 125));
            error.setVisible(false);
            input.setToolTipText("Custom: digits only; 0 or blank means unknown. Enter or leave to save.");
            input.getAccessibleContext().setAccessibleName(skill.getName() + " XP per hour");
            input.addActionListener(event -> saveCustom());
            input.addFocusListener(new FocusAdapter() {
                @Override public void focusLost(FocusEvent event) { saveCustom(); }
            });
            choice.addActionListener(event -> {
                if (!syncing) { select(choice.getSelectedIndex()); }
            });
            wiki.addActionListener(event -> {
                if (disposed) { return; }
                TrainingMethod method = TrainingMethods.selected(skill, store.getMethod(skill));
                if (!method.isCustom()) { LinkBrowser.browse(method.source); }
            });
        }

        private void select(int index)
        {
            if (disposed || index < 0) { return; }
            store.setMethod(skill, methods.get(index).id);
            invalid = false; error.setVisible(false);
            // Force a refresh even if the custom input still owns focus during selection.
            input.setText(store.effectiveRate(skill) == 0 ? "" : Long.toString(store.effectiveRate(skill)));
            render();
        }

        private void saveCustom()
        {
            if (disposed || !TrainingMethods.selected(skill, store.getMethod(skill)).isCustom()) { return; }
            try
            {
                store.set(skill, XpRateStore.parse(input.getText()));
                invalid = false; error.setVisible(false); render();
            }
            catch (IllegalArgumentException ex)
            {
                invalid = true; error.setText("Use 0–1000000000, digits only"); error.setVisible(true);
                revalidate();
            }
        }

        private void sync(long rate)
        {
            TrainingMethod method = TrainingMethods.selected(skill, store.getMethod(skill));
            syncing = true;
            passive.setSelected(store.isPassive(skill));
            choice.setSelectedItem(method);
            methodName.setText(html(method.name));
            syncing = false;
            input.setEditable(method.isCustom());
            editor.setVisible(method.isCustom()); rateLabel.setVisible(!method.isCustom());
            rateLabel.setText(numbers.format(rate) + " XP/hr");
            choice.setToolTipText(html(method.name + "\n" + method.notes + "\nRight-click to open Wiki source."));
            if ((!input.hasFocus() && !invalid) || !method.isCustom())
            {
                String text = rate == 0 ? "" : Long.toString(rate);
                if (!input.getText().equals(text)) { input.setText(text); }
            }
            wiki.setVisible(!method.isCustom());
            wiki.setToolTipText(html(method.notes + "\nSource: OSRS Wiki. Snapshot checked 2026-09-06."));
            methodName.setToolTipText(html(method.notes));
        }
    }
}
