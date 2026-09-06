package com.maxingtime;

import java.awt.*;
import java.awt.image.BufferedImage;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;

/** Small original vector drawings matching the four illustrated skills in the design. */
final class TrackerSkillIcons
{
    private final SkillIconManager fallback = new SkillIconManager();

    BufferedImage get(Skill skill)
    {
        String key = skill.name();
        if (!key.equals("AGILITY") && !key.equals("MINING") && !key.equals("SMITHING") && !key.equals("SAILING"))
        { return fallback.getSkillImage(skill); }
        BufferedImage image = new BufferedImage(30, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (key.equals("AGILITY")) {
            line(g, new int[]{14,12,15,12,5,3}, new int[]{10,18,21,26,29,28}, new Color(154,117,75), 3);
            line(g, new int[]{14,19,20,25}, new int[]{20,24,29,29}, new Color(154,117,75), 3);
            line(g, new int[]{13,8,6,8}, new int[]{11,12,17,20}, new Color(170,131,85), 3);
            line(g, new int[]{14,20,24,27}, new int[]{12,16,15,13}, new Color(170,131,85), 3);
            g.setColor(new Color(28,23,19)); g.fillOval(11,2,8,9);
            g.setColor(new Color(188,152,108)); g.fillOval(13,3,5,6);
        } else if (key.equals("MINING")) {
            line(g,new int[]{5,24},new int[]{28,6},new Color(134,105,49),3);
            poly(g,new int[]{2,7,16,23,27,28,25,23,20,15,7},new int[]{9,5,4,8,15,23,27,26,13,8,8},new Color(156,161,157));
            line(g,new int[]{5,15,21},new int[]{7,6,10},new Color(211,214,207),1);
        } else if (key.equals("SMITHING")) {
            poly(g,new int[]{2,18,19,28,28,19,16,16,21,22,6,7,12,12,7,2},new int[]{9,9,10,11,15,17,20,24,26,29,29,26,24,20,20,19},new Color(106,111,99));
            poly(g,new int[]{8,18,18,8},new int[]{9,9,19,19},new Color(139,142,127));
            line(g,new int[]{7,21},new int[]{27,27},new Color(169,171,154),1);
        } else {
            line(g,new int[]{3,11,20,28},new int[]{30,31,30,30},new Color(53,99,170),1);
            poly(g,new int[]{3,27,23,10},new int[]{25,25,30,30},new Color(142,96,37));
            poly(g,new int[]{13,13,3},new int[]{6,23,23},new Color(221,216,201));
            poly(g,new int[]{17,27,17},new int[]{5,22,22},new Color(236,230,211));
            line(g,new int[]{15,15},new int[]{2,26},new Color(145,97,41),2);
        }
        g.dispose(); return image;
    }

    private static void line(Graphics2D g, int[] x, int[] y, Color c, int width) {
        g.setStroke(new BasicStroke(width + 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(18,18,15)); g.drawPolyline(x,y,x.length);
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c); g.drawPolyline(x,y,x.length);
    }
    private static void poly(Graphics2D g, int[] x, int[] y, Color c) {
        g.setColor(c); g.fillPolygon(x,y,x.length); g.setColor(new Color(18,18,15));
        g.setStroke(new BasicStroke(1)); g.drawPolygon(x,y,x.length);
    }
}
