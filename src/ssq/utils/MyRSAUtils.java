package ssq.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * 完全符合PCKS#1 v2.1 中的加密方案, 实例化所用的默认Hash函数是MD5, l为对方公钥的指数, 表明接收人
 * 
 * @author ssqstone
 *
 */
public class MyRSAUtils
{
    /**
     * 以8元组计的模数长度
     */
    private static final int ModulusLength   = 128;
    public static final int  PUBLIC_MODE     = 0;
    public static final int  PRIVATE_MODE    = 1;
    public static final int  CONTERPART_BASE = 2;
    
    private class MyLong
    {
        private long l;
        
        public long addOne()
        {
            return l++;
        }
    }
    
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
                for (int i = 0; i < bytes.length; i++)
                {
                    if (((byte[]) byteArray)[i] != this.bytes[i])
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
    
    private int                    MaxBlockSize;
    public RSAPublicKey            publicKey;
    private RSAPrivateKey          privateKey;
    private ArrayList<BigInteger>  nn            = new ArrayList<BigInteger>();
    private ArrayList<BigInteger>  ee            = new ArrayList<BigInteger>();
    private ArrayList<MyByteArray> lhash         = new ArrayList<MyByteArray>();
    private ArrayList<MyLong>      count         = new ArrayList<MyLong>();
    
    private ArrayList<String>      lastTimeStamp = new ArrayList<String>();
    private ArrayList<String>      lastCount     = new ArrayList<String>();
    private LinkedList<Integer>    free          = new LinkedList<Integer>();
    private HashGenerator          hashGenerator = new HashGenerator(16, -1)
                                                 {
                                                     @Override
                                                     public byte[] hash(byte[] m)
                                                     {
                                                         return MD5Utils.getMD5Bytes(m);
                                                     }
                                                 };
    private HashSet<MyByteArray>   added         = new HashSet<MyByteArray>();
    private static final String    KEY_ALGORITHM = "RSA";
    
    public MyRSAUtils()
    {
        this(null);
    }
    
    public MyRSAUtils(HashGenerator hashGenerator)
    {
        if (hashGenerator != null)
        {
            this.hashGenerator = hashGenerator;
        }
        KeyPair keyPair = genKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        BigInteger n = publicKey.getModulus();
        BigInteger e = publicKey.getPublicExponent();
        BigInteger d = privateKey.getPrivateExponent();
        MaxBlockSize = ModulusLength - 2 * this.hashGenerator.length - 2;
        addConterpart(n, e);
        addConterpart(n, d);
    }
    
    public static KeyPair genKeyPair()
    {
        KeyPairGenerator keyPairGen = null;
        try
        {
            keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        keyPairGen.initialize(ModulusLength << 3);
        return keyPairGen.generateKeyPair();
    }
    
    public static KeyPair loadKeys(String fileName) throws Exception
    {
        if (StrUtils.noContent(fileName))
        {
            fileName = "RSAkeys";
        }
        FileInputStream fis = new FileInputStream(DirUtils.getKeyRoot() + File.separator + fileName);
        ObjectInputStream oos = new ObjectInputStream(fis);
        KeyPair keyPair = (KeyPair) oos.readObject();
        oos.close();
        fis.close();
        return keyPair;
    }
    
    public void storeKeyPair(String fileName) throws Exception
    {
        if (StrUtils.noContent(fileName))
        {
            fileName = "RSAkeys";
        }
        FileOutputStream fos = new FileOutputStream(DirUtils.getKeyRoot() + File.separator + fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        // 生成密钥
        oos.writeObject(new KeyPair(publicKey, privateKey));
        oos.close();
        fos.close();
    }
    
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
    
    public void deleteConterpart(int mode) throws Exception
    {
        if (mode >= CONTERPART_BASE && !free.contains(Integer.valueOf(mode)))
        {
            free.add(Integer.valueOf(mode));
            added.remove(lhash.get(mode));
        }
        else
        {
            throw new Exception("cannot delete yourself");
        }
    }
    
    public byte[] doPubulic(BigInteger m) throws Exception
    {
        return doModPow(m, ee.get(PUBLIC_MODE), nn.get(PUBLIC_MODE));
    }
    
    public byte[] doPrivate(BigInteger m) throws Exception
    {
        return doModPow(m, ee.get(PRIVATE_MODE), nn.get(PRIVATE_MODE));
    }
    
    public byte[] doModPow(BigInteger m, BigInteger e, BigInteger n) throws Exception
    {
        if (m.compareTo(n) >= 0)
        {
            throw new Exception("too large");
        }
        return m.modPow(e, n).toByteArray();
    }
    
    /**
     * 掩码生成函数
     * 
     * @param data
     * @return
     * @throws Exception
     */
    public byte[] maskGen(byte[] seed, int len, int off, int srclen)
    {
        int k = len / hashGenerator.length;
        byte[] result = new byte[len];
        byte[] x = new byte[srclen + 4];
        try
        {
            System.arraycopy(seed, off, x, 0, srclen);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        int offset = 0;
        int i;
        for (i = 0; i < k; ++i)
        {
            byte[] c = i2osp(i, 4);
            System.arraycopy(c, 0, x, srclen, 4);
            byte[] md5 = hashGenerator.hash(x);
            
            System.arraycopy(md5, 0, result, offset, hashGenerator.length);
            offset += hashGenerator.length;
        }
        if (offset < len)
        {
            byte[] c = i2osp(i, 4);
            System.arraycopy(c, 0, x, srclen, 4);
            byte[] md5 = hashGenerator.hash(x);
            
            System.arraycopy(md5, 0, result, offset, len - offset);
        }
        return result;
    }
    
    public byte[] maskGen(byte[] seed, int len)
    {
        return maskGen(seed, len, 0, seed.length);
    }
    
    private byte[] i2osp(int i, int len)
    {
        return i2osp((long) i, len);
    }
    
    public byte[] i2osp(long i, int len)
    {
        byte[] result = new byte[len];
        
        for (int j = 0; j < len; j++)
        {
            result[j] = (byte) i;
            i >>>= 8;
        }
        
        return result;
    }
    
    //    private byte[] i2osp(byte[] i, int len)
    //    {
    //        byte[] result = new byte[len];
    //        
    //        for (int j = 0; j < len && j <= i.length; j++)
    //        {
    //            result[j] = i[i.length - j];
    //        }
    //        
    //        return result;
    //    }
    
    private byte[] trimToK(byte[] data)
    {
        if (data.length == ModulusLength)
        {
            return data;
        }
        else if (data.length > ModulusLength)
        {
            byte[] result = new byte[ModulusLength];
            System.arraycopy(data, data.length - ModulusLength, result, 0, ModulusLength);
            return result;
        }
        else
        {
            byte[] result = new byte[ModulusLength];
            System.arraycopy(data, 0, result, ModulusLength - data.length, data.length);
            return result;
        }
    }
    
    public byte[] encryptBlock(byte[] data, int offset, int len, BigInteger e, BigInteger n, byte[] lhash) throws Exception
    {
        // 初始化加密块, 默认全置0 {1'b0, h' maskedSeed, h' lhash, k-mlen-2h-2' ps, 1'b1, len'm}
        byte[] em = new byte[ModulusLength];
        System.arraycopy(lhash, 0, em, hashGenerator.length + 1, hashGenerator.length);
        em[ModulusLength - len - 1] = 1;
        System.arraycopy(data, offset, em, ModulusLength - len, len);
        
        byte[] seed = hashGenerator.hash(String.valueOf(SecureRandom.getInstance("SHA1PRNG").nextInt()).getBytes());
        byte[] dbMask = maskGen(seed, ModulusLength - hashGenerator.length - 1);
        ArrayUtils.xorNoCopy(em, dbMask, hashGenerator.length + 1, dbMask.length);
        byte[] seedMask = maskGen(em, hashGenerator.length, hashGenerator.length + 1, ModulusLength - 1 - hashGenerator.length);
        ArrayUtils.xorNoCopy(seed, seedMask);
        System.arraycopy(seed, 0, em, 1, hashGenerator.length);
        
        byte[] result = trimToK(doModPow(new BigInteger(em), e, n));
        return result;
    }
    
    public byte[] decryptBlock(byte[] data, int offset, BigInteger e, BigInteger n, byte[] lhash) throws Exception
    {
        // {1'b0, h' maskedSeed, {h' lhash, k-mlen-2h-2' ps, 1'b1, len'm}}
        byte[] c = Arrays.copyOfRange(data, offset, offset + ModulusLength);
        byte[] em = trimToK(doModPow(new BigInteger(1, c), e, n));
        byte[] seedMask = maskGen(em, hashGenerator.length, hashGenerator.length + 1, ModulusLength - hashGenerator.length - 1);
        ArrayUtils.xorNoCopy(em, seedMask, 1, hashGenerator.length);
        byte[] dbMask = maskGen(em, ModulusLength - hashGenerator.length - 1, 1, hashGenerator.length);
        ArrayUtils.xorNoCopy(em, dbMask, hashGenerator.length + 1, ModulusLength - hashGenerator.length - 1);
        
        for (int i = 0; i < hashGenerator.length; i++)
        {
            if (em[hashGenerator.length + 1 + i] != lhash[i])
                throw new Exception("data invalid");
        }
        
        int i = (hashGenerator.length << 1) + 1;
        for (; em[i] == 0; i++)
        {
        }
        if (em[i++] != 1)
        {
            throw new Exception("data invalid");
        }
        byte[] result = new byte[ModulusLength - i];
        System.arraycopy(em, i, result, 0, ModulusLength - i);
        return result;
    }
    
    public byte[] decrypt(byte[] data, int from) throws Exception
    {
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedOutputStream bo = new BufferedOutputStream(out);
        int offSet = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0)
        {
            try
            {
                bo.write(decryptBlock(data, offSet, ee.get(from), nn.get(from), lhash.get(PUBLIC_MODE).getBytes()));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            offSet += ModulusLength;
        }
        bo.flush();
        byte[] decryptedData = out.toByteArray();
        bo.close();
        return decryptedData;
    }
    
    public byte[] encrypt(byte[] data, int to) throws Exception
    {
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedOutputStream bo = new BufferedOutputStream(out);
        int offSet = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0)
        {
            bo.write(encryptBlock(data, offSet, Math.min(inputLen - offSet, MaxBlockSize), ee.get(PRIVATE_MODE), nn.get(PRIVATE_MODE), lhash.get(to).getBytes()));
            offSet += MaxBlockSize;
        }
        bo.flush();
        byte[] encryptedData = out.toByteArray();
        bo.close();
        return encryptedData;
    }
    
    public String sign(String m, int to) throws Exception
    {
        StringBuilder s = new StringBuilder(m);
        s.append("@").append(StrUtils.getPaddedHex(Calendar.getInstance().getTimeInMillis(), 11)).append("#").append(StrUtils.getPaddedHex(count.get(to).addOne(), 11));
        byte[] h = hashGenerator.hash(s.toString().getBytes());
        byte[] sign = encryptBlock(h, 0, h.length, ee.get(PRIVATE_MODE), nn.get(PRIVATE_MODE), lhash.get(to).getBytes());
        s.append("^").append(Base64Utils.encode(sign).toString());
        return s.toString();
    }
    
    public boolean verify(String m, int from) throws Exception
    {
        String[] parts = m.split("^");
        String msg = parts[0];
        String hash = parts[1];
        parts = msg.split("@|#");
        
        if (parts[1].compareTo(lastTimeStamp.get(from)) < 0 || parts[2].compareTo(lastCount.get(from)) <= 0)
        {
            return false;
        }
        byte[] h = decryptBlock(Base64Utils.decode(hash), 0, ee.get(from), nn.get(from), lhash.get(PUBLIC_MODE).getBytes());
        byte[] restored = hashGenerator.hash(msg.getBytes());
        
        for (int i = 0; i < h.length; i++)
        {
            if (h[i] != restored[i])
                return false;
        }
        lastTimeStamp.set(from, parts[1]);
        lastCount.set(from, parts[2]);
        return true;
    }
    
    public static RSAPublicKey generateRSAPublicKey(byte[] modulus, byte[] publicExponent) throws InvalidKeySpecException
    {
        BigInteger m = new BigInteger(modulus), e = new BigInteger(publicExponent);
        return generateRSAPublicKey(m, e);
    }
    
    public static RSAPublicKey generateRSAPublicKey(BigInteger m, BigInteger e) throws InvalidKeySpecException
    {
        KeyFactory keyFac = null;
        try
        {
            keyFac = KeyFactory.getInstance(KEY_ALGORITHM);
        }
        catch (NoSuchAlgorithmException ex)
        {
            ex.printStackTrace();
        }
        
        RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(m, e);
        return (RSAPublicKey) keyFac.generatePublic(pubKeySpec);
    }
    
    public static RSAPrivateKey generateRSAPrivateKey(byte[] modulus, byte[] privateExponent) throws InvalidKeySpecException
    {
        BigInteger m = new BigInteger(modulus), d = new BigInteger(privateExponent);
        return generateRSAPrivateKey(m, d);
    }
    
    public static RSAPrivateKey generateRSAPrivateKey(BigInteger m, BigInteger d) throws InvalidKeySpecException
    {
        KeyFactory keyFac = null;
        try
        {
            keyFac = KeyFactory.getInstance(KEY_ALGORITHM);
        }
        catch (NoSuchAlgorithmException ex)
        {
            ex.printStackTrace();
        }
        
        RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(m, d);
        return (RSAPrivateKey) keyFac.generatePrivate(priKeySpec);
    }
    
    public static PublicKey getPubKey(String pubKey)
    {
        return getPubKey(pubKey.getBytes(), true);
    }
    
    public static PublicKey getPubKey(byte[] pubKey, boolean base64)
    {
        if (base64)
        {
            pubKey = Base64Utils.decode(pubKey);
        }
        PublicKey publicKey = null;
        try
        {
            java.security.spec.X509EncodedKeySpec bobPubKeySpec = new java.security.spec.X509EncodedKeySpec(pubKey);
            // RSA对称加密算法
            java.security.KeyFactory keyFactory;
            keyFactory = java.security.KeyFactory.getInstance(KEY_ALGORITHM);
            // 取公钥匙对象
            publicKey = keyFactory.generatePublic(bobPubKeySpec);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (InvalidKeySpecException e)
        {
            e.printStackTrace();
        }
        return publicKey;
    }
    
    public static PrivateKey getPrivateKey(String priKey)
    {
        return getPrivateKey(priKey.getBytes(), true);
    }
    
    public static PrivateKey getPrivateKey(byte[] priKey, boolean base64)
    {
        if (base64)
        {
            priKey = Base64Utils.decode(priKey);
        }
        PrivateKey privateKey = null;
        PKCS8EncodedKeySpec priPKCS8;
        try
        {
            priPKCS8 = new PKCS8EncodedKeySpec(priKey);
            KeyFactory keyf = KeyFactory.getInstance(KEY_ALGORITHM);
            privateKey = keyf.generatePrivate(priPKCS8);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (InvalidKeySpecException e)
        {
            e.printStackTrace();
        }
        return privateKey;
    }
    
    public static String getKeyString(Key key) throws Exception
    {
        byte[] keyBytes = key.getEncoded();
        String s = Base64Utils.encode(keyBytes);
        return s;
    }
    
    public byte[] encryptBlock(byte[] bytes) throws Exception
    {
        return encryptBlock(bytes, 0, bytes.length, ee.get(PUBLIC_MODE), nn.get(PUBLIC_MODE), lhash.get(PUBLIC_MODE).getBytes());
    }
    
    public byte[] decryptBlock(byte[] en) throws Exception
    {
        return decryptBlock(en, 0, ee.get(PRIVATE_MODE), nn.get(PRIVATE_MODE), lhash.get(PUBLIC_MODE).getBytes());
    }
}
