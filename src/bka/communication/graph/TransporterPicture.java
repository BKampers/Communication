/*
** Copyright © Bart Kampers
*/

package bka.communication.graph;

import bka.graph.*;
import bka.graph.swing.*;
import java.awt.*;
import java.awt.geom.*;


public class TransporterPicture extends VertexPicture {


    public TransporterPicture() {
        size = new Dimension(40, 35);
    }


    // TODO modify color by highlights
//    @Override
//    public void paint(Graphics2D g2d) {
//        g2d.setColor(((TransporterVertex) getVertex()).getTransporter() == null ? Color.GRAY : drawColor);
//        g2d.setStroke(stroke);
//        Path2D polygon = POLYGON.create(xWest(), yNorth(), size.width, size.height);
//        g2d.fill(polygon);
//    }


    @Override
    public Shape getShape() {
        return POLYGON.create(xWest(), yNorth(), size.width, size.height);
    }


    @Override
    protected void paintIcon(Graphics2D g2d, int width, int height) {
        Path2D polygon = POLYGON.create(0, 0, width, height);
        g2d.fill(polygon);
    }


    @Override
    protected void initAttachmentPoints() {
        attachmentPoints = new Point[2];
        attachmentPoints[0] = new Point(xWest() + size.width / 2, yNorth() + size.height / 2);
        attachmentPoints[1] = new Point(xEast(), yNorth() + size.height / 2);
    }


    @Override
    protected Vertex createVertex() {
        return new TransporterVertex();
    }


    private static final PolygonFactory POLYGON = new PolygonFactory(
        new float[] { 0.0f, 0.5f, 1.0f, 0.5f, 0.0f, 0.5f },
        new float[] { 0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 0.5f });

}
