/*
** Copyright © Bart Kampers
*/

package bka.communication.graph;

import bka.graph.*;
import bka.graph.swing.*;
import java.awt.*;
import java.awt.geom.*;


public class TerminalPicture extends VertexPicture {


    public TerminalPicture() {
        size = new Dimension(40, 35);
    }


    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(((TerminalVertex) getVertex()).getTransporter() == null ? Color.GRAY : drawColor);
        g2d.setStroke(stroke);
        Path2D polygon = POLYGON.create(xWest(), yNorth(), size.width, size.height);
        g2d.fill(polygon);
    }


    @Override
    protected void paintIcon(Graphics2D g2d, int width, int height) {
        Path2D polygon = POLYGON.create(0, 0, width, height);
        g2d.fill(polygon);
    }


    @Override
    protected void initAttachmentPoints() {
        attachmentPoints = new Point[1];
        attachmentPoints[0] = new Point(xWest() + size.width / 2, yNorth() + size.height / 2);
    }


    @Override
    public Vertex createVertex() {
        return new TerminalVertex();
    }


    @Override
    protected AbstractEditPanel getEditPanel() {
        return new JsonTerminal((TerminalVertex) getVertex());
    }


    private static final PolygonFactory POLYGON = new PolygonFactory(
        new float[] { 0.0f, 1.0f, 1.0f, 0.0f, 0.5f },
        new float[] { 0.0f, 0.0f, 1.0f, 1.0f, 0.5f });

}
