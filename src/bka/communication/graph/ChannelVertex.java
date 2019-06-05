/*
** Copyright © Bart Kampers
*/

package bka.communication.graph;

import bka.communication.*;


public class ChannelVertex extends bka.graph.Vertex {


    public static ChannelVertex create(String string) {
        ChannelVertex vertex =  new ChannelVertex();
        if (string.contains(":")) {
            String[] split = string.split(":");
            if (split.length == 2) {
                vertex.setChannel(SocketChannel.create(split[0], Integer.parseInt(split[1])));
            }
        }
        else {
            String[] split = string.split("&");
            if (split.length == 2) {
                vertex.setChannel(JsscChannel.create(split[0], Integer.parseInt(split[1])));
            }
            else {
                vertex.setChannel(JsscChannel.create(string));
            }
        }
        return vertex;
    }


    public Channel getChannel() {
        return channel;
    }


    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    private String instantiationString() {
        if (channel instanceof SocketChannel) {
            return ((SocketChannel) channel).toString();
        }
        else if (channel instanceof JsscChannel) {
            return ((JsscChannel) channel).getName() + '&' + Integer.toString(((JsscChannel) channel).getBaudrate());
        }
        else {
            return null;
        }
    }


    private Channel channel;


    public static final java.beans.PersistenceDelegate PERSISTENCE_DELEGATE = new java.beans.PersistenceDelegate() {

        @Override
        protected java.beans.Expression instantiate(java.lang.Object oldInstance, java.beans.Encoder out) {
            Class oldClass = oldInstance.getClass();
            ChannelVertex vertex = (ChannelVertex) oldInstance;
            return new java.beans.Expression(oldInstance, oldClass, "create", new java.lang.Object[] { vertex.instantiationString() });
        }

    };
}
