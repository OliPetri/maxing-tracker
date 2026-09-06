package com.maxingtime;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

/** Draws the control independently of the RuneScape font and platform look and feel. */
final class TrackerComboBoxUI extends BasicComboBoxUI
{
    static final Color SURFACE = new Color(46, 49, 49);

    static void install(JComboBox<?> box)
    {
        box.setUI(new TrackerComboBoxUI());
        box.setOpaque(false);
        box.setBorder(new AbstractBorder() {
            @Override public Insets getBorderInsets(Component c) { return new Insets(4, 7, 4, 4); }
            @Override public Insets getBorderInsets(Component c, Insets i) {
                i.set(4, 7, 4, 4); return i;
            }
            @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D p = (Graphics2D) g.create();
                p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                p.setColor(c.hasFocus() ? new Color(221, 176, 68) : new Color(83, 88, 88));
                p.drawRoundRect(x, y, w - 1, h - 1, 8, 8); p.dispose();
            }
        });
    }

    @Override public void paint(Graphics g, JComponent c)
    {
        Graphics2D p = (Graphics2D) g.create();
        p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        p.setPaint(new GradientPaint(0, 0, new Color(53, 56, 56), 0, c.getHeight(), SURFACE));
        p.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
        p.dispose();
        super.paint(g, c);
    }

    @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean focus) { }

    @Override protected JButton createArrowButton()
    {
        JButton arrow = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D p = (Graphics2D) g.create();
                p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                p.setColor(new Color(221, 176, 68));
                p.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int x = getWidth() / 2, y = getHeight() / 2;
                p.drawPolyline(new int[]{x - 4, x, x + 4}, new int[]{y - 2, y + 2, y - 2}, 3);
                p.dispose();
            }
        };
        arrow.setOpaque(false); arrow.setContentAreaFilled(false); arrow.setBorderPainted(false);
        arrow.setFocusable(false); arrow.setPreferredSize(new Dimension(23, 24));
        arrow.getAccessibleContext().setAccessibleName("Show training methods");
        return arrow;
    }
}
