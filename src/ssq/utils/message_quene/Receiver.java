package ssq.utils.message_quene;

import java.io.Serializable;

import net.sf.json.JSONObject;

public interface Receiver extends Serializable
{
    void consume(JSONObject msg);
}
