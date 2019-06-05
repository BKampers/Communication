/*
** Copyright © Bart Kampers
*/

package bka.communication.graph;

import bka.communication.json.*;


public class TerminalVertex extends bka.graph.Vertex {


    void setTransporter(Transporter transporter) {
        this.transporter = transporter;
    }


    Transporter getTransporter() {
        return transporter;
    }


    private Transporter transporter;


}
