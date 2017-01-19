/*
** Copyright © Bart Kampers
*/

package bka.communication.json;


import bka.communication.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import org.json.*;


public class Transporter {

    
    public Transporter(Channel channel, String applicationName) {
        this(channel, '\u0004', applicationName);
    }


    public Transporter(Channel channel, char endOfTransmission, String applicationName) {
        this.channel = channel;
        this.endOfTransmission = endOfTransmission;
        this.applicationName = applicationName;
        LOGGER.setLevel(Level.FINE);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINER);
        LOGGER.addHandler(handler);

    }
    
    
    public String getName() {
        return channel.toString();
    }
    
    
    public void open() throws ChannelException {
        channel.open(applicationName);
        objectReceiver = new ObjectReceiver();
        channel.addListener(objectReceiver);
    }
    
    
    public void close() throws ChannelException {
        receivedObjects.add(new JSONObject()); // deblock thread waiting in nextReceivedObject
        channel.removeListener(objectReceiver);
        objectReceiver = null;
        channel.close();
    }
    
    
    public void send(JSONObject message) {
        StringBuilder builder = new StringBuilder(message.toString());
        builder.append(endOfTransmission);
        Timer timer = new Timer();
        timer.schedule(new SendTask(builder.toString().getBytes()), 0, 100);
    }
    
    
    public JSONObject nextReceivedObject() throws InterruptedException {
          return receivedObjects.take();
    }


    private class SendTask extends TimerTask {

        SendTask(byte[] buffer) {
            this.buffer = buffer;
        }

        @Override
        public void run() {
            if (index < buffer.length) {
                int end = Math.min(index + SEND_BUFFER_SIZE, buffer.length);
                channel.send(Arrays.copyOfRange(buffer, index, end));
                index = end;
            }
            else {
                cancel();
            }
        }

        private final byte[] buffer;
        private int index;

    }

    
    private class ObjectReceiver implements ChannelListener {

        @Override
        public void receive(byte[] bytes) {
            LOGGER.log(Level.FINEST, "Received {0} bytes", bytes.length);
            for (int i = 0; i < bytes.length; ++i) {
                char character = (char) bytes[i];
                if (character != endOfTransmission) {
                    receivedCharacters.append(character);
                }
                else {
                    processReceivedCharacters();
                }
            }
        }

        private void processReceivedCharacters() {
            try {
                String string = receivedCharacters.toString();
                LOGGER.log(Level.FINE, ">> {0}", string);
                receivedObjects.add(new JSONObject(string));
            }
            catch (org.json.JSONException ex) {
                handleException(ex);
            }
            receivedCharacters = new StringBuilder();
        }

        @Override
        public void handleException(Exception ex) {
            LOGGER.log(Level.WARNING, ObjectReceiver.class.getName(), ex);
        }
        
        private StringBuilder receivedCharacters = new StringBuilder();
        
    }
    
    
    private final Channel channel;
    private final char endOfTransmission;
    private final String applicationName;

    private ChannelListener objectReceiver = null;
    
    private final BlockingQueue<JSONObject> receivedObjects = new LinkedBlockingQueue<>();

    private static final int SEND_BUFFER_SIZE = 48;
    private static final Logger LOGGER = Logger.getLogger(Transporter.class.getName());
    
   
}