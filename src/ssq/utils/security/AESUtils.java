package ssq.utils.security;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES实用工具类<br>
 * 长度: 1024bit<br>
 * AES支持的加密模式: ECB/CBC/PCBC/CTR/CTS/CFB/CFB8 to CFB128/OFB/OBF8 to OFB128<br>
 * AES支持的填充类型: Nopadding/PKCS5Padding/ISO10126Padding/
 *
 * @author s
 *
 */
public class AESUtils
{
    /**
     * 密钥算法
     */
    private static final String KEY_ALGORITHM            = "AES";
    /**
     * 算法, 由主算法AES, 加密模式和填充方式构成
     */
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    public static final Cipher  defaultCipher            = getDefaultCipher();
    
    /**
     * 得到默认的Cipher
     */
    public static Cipher getDefaultCipher()
    {
        try
        {
            return Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 初始化密钥
     */
    public static Key genKey(int keySize, byte[] seed)
    {
        try
        {
            return KeyAndCertUtils.genKey(KEY_ALGORITHM, keySize, seed);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 用随机数初始化1024位的密钥
     */
    public static Key genKey()
    {
        return genKey(128, null);
    }

    /**
     * 转换密钥
     */
    private static Key toKey(byte[] key)
    {
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }
    
    /**
     * 加密, 不限制明文长度
     */
    public static byte[] encrypt(byte[] data, Key key) throws Exception
    {
        defaultCipher.init(Cipher.ENCRYPT_MODE, key);
        return defaultCipher.doFinal(data);
    }
    
    /**
     * 加密, 不限制明文长度
     */
    public static byte[] encrypt(byte[] data, byte[] key) throws Exception
    {
        return encrypt(data, toKey(key));
    }

    /**
     * 加密, 不限制明文长度
     */
    public static byte[] encrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception
    {
        Key k = toKey(key);
        return encrypt(data, k, cipherAlgorithm);
    }
    
    /**
     * 加密, 不限制明文长度
     */
    public static byte[] encrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception
    {
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * 解密, 不限制密文长度
     */
    public static byte[] decrypt(byte[] data, byte[] key) throws Exception
    {
        return decrypt(data, toKey(key));
    }
    
    /**
     * 解密, 不限制密文长度
     */
    public static byte[] decrypt(byte[] data, Key key) throws Exception
    {
        defaultCipher.init(Cipher.DECRYPT_MODE, key);
        return defaultCipher.doFinal(data);
    }
    
    /**
     * 解密, 不限制密文长度
     */
    public static byte[] decrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception
    {
        Key k = toKey(key);
        return decrypt(data, k, cipherAlgorithm);
    }

    /**
     * 解密, 不限制密文长度
     */
    public static byte[] decrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception
    {
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }
}