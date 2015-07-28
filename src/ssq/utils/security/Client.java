package ssq.utils.security;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

public class Client extends WebSocketClient
{
    public Client(URI serverUri)
    {
        super(serverUri, new Draft_17());
    }
    
    @Override
    public void onOpen(ServerHandshake handshakedata)
    {
        
    }
    
    @Override
    public void onMessage(String message)
    {
        
    }
    
    @Override
    public void onClose(int code, String reason, boolean remote)
    {
        
    }
    
    @Override
    public void onError(Exception ex)
    {
        
    }
}
