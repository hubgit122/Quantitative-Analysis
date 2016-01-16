package ssq.utils.security;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import ssq.utils.ArrayUtils;
import ssq.utils.Base64Utils;
import ssq.utils.HashGenerator;

/**
 * 完全依照PCKS#1 v2.1 中的OAEP(最佳非对称加密填充)两素数加密方案编程, 各种标示符都完全一致<br>
 * 实例化所用的默认Hash函数是MD5<br>
 * 提供利用RSA进行基本通信的方法, 包括加密/解密, 签名/验证
 *
 * @author ssqstone
 *
 */
public class RSAUtils
{
    /**
     * 以8元组计的模数长度, 也就是说, 由本变量计算加密长度, 要乘以8
     */
    private static final int    ModulusSize = 128;
    
    /**
     * 消息过长, 需要分段时, 最大的段长度, 按比特记
     */
    private int                 MaxBlockSize;

    /**
     * 己方公钥
     */
    private final RSAPublicKey  publicKey;

    /**
     * 己方私钥
     */
    private final RSAPrivateKey privateKey;
    
    /**
     * 默认使用MD5作为hash函数
     */
    private HashGenerator       hashGenerator = new MD5Utils();

    /**
     * 加密算法是RSA
     */
    private static final String KEY_ALGORITHM = "RSA";

    /**
     * 使用默认的MD5作为hash函数, 用内部随机数生成公私钥
     */
    public RSAUtils()
    {
        this(null);
    }
    
    /**
     * 指定hash函数, 用内部随机数生成公私钥
     */
    public RSAUtils(HashGenerator hashGenerator)
    {
        this(genKeyPair(), hashGenerator);
    }

    /**
     * 指定hash函数, 用场外公私钥对初始化
     */
    public RSAUtils(KeyPair keyPair, HashGenerator hashGenerator)
    {
        if (hashGenerator != null)
        {
            this.hashGenerator = hashGenerator;
        }
        
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();

        MaxBlockSize = ModulusSize - 2 * this.hashGenerator.length - 2;
    }

    /**
     * @return 随机产生的RSA密钥对
     */
    public static KeyPair genKeyPair()
    {
        return genKeyPair(ModulusSize, null);
    }
    
    public static KeyPair genKeyPair(int keySize, byte[] seed)
    {
        try
        {
            return KeyAndCertUtils.genKeyPair(KEY_ALGORITHM, keySize, seed);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    protected RSAPublicKey getPublicKey()
    {
        return publicKey;
    }

    protected RSAPrivateKey getPrivateKey()
    {
        return privateKey;
    }

    /**
     * 用公钥作指数计算模幂
     */
    public byte[] doPubulic(BigInteger m) throws Exception
    {
        return doModPow(m, publicKey.getPublicExponent(), publicKey.getModulus());
    }

    /**
     * 用私钥作指数计算模幂
     */
    public byte[] doPrivate(BigInteger m) throws Exception
    {
        return doModPow(m, privateKey.getPrivateExponent(), privateKey.getModulus());
    }

    /**
     * 指定指数和模, 计算模幂
     */
    public static byte[] doModPow(BigInteger m, BigInteger e, BigInteger n) throws Exception
    {
        if (m.compareTo(n) >= 0)
        {
            throw new Exception("too large");
        }
        return m.modPow(e, n).toByteArray();
    }

    /**
     * 掩码生成函数, 把srclen字节长的, 从srcOff开始的src中的字节, 通过hash函数延拓为resultLen字节长
     */
    public byte[] maskGen(byte[] src, int resultLen, int srcOff, int srclen)
    {
        int k = resultLen / hashGenerator.length;
        byte[] result = new byte[resultLen];
        byte[] x = new byte[srclen + 4];
        try
        {
            System.arraycopy(src, srcOff, x, 0, srclen);
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
        if (offset < resultLen)
        {
            byte[] c = i2osp(i, 4);
            System.arraycopy(c, 0, x, srclen, 4);
            byte[] md5 = hashGenerator.hash(x);

            System.arraycopy(md5, 0, result, offset, resultLen - offset);
        }
        return result;
    }

    /**
     * 掩码生成函数, 把srclen字节长的, 从0开始的src中的字节, 通过hash函数延拓为resultLen字节长
     */
    public byte[] maskGen(byte[] src, int resultLen)
    {
        return maskGen(src, resultLen, 0, src.length);
    }
    
    /**
     * 把一个int转换为一个长度指定的字节串
     */
    public static byte[] i2osp(int i, int len)
    {
        return i2osp((long) i, len);
    }
    
    /**
     * 把一个long转换为一个长度指定的字节串
     */
    public static byte[] i2osp(long i, int len)
    {
        byte[] result = new byte[len];

        for (int j = 0; j < len; j++)
        {
            result[j] = (byte) i;
            i >>>= 8;
        }

        return result;
    }

    //    private static byte[] i2osp(byte[] i, int len)
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

    /**
     * 将data截断或者填充到ModulusLength长度
     */
    private static byte[] trimToModulusLength(byte[] data)
    {
        if (data.length == ModulusSize)
        {
            return data;
        }
        else if (data.length > ModulusSize)
        {
            byte[] result = new byte[ModulusSize];
            System.arraycopy(data, data.length - ModulusSize, result, 0, ModulusSize);
            return result;
        }
        else
        {
            byte[] result = new byte[ModulusSize];
            System.arraycopy(data, 0, result, ModulusSize - data.length, data.length);
            return result;
        }
    }

    /**
     * 加密块, 密文结构是: {1'b0, h' maskedSeed, h' lhash, (k-mLen-2h-2)' ps, 1'b1, mLen'm}<br>
     * 解密与加密函数的区别不在于使用不同的指数, 而在于步骤. 加密需要附上接收者的lhash, 解密时应该验证lhash, 以确定发送者所期望的双方身份.
     */
    public byte[] encryptBlock(byte[] data, int offset, int mLen, BigInteger e, BigInteger n, byte[] lhash) throws Exception
    {
        // 初始化加密块, 默认全置0, 避免了填充ps
        byte[] em = new byte[ModulusSize];
        System.arraycopy(lhash, 0, em, hashGenerator.length + 1, hashGenerator.length);
        em[ModulusSize - mLen - 1] = 1;
        System.arraycopy(data, offset, em, ModulusSize - mLen, mLen);

        byte[] seed = hashGenerator.hash(String.valueOf(SecureRandom.getInstance("SHA1PRNG").nextInt()).getBytes());
        byte[] dbMask = maskGen(seed, ModulusSize - hashGenerator.length - 1);
        ArrayUtils.xorNoCopy(em, dbMask, hashGenerator.length + 1, dbMask.length);
        byte[] seedMask = maskGen(em, hashGenerator.length, hashGenerator.length + 1, ModulusSize - 1 - hashGenerator.length);
        ArrayUtils.xorNoCopy(seed, seedMask);
        System.arraycopy(seed, 0, em, 1, hashGenerator.length);

        byte[] result = trimToModulusLength(doModPow(new BigInteger(1, em), e, n));
        return result;
    }

    /**
     * 解密块, 密文结构是: {1'b0, h' maskedSeed, h' lhash, (k-mLen-2h-2)' ps, 1'b1, mLen'm}<br>
     * 解密与加密函数的区别不在于使用不同的指数, 而在于步骤. 加密需要附上接收者的lhash, 解密时应该验证lhash, 以确定发送者所期望的双方身份.
     */
    public byte[] decryptBlock(byte[] data, int offset, BigInteger e, BigInteger n, byte[] lhash) throws Exception
    {
        byte[] c = Arrays.copyOfRange(data, offset, offset + ModulusSize);
        byte[] em = trimToModulusLength(doModPow(new BigInteger(1, c), e, n));
        byte[] seedMask = maskGen(em, hashGenerator.length, hashGenerator.length + 1, ModulusSize - hashGenerator.length - 1);
        ArrayUtils.xorNoCopy(em, seedMask, 1, hashGenerator.length);
        byte[] dbMask = maskGen(em, ModulusSize - hashGenerator.length - 1, 1, hashGenerator.length);
        ArrayUtils.xorNoCopy(em, dbMask, hashGenerator.length + 1, ModulusSize - hashGenerator.length - 1);

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
        byte[] result = new byte[ModulusSize - i];
        System.arraycopy(em, i, result, 0, ModulusSize - i);
        return result;
    }

    /**
     * 解密整个密文<br>
     * 解密与加密函数的区别不在于使用不同的指数, 而在于步骤. 加密需要附上接收者的lhash, 解密时应该验证lhash, 以确定发送者所期望的双方身份.
     */
    public byte[] decrypt(byte[] data, BigInteger e, BigInteger n, byte[] lhash)
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
                bo.write(decryptBlock(data, offSet, e, n, lhash));
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            offSet += ModulusSize;
        }
        try
        {
            bo.flush();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        byte[] decryptedData = out.toByteArray();
        try
        {
            bo.close();
        }
        catch (IOException e1)
        {
        }
        return decryptedData;
    }

    /**
     * 加密一段明文<br>
     * 可以用别人的公钥加密, 也可以用自己的公钥和私钥加密, 要加上参数指定幂和模<br>
     * 解密与加密函数的区别不在于使用不同的指数, 而在于步骤. 加密需要附上接收者的lhash, 解密时应该验证lhash, 以确定发送者所期望的双方身份.
     */
    public byte[] encrypt(byte[] data, BigInteger e, BigInteger n, byte[] lhash) throws Exception
    {
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedOutputStream bo = new BufferedOutputStream(out);
        int offSet = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0)
        {
            bo.write(encryptBlock(data, offSet, Math.min(inputLen - offSet, MaxBlockSize), e, n, lhash));
            offSet += MaxBlockSize;
        }
        bo.flush();
        byte[] encryptedData = out.toByteArray();
        bo.close();
        return encryptedData;
    }

    /**
     * 为m签名<br>
     * 签名一定是用自己的私钥, 所以不加e和n这两个参数<br>
     * 结构: {m}^{签名的base64编码}<br>
     */
    public String sign(String m, byte[] lhash) throws Exception
    {
        StringBuilder s = new StringBuilder(m);
        byte[] h = hashGenerator.hash(m.getBytes());
        byte[] sign = encryptBlock(h, 0, h.length, privateKey.getPrivateExponent(), privateKey.getModulus(), lhash);
        s.append("^").append(Base64Utils.encode(sign).toString());
        return s.toString();
    }

    /**
     * 验证签名<br>
     */
    public boolean verify(String msg, String sig, BigInteger e, BigInteger n, byte[] lhash) throws Exception
    {
        byte[] h = decryptBlock(Base64Utils.decode(sig), 0, e, n, lhash);
        byte[] restored = hashGenerator.hash(msg.getBytes());
        
        for (int i = 0; i < h.length; i++)
        {
            if (h[i] != restored[i])
                return false;
        }
        return true;
    }

    /**
     * 给出m和e, 生成程序内部的RSA公钥格式
     */
    public static RSAPublicKey getRSAPublicKey(byte[] modulus, byte[] publicExponent) throws InvalidKeySpecException
    {
        BigInteger m = new BigInteger(1, modulus), e = new BigInteger(1, publicExponent);
        return getRSAPublicKey(m, e);
    }

    /**
     * 给出m和e, 生成程序内部的RSA公钥格式
     */
    public static RSAPublicKey getRSAPublicKey(BigInteger m, BigInteger e) throws InvalidKeySpecException
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

    /**
     * 给出m和d, 生成程序内部的RSA私钥格式
     */
    public static RSAPrivateKey getRSAPrivateKey(byte[] modulus, byte[] privateExponent) throws InvalidKeySpecException
    {
        BigInteger m = new BigInteger(1, modulus), d = new BigInteger(1, privateExponent);
        return getRSAPrivateKey(m, d);
    }

    /**
     * 给出m和d, 生成程序内部的RSA私钥格式
     */
    public static RSAPrivateKey getRSAPrivateKey(BigInteger m, BigInteger d) throws InvalidKeySpecException
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

    /**
     * 从base64编码的X509证书得到公钥
     */
    public static PublicKey getRSAPublicKey(String pubKey)
    {
        return getRSAPublicKey(pubKey.getBytes(), true);
    }

    /**
     * 从X509证书的base64编码的字节数组或未被编码的字节数组得到公钥
     */
    public static RSAPublicKey getRSAPublicKey(byte[] pubKey, boolean base64)
    {
        if (base64)
        {
            pubKey = Base64Utils.decode(pubKey);
        }
        RSAPublicKey publicKey = null;
        try
        {
            X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(pubKey);
            KeyFactory keyFactory;
            keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            publicKey = (RSAPublicKey) keyFactory.generatePublic(bobPubKeySpec);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return publicKey;
    }

    /**
     * 从base64编码的PKCS8私钥得到内部格式的私钥
     */
    public static PrivateKey getRSAPrivateKey(String priKey)
    {
        KeyStore ks;
        try
        {
            //读取keystore文件到KeyStore对象
            FileInputStream in = new FileInputStream("d:/.keystore");
            ks = KeyStore.getInstance("JKS");
            ks.load(in, "123456".toCharArray());
            in.close();

            //从keystore中读取证书和私钥
            String alias = "orbitca"; // 记录的别名
            String pswd = "111111"; // 记录的访问密码
            Certificate cert = ks.getCertificate(alias);
            PublicKey publicKey = cert.getPublicKey();
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, pswd.toCharArray());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return getRSAPrivateKey(priKey.getBytes(), true);
    }

    /**
     * 从PKCS8私钥的base64编码的字节数组或未被编码的字节数组得到私钥
     */
    public static RSAPrivateKey getRSAPrivateKey(byte[] priKey, boolean base64)
    {
        if (base64)
        {
            priKey = Base64Utils.decode(priKey);
        }
        RSAPrivateKey privateKey = null;
        PKCS8EncodedKeySpec priPKCS8;
        try
        {
            priPKCS8 = new PKCS8EncodedKeySpec(priKey);
            KeyFactory keyf = KeyFactory.getInstance(KEY_ALGORITHM);
            privateKey = (RSAPrivateKey) keyf.generatePrivate(priPKCS8);
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

    /**
     * 将公钥或私钥用base64编码
     */
    public static String getKeyString(Key key) throws Exception
    {
        byte[] keyBytes = key.getEncoded();
        String s = Base64Utils.encode(keyBytes);
        return s;
    }
}
