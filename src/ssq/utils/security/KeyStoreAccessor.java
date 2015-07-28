package ssq.utils.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import ssq.utils.DirUtils;
import ssq.utils.FileUtils;

public class KeyStoreAccessor
{
    KeyStore keyStore;
    String   type, name;

    public KeyStoreAccessor(String name, String pass, String type) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException
    {
        this.type = type;
        this.name = name;
        keyStore = getKeyStore(pass, type);
    }

    public KeyStoreAccessor(KeyStore ks)
    {
        keyStore = ks;
        type = ks.getType();
    }

    /**
     * 将名字包装成密钥库的路径
     */
    public File getKeyStoreFile(String name)
    {
        return FileUtils.assertFileExists(new File(DirUtils.getKeyRoot(), name + "." + type));
    }
    
    /**
     * 读取name对应的密钥库
     */
    public KeyStore getKeyStore(String pass, String type) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException
    {
        FileInputStream in = new FileInputStream(getKeyStoreFile(name));
        KeyStore ks = KeyStore.getInstance(type);
        try
        {
            ks.load(in, pass != null ? pass.toCharArray() : null);
        }
        catch (Exception e)
        {
            ks.load(null, null);
        }

        in.close();
        return ks;
    }

    /**
     * 添加一个私钥
     */
    public void addPrivateKeyEntry(String alias, String pass, Key k, Certificate[] chain) throws KeyStoreException
    {
        keyStore.setKeyEntry(alias, k, pass.toCharArray(), chain);
    }

    /**
     * 将更新后的密钥库储存起来
     */
    public void store(String pass) throws KeyStoreException, CertificateException, IOException
    {
        try
        {
            keyStore.store(new FileOutputStream(getKeyStoreFile(name)), pass.toCharArray());
        }
        catch (NoSuchAlgorithmException e)
        {
        }
    }

    /**
     * 将索引得到的密钥强制转换成私钥
     */
    public PrivateKey getPrivateKey(String alias, char[] password) throws UnrecoverableKeyException
    {
        return (PrivateKey) getKey(alias, password);
    }

    /**
     * 增加一个别名为alias的证书cert
     */
    public void addCertEntry(String alias, Certificate cert) throws KeyStoreException
    {
        keyStore.setCertificateEntry(alias, cert);
    }
    
    /**
     * 通过alias索引得到证书
     */
    public Certificate getCert(String alias)
    {
        try
        {
            return keyStore.getCertificate(alias);
        }
        catch (KeyStoreException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 得到别名为alias的私钥的证书链
     */
    public Certificate[] getCertChain(String alias)
    {
        try
        {
            return keyStore.getCertificateChain(alias);
        }
        catch (KeyStoreException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据alias索引一个密钥, 根据password解码
     */
    public Key getKey(String alias, char[] password) throws UnrecoverableKeyException
    {
        try
        {
            return keyStore.getKey(alias, password);
        }
        catch (KeyStoreException | NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
