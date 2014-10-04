package bka.communication;

public class ChannelException extends Exception
{
    
    public ChannelException(Exception source) {
        this.source = source;
    }
    
    
    public Exception getSource() {
        return source;
    }
    
    
    private Exception source;

}