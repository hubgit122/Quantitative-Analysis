package ssq.utils.security;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import ssq.stock.simulator.Simulator;
import ssq.utils.Base64Utils;

public class Server extends WebSocketServer
{
    final Certificate certificate;

    public Server(InetSocketAddress address, int decoders, Certificate certificate)
    {
        super(address, decoders, Collections.singletonList(new Draft_17()), new CopyOnWriteArraySet<WebSocket>());
        this.certificate = certificate;
    }
    
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        try
        {
            conn.send(Simulator.getCommunicateJsonString("cert", Base64Utils.encode(certificate.getEncoded())));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        
    }

    @Override
    public void onMessage(WebSocket conn, String message)
    {

    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        ex.printStackTrace();
        conn.send("{ 'type':'error', 'msg':'" + ex.getLocalizedMessage() + "'}");
    }
}
