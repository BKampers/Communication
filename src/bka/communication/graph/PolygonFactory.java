/*
** Copyright © Bart Kampers
*/

package bka.communication.graph;

import java.awt.geom.*;


public class PolygonFactory {


    PolygonFactory(float[] xPoints, float[] yPoints) {
        this.xPoints = xPoints;
        this.yPoints = yPoints;
    }


    Path2D create(int xWest, int yNorth, int width, int height) {
        int length = Math.min(xPoints.length, yPoints.length);
        Path2D.Double polygon = new Path2D.Double(Path2D.WIND_NON_ZERO, length);
        if (length > 0) {
            polygon.moveTo(xWest + xPoints[0] * width, yNorth + yPoints[0] * height);
            for (int i = 1; i < length; ++i) {
                polygon.lineTo(xWest + xPoints[i] * width, yNorth + yPoints[i] * height);
            }
        }
        return polygon;
    }


    private final float[] xPoints;
    private final float[] yPoints;

}
