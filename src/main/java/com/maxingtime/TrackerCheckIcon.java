package com.maxingtime;

import java.awt.*;
import javax.swing.*;

/** Dark checkbox consistent across RuneLite and the local preview. */
final class TrackerCheckIcon implements Icon
{
    public int getIconWidth() { return 13; }
    public int getIconHeight() { return 13; }
    public void paintIcon(Component c, Graphics graphics, int x, int y)
    {
        Graphics2D g = (Graphics2D) graphics.create();
        g.translate(x,y);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(38,40,40)); g.fillRoundRect(0,0,12,12,3,3);
        g.setColor(new Color(104,108,103)); g.drawRoundRect(0,0,12,12,3,3);
        if (c instanceof AbstractButton && ((AbstractButton)c).isSelected()) {
            g.setColor(new Color(221,176,68));
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawPolyline(new int[]{3,5,10},new int[]{6,9,3},3);
        }
        g.dispose();
    }
}
