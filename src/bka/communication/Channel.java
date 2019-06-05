package bka.communication;


import java.util.*;

/**
 * This class sends commands through any Channel
 * and listens to the Channel for receiving responses.
 * ChannelListeners are notified when a response has arrived.
 * 
 * This class must be extended for different types of channels such as serial and 
 * parallel ports.
 */
public abstract class Channel {
    
    abstract public void open(String name) throws ChannelException;
    abstract public boolean isOpened();
    abstract public void send(byte[] bytes);
     

    public void addListener(ChannelListener listener) {
        listeners.add(listener);
    }


    public void removeListener(ChannelListener listener) {
        listeners.remove(listener);
    }

    
    public void close() throws ChannelException {
        listeners.clear();
    }

   
    protected void notifyListeners(byte[] bytes) {
        for (ChannelListener listener : listeners) {
            listener.receive(bytes);
        }
    }
    
    
    protected void notifyListeners(Exception e) {
        for (ChannelListener listener : listeners) {
            listener.handleException(e);
        }
    }


    private final Set<ChannelListener> listeners = new LinkedHashSet<>();

}