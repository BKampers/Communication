/*
** Copyright © Bart Kampers
*/

package bka.communication.graph;

import bka.communication.*;
import bka.communication.json.*;
import bka.graph.*;
import bka.graph.swing.*;
import java.awt.*;
import java.util.*;
import java.util.logging.*;


public class Schema extends GraphEditor {


    public Schema() {
        onLoadDelegate = () -> {
            onLoad();
        };
    }


    public static void main(String[] arguments) {
        EventQueue.invokeLater(() -> {
            Schema frame = new Schema();
            frame.setVisible(true);
        });
    }


    @Override
    protected Map<String, Class<? extends VertexPicture>> getVertexButtons() {
        HashMap<String, Class<? extends VertexPicture>> map = new HashMap<>();
        map.put("Channel", ChannelPicture.class);
        map.put("Transporter", TransporterPicture.class);
        map.put("Terminal", TerminalPicture.class);
        return map;
    }


    @Override
    protected void edgePictureAdded(DiagramComponent diagramComponent, EdgePicture edgePicture) {
        configureVertices(edgePicture.getEdge());
    }


    @Override
    protected void edgePictureRemoved(EdgePicture edgePicture) {
        Vertex terminus = edgePicture.getEdge().getTerminus();
        if (terminus instanceof TransporterVertex) {
            try {
                ((TransporterVertex) terminus).getTransporter().close();
            }
            catch (ChannelException ex) {
                Logger.getLogger(Schema.class.getName()).log(Level.WARNING, null, ex);
            }
            ((TransporterVertex) terminus).setTransporter(null);
        }
    }


    @Override
    protected java.beans.XMLEncoder createEncoder() {
        java.beans.XMLEncoder encoder = super.createEncoder();
        encoder.setPersistenceDelegate(ChannelVertex.class, ChannelVertex.PERSISTENCE_DELEGATE);
        return encoder;
    }


    private void onLoad() {
        ArrayList<Edge> edges = new ArrayList<>(allEdges());
        edges.sort((edge1, edge2) -> { return edgeCompare(edge1, edge2); });
        for (Edge edge : edges) {
            configureVertices(edge);
        }
    }


    private void configureVertices(Edge edge) {
        Vertex origin = edge.getOrigin();
        Vertex terminus = edge.getTerminus();
        if (isChannelTransporterEdge(origin, terminus)) {
            configureEdge((ChannelVertex) origin, (TransporterVertex) terminus);
        }
        else if (isTransporterTerminalEdge(origin, terminus)) {
            configureEdge((TransporterVertex) origin, (TerminalVertex) terminus);
        }
    }


    private void configureEdge(ChannelVertex channelVertex, TransporterVertex transporterVertex) {
        Channel channel = channelVertex.getChannel();
        Transporter transporter = new Transporter(channel, '#', applicationName());
        transporterVertex.setTransporter(transporter);
        try {
            channel.open(applicationName());
            transporter.open();
        }
        catch (ChannelException ex) {
            Logger.getLogger(Schema.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void configureEdge(TransporterVertex transporterVertex, TerminalVertex terminalVertex) {
        terminalVertex.setTransporter(transporterVertex.getTransporter());
    }


    private static int edgeCompare(Edge edge1, Edge edge2) {
        if (isChannelTransporterEdge(edge1) && isTransporterTerminalEdge(edge2)) {
            return -1;
        }
        if (isTransporterTerminalEdge(edge1) && isChannelTransporterEdge(edge2)) {
            return 1;
        }
        else {
            return 0;
        }
    }


    private static boolean isTransporterTerminalEdge(Edge edge) {
        return isTransporterTerminalEdge(edge.getOrigin(), edge.getTerminus());
    }


    private static boolean isTransporterTerminalEdge(Vertex origin, Vertex terminus) {
        return origin instanceof TransporterVertex && terminus instanceof TerminalVertex;
    }


    private static boolean isChannelTransporterEdge(Edge edge) {
        return isChannelTransporterEdge(edge.getOrigin(), edge.getTerminus());
    }


    private static boolean isChannelTransporterEdge(Vertex origin, Vertex terminus) {
        return origin instanceof ChannelVertex && terminus instanceof TransporterVertex;
    }

}
