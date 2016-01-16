package ssq.utils.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;

import ssq.utils.HashGenerator;
import ssq.utils.StringUtils;

/**
 * 主要使用RSAUtils的算法, 同时也使用AES, 做一个在非认证信道下, SK安全的密钥交换协议. <br>
 * 参与方保有长期私钥并公开公钥, 在开启每次会话前共同生成会话密钥. <br>
 * 同时对 MD5(消息+时间戳+消息id)签名, 保证不可否认性.<br>
 * 实体认证在对称加密用密文可解密和签名来实现, 在非对称加密用lhash来实现. 在有签名的对称加密里, 而者都有.
 *
 * @author ssqstone
 */
public class KeyExchange
{
    /**
     * 用于适应统一的加解密接口
     */
    public static final int PUBLIC_MODE     = 0;
    public static final int PRIVATE_MODE    = 1;
    public static final int CONTERPART_BASE = 2;
    
    /**
     * 支持自增的Long对象
     */
    public class MyLong
    {
        private long l;
        
        public long addOne()
        {
            return l++;
        }
    }

    /**
     * 带hash的字节数组
     */
    private class MyByteArray
    {
        private byte[] bytes;
        
        public MyByteArray(byte[] bytes)
        {
            this.bytes = bytes;
        }
        
        public byte[] getBytes()
        {
            return bytes;
        }
        
        @Override
        public boolean equals(Object byteArray)
        {
            if (byteArray == null)
            {
                return false;
            }
            else if (byteArray instanceof MyByteArray)
            {
                
                for (int i = 0; i < bytes.length; i++)
                {
                    if (((MyByteArray) byteArray).bytes[i] != this.bytes[i])
                        return false;
                }
                return true;
            }
            else if (byteArray instanceof byte[])
            {
                byte[] basicByteArray = (byte[]) byteArray;
                if (bytes.length != basicByteArray.length)
                {
                    return false;
                }
                
                for (int i = 0; i < bytes.length; i++)
                {
                    if (basicByteArray[i] != this.bytes[i])
                        return false;
                }
                return true;
            }
            return true;
        }
        
        @Override
        public int hashCode()
        {
            int i = 0;
            for (byte b : bytes)
            {
                i = (i << 8) | (i >>> 24 ^ b);
            }
            return i;
        }
    }

    /**
     * 所有参与方的n, 开头的两个是己方的n, 用于使用一致的加解密接口
     */
    private ArrayList<BigInteger>  nn            = new ArrayList<BigInteger>();
    /**
     * 所有参与方的e, 开头的两个是己方的e和d, 用于使用一致的加解密接口
     */
    private ArrayList<BigInteger>  ee            = new ArrayList<BigInteger>();
    /**
     * 一段可选的标签, 用于区分不同参与方, 作为天然且固定的id
     */
    private ArrayList<MyByteArray> lhash         = new ArrayList<MyByteArray>();
    /**
     * 己方消息的编号, 用于检测重复消息, 防止重放攻击
     */
    private ArrayList<MyLong>      count         = new ArrayList<MyLong>();
    /**
     * 对手方上次消息的时间戳, 防止重放攻击
     */
    private ArrayList<String>      lastTimeStamp = new ArrayList<String>();
    /**
     * 对手方上次的消息编号, 用于检测重复消息, 防止重放攻击
     */
    private ArrayList<String>      lastCount     = new ArrayList<String>();
    /**
     * 增加的对手方的lhash标签, 用n做hash标签
     */
    private HashSet<MyByteArray>   added         = new HashSet<MyByteArray>();
    /**
     * 缓存删除掉的对手方, 防止删除元素时数组移动, 为了保持通信对手方的编号不变和提高性能而引入
     */
    private LinkedList<Integer>    free          = new LinkedList<Integer>();
    
    /**
     * 默认的MD5 hash函数
     */
    private HashGenerator          hashGenerator = new MD5Utils();

    RSAUtils                       rsaUtils;
    
    /**
     * 为rsa部分自动生成一个密钥, 作为长期密钥. <br>
     * 这样并不好. 应该使用自己生成的密钥来初始化rsa部分
     */
    public KeyExchange()
    {
        this(RSAUtils.genKeyPair());
    }
    
    /**
     * 从文件读取密钥对
     */
    public static KeyPair loadKeyPair(String path)
    {
        try
        {
            ObjectInputStream oi = new ObjectInputStream(new FileInputStream(new File(path)));
            KeyPair result;
            result = (KeyPair) oi.readObject();
            oi.close();
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public KeyExchange(KeyPair keyPair)
    {
        rsaUtils = new RSAUtils(keyPair, hashGenerator);
        addConterpart(rsaUtils.getPublicKey().getModulus(), rsaUtils.getPublicKey().getPublicExponent());
        addConterpart(rsaUtils.getPrivateKey().getModulus(), rsaUtils.getPrivateKey().getPrivateExponent());
    }
    
    public KeyExchange(String path)
    {
        this(loadKeyPair(path));
    }
    
    public MyByteArray getLhash(int index)
    {
        return lhash.get(index);
    }
    
    public HashGenerator getHashGenerator()
    {
        return hashGenerator;
    }

    /**
     * 增加一个对手方, 需要知道其公钥和模数<br>
     * 优先填补上数组的空隙
     */
    public boolean addConterpart(BigInteger n, BigInteger e)
    {
        MyByteArray hash = new MyByteArray(hashGenerator.hash(n.toByteArray()));
        if (added.size() >= 2 && added.contains(hash))
        {
            return false;
        }

        added.add(hash);
        if (free.size() > 0)
        {
            int i = free.getFirst();
            nn.set(i, n);
            ee.set(i, e);
            lhash.set(i, hash);
            count.set(i, new MyLong());
            lastTimeStamp.set(i, "");
            lastCount.set(i, "");
            free.removeFirst();
        }
        else
        {
            nn.add(n);
            ee.add(e);
            lhash.add(hash);
            count.add(new MyLong());
            lastTimeStamp.add("");
            lastCount.add("");
        }
        return true;
    }

    /**
     * 删除一个对手方
     */
    public void deleteConterpart(int indice)
    {
        if (indice >= CONTERPART_BASE && !free.contains(Integer.valueOf(indice)))
        {
            free.add(Integer.valueOf(indice));
            added.remove(lhash.get(indice));
        }
        else
        {
            System.err.println("cannot delete yourself or a counterpart of nonexistence. ");
        }
    }

    /**
     * 用己方的d加密一段明文, 传给to
     */
    public byte[] encryptRSA(byte[] data, int to) throws Exception
    {
        return rsaUtils.encrypt(data, ee.get(KeyExchange.PRIVATE_MODE), nn.get(KeyExchange.PRIVATE_MODE), lhash.get(to).getBytes());
    }
    
    /**
     * 给定来源from, 解密一段由from的d加密的密文, 密文由一个或多个密文块组成<br>
     * 如果from<2, 是己方加密的. 用己方的非加密密钥解密(即, 用d加密, from为0, 用e加密, from为1)
     */
    public byte[] decryptRSA(byte[] data, int from) throws Exception
    {
        return rsaUtils.decrypt(data, ee.get(from), nn.get(from), lhash.get(KeyExchange.PUBLIC_MODE).getBytes());
    }
    
    /**
     * 将消息打上计数和时间标签<br>
     *
     * @return {m}@{11位时间戳}#{11位计数}
     */
    public String getLabeledMessage(String m, int to)
    {
        StringBuilder s = new StringBuilder(m);
        s.append("@").append(StringUtils.getPaddedHex(Calendar.getInstance().getTimeInMillis(), 11)).append("#").append(StringUtils.getPaddedHex(count.get(to).addOne(), 11));
        return s.toString();
    }
    
    /**
     * 为了向to发送消息而签名, 附加时间戳和计数信息<br>
     * 签名的生成: enc(d, hash({m}@{11位时间戳}#{11位计数}))
     *
     * @return {m}@{11位时间戳}#{11位计数}^{签名的base64编码}
     */
    public String signRSA(String m, int to) throws Exception
    {
        return rsaUtils.sign(getLabeledMessage(m, to), lhash.get(to).getBytes());
    }
    
    /**
     * 验证签名和消息<br>
     * 要求时间戳不早于上一个时间戳, 计数大于上一个计数, lhash标签值与from所指示的对手方一致
     */
    public boolean verifyRSA(String m, int from) throws Exception
    {
        String[] parts = m.split("^");
        String msg = parts[0];
        String hash = parts[1];
        parts = msg.split("@|#");
        
        if (parts[1].compareTo(lastTimeStamp.get(from)) < 0 || parts[2].compareTo(lastCount.get(from)) <= 0)
        {
            return false;
        }
        
        if (rsaUtils.verify(msg, hash, ee.get(from), nn.get(from), lhash.get(KeyExchange.PUBLIC_MODE).getBytes()))
        {

            lastTimeStamp.set(from, parts[1]);
            lastCount.set(from, parts[2]);
            return true;
        }
        else
        {
            return false;
        }
    }
}
