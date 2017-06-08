/*
** Copyright © Bart Kampers
*/

package bka.communication.graph;

import bka.graph.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class ChannelPicture extends VertexPicture {

    
    public ChannelPicture() {
        size = new Dimension(40, 35);
    }


    @Override
    protected Shape buildShape() {
        return POLYGON.create(xWest(), yNorth(), size.width, size.height);
    }


    @Override
    protected void paintIcon(Graphics2D g2d, int width, int height) {
        Path2D polygon = POLYGON.create(0, 0, width, height);
        g2d.fill(polygon);
    }


    @Override
    protected void initAttachmentPoints() {
        attachmentPoints = new Point[1];
        attachmentPoints[0] = new Point(xEast(), yNorth() + size.height / 2);
    }


    @Override
    protected bka.graph.Vertex createVertex() {
        return new ChannelVertex();
    }


    @Override
    protected AbstractEditPanel getEditPanel() {
        return new ChannelView((ChannelVertex) getVertex());
    }


    // TODO modify color by highlights
//    private Color getColor() {
//        Channel channel = ((ChannelVertex) getVertex()).getChannel();
//        if (channel != null) {
//            return (channel.isOpened()) ? OPEN_COLOR : Color.RED;
//        }
//        else {
//            return drawColor;
//        }
//
//    }


    private static final PolygonFactory POLYGON = new PolygonFactory(
        new float[] { 0.0f, 0.5f, 1.0f, 0.5f, 0.0f },
        new float[] { 0.0f, 0.0f, 0.5f, 1.0f, 1.0f });


    private static final Color OPEN_COLOR = new Color(0x31B505);
}
