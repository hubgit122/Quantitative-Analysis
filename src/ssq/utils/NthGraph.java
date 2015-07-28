package ssq.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import ssq.utils.security.ConnectionStatus;

/**
 * n叉图
 *
 * @author ssqstone
 */
public abstract class NthGraph<T>
{
    final Class<? extends NthNode> nodeClass;
    NthNode<T>[]                   nodes;
    HashMap<T, NthNode<T>>         map = new HashMap<>();
    
    public NthGraph(Class<? extends NthNode> c, T[] nodes, Pair<T, T>[] edges)
    {
        nodeClass = c;

        setNodes(nodes);
        setEdges(edges);
    }
    
    public NthGraph(Class<? extends NthNode> c, T[] nodes)
    {
        this(c, nodes, null);
    }

    public void setNodes(T[] nodes)
    {
        if (nodes == null)
        {
            return;
        }

        this.nodes = new NthNode[nodes.length];
        for (int i = 0; i < this.nodes.length; i++)
        {
            try
            {
                NthNode<T> node = nodeClass.newInstance();
                node.setElement(nodes[i]);
                this.nodes[i] = node;
                map.put(nodes[i], node);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public int getN()
    {
        try
        {
            return (int) nodeClass.getMethod("getN", null).invoke(null, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public void setEdges(Pair<T, T>[] edges)
    {
        if (edges == null)
        {
            return;
        }
        
        for (int i = 0; i < edges.length; i++)
        {
            Pair<T, T> edge = edges[i];
            map.get(edge.getKey()).addChild(map.get(edge.getValue()));
        }
    }

    public void addEdge(Pair<T, T> edge)
    {
        map.get(edge.getKey()).addChild(map.get(edge.getValue()));
    }

    /**
     * 从一个json对象解析出所有的出入边
     *
     * @param json
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public void addEdgesFromJson(String json) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {
        addEdgesFromJson(JSONObject.fromObject(json));
    }

    public void addEdgesFromJson(JSONObject json) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {
        for (Object key : json.keySet())
        {
            NthNode<T> from = findNodeByElementName((String) key);
            
            JSONArray array = (JSONArray) json.get(key);
            
            for (int i = 0; i < array.size(); ++i)
            {
                NthNode<T> to = findNodeByElementName(array.getString(i));
                
                from.addChild(to);
            }
        }
    }
    
    public NthNode<T> findNodeByElementName(String entry) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {
        for (int i = 0; i < nodes.length; i++)
        {
            NthNode<T> node = nodes[i];

            if (node.getElement().toString().equals(entry))
            {
                return node;
            }
        }
        
        return map.get(nodeClass.getMethod("valueOf", String.class).invoke(null, entry));
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("Adjencent list of Graph:\r\n");
        for (int i = 0; i < nodes.length; i++)
        {
            NthNode<T> node = nodes[i];
            sb.append(node.toString()).append(" [").append(StringUtils.join(",", node.getChildren())).append("]\n");
        }
        return sb.toString();
    }
    
    public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {
        NthGraph<ConnectionStatus> graph = new BiGraph<ConnectionStatus>(BiNode.class, ConnectionStatus.values());

        graph.addEdgesFromJson("{"
                + "'CONNECTING':['ASK_REGISTERED','CONNECTING'], "
                + "'ASK_REGISTERED':['USE_CERT_LOG','NAME_AND_R_REG'],"
                + "'NAME_AND_R_REG':['USE_CERT_REG','NAME_AND_R_REG'],"
                + "'USE_CERT_REG':['REG_CERT','REG_PASS'],"
                + "'REG_CERT':['ASK_REGISTERED','REG_CERT'],"
                + "'REG_PASS':['ASK_REGISTERED','REG_PASS'],"
                + "'USE_CERT_LOG':['NAME_AND_R_CERT','NAME_AND_R_PASS'],"
                + "'NAME_AND_R_PASS':['CHECK_PASS','NAME_AND_R_PASS'],"
                + "'NAME_AND_R_CERT':['CHECK_R','NAME_AND_R_CERT'],"
                + "'CHECK_PASS':['LOGGED_IN','CHECK_PASS'],"
                + "'CHECK_R':['LOGGED_IN','CHECK_R'],"
                + "'LOGGED_IN':['LOGGED_IN','LOGGED_OUT'],"
                + "}");
        
        System.err.println(graph);
    }
}
