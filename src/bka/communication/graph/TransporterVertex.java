/*
** Copyright © Bart Kampers
*/

package bka.communication.graph;

import bka.communication.json.*;
import bka.graph.*;


public class TransporterVertex extends Vertex {


    public Transporter getTransporter() {
        return transporter;
    }


    public void setTransporter(Transporter transporter) {
        this.transporter = transporter;
    }


    private Transporter transporter;

}
