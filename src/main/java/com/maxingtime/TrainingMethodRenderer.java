package com.maxingtime;

import java.awt.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import net.runelite.client.ui.FontManager;

/** Wraps long names using the actual RuneLite font metrics, also in the closed control. */
final class TrainingMethodRenderer extends JComponent implements ListCellRenderer<TrainingMethod>
{
    private TrainingMethod method;
    private boolean popup, selected;

    TrainingMethodRenderer() { setFont(FontManager.getRunescapeSmallFont()); }

    @Override public Component getListCellRendererComponent(JList<? extends TrainingMethod> list,
        TrainingMethod value, int index, boolean selected, boolean focus)
    {
        this.method = value; this.popup = index >= 0; this.selected = selected;
        setPreferredSize(new Dimension(150, popup ? 56 : 38));
        return this;
    }

    @Override protected void paintComponent(Graphics graphics)
    {
        if (method == null) { return; }
        Graphics2D g = (Graphics2D) graphics.create();
        if (popup) {
            g.setColor(selected ? new Color(62,65,61) : TrackerComboBoxUI.SURFACE);
            g.fillRect(0,0,getWidth(),getHeight());
        }
        g.setFont(FontManager.getRunescapeSmallFont());
        FontMetrics metrics = g.getFontMetrics();
        int padding = popup ? 7 : 1;
        List<String> lines = new ArrayList<>();
        String line = "";
        for (String word : method.name.split(" ")) {
            String next = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && metrics.stringWidth(next) > getWidth() - 2 * padding) {
                lines.add(line); line = word;
            } else { line = next; }
        }
        lines.add(line);
        int lineHeight = metrics.getHeight();
        int count = lines.size() + (popup && !method.isCustom() ? 1 : 0);
        int y = Math.max(0, (getHeight() - count * lineHeight) / 2) + metrics.getAscent();
        g.setColor(new Color(226,226,220));
        for (String text : lines) { g.drawString(text,padding,y); y += lineHeight; }
        if (popup && !method.isCustom()) {
            g.setColor(new Color(221,176,68));
            g.drawString(NumberFormat.getIntegerInstance(Locale.UK).format(method.rate) + " XP/hr",padding,y);
        }
        g.dispose();
    }
}
