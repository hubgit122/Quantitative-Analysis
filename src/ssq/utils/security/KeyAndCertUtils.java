package ssq.utils.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.KeyGenerator;

import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.BasicConstraintsExtension;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class KeyAndCertUtils
{

    public static void main(String[] args)
    {
        System.err.println(new SecureRandom(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 }).nextInt());
    }

    public static void main1(String[] args)
    {
        try
        {
            KeyStoreAccessor accessor = new KeyStoreAccessor("test", "testPassOfStore", "JCEKS");
            Key pvtKey = accessor.getKey("Private", "testPass".toCharArray());

            //Generate ROOT certificate
            CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen.generate(512);
            PrivateKey rootPrivateKey = keyGen.getPrivateKey();
            
            X509Certificate rootCertificate = keyGen.getSelfCertificate(new X500Name("CN=ROOT"), (long) 365 * 24 * 60 * 60);
            
            //Generate intermediate certificate
            CertAndKeyGen keyGen1 = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen1.generate(512);
            PrivateKey middlePrivateKey = keyGen1.getPrivateKey();
            
            X509Certificate middleCertificate = keyGen1.getSelfCertificate(new X500Name("CN=MIDDLE"), (long) 365 * 24 * 60 * 60);
            
            //Generate leaf certificate
            CertAndKeyGen keyGen2 = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
            keyGen2.generate(512);
            PrivateKey topPrivateKey = keyGen2.getPrivateKey();
            
            X509Certificate topCertificate = keyGen2.getSelfCertificate(new X500Name("CN=TOP"), (long) 365 * 24 * 60 * 60);

            X509Certificate[] chain = new X509Certificate[3];
            chain[0] = topCertificate;
            chain[1] = middleCertificate;
            chain[2] = rootCertificate;
            
            System.out.println(Arrays.toString(chain));
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            
            rootCertificate = signCertificate(rootCertificate, rootCertificate, rootPrivateKey, true);
            middleCertificate = signCertificate(middleCertificate, rootCertificate, rootPrivateKey, true);
            topCertificate = signCertificate(topCertificate, middleCertificate, middlePrivateKey, false);
            
            chain[0] = topCertificate;
            chain[1] = middleCertificate;
            chain[2] = rootCertificate;
            
            System.out.println(Arrays.toString(chain));
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();

            rootCertificate = signCertificate(rootCertificate, rootCertificate, rootPrivateKey, false);
            middleCertificate = signCertificate(middleCertificate, rootCertificate, rootPrivateKey, false);
            topCertificate = signCertificate(topCertificate, middleCertificate, middlePrivateKey, true);
            
            chain[0] = topCertificate;
            chain[1] = middleCertificate;
            chain[2] = rootCertificate;
            
            System.out.println(Arrays.toString(chain));

            accessor.addPrivateKeyEntry("Private", "testPass", topPrivateKey, chain);
            accessor.store("testPassOfStore");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @return 随机产生的密钥对
     */
    public static KeyPair genKeyPair(String keyAlgorithm, int keySize, byte[] seed) throws NoSuchAlgorithmException
    {
        KeyPairGenerator kg = null;
        kg = KeyPairGenerator.getInstance(keyAlgorithm);

        if (seed != null)
        {
            kg.initialize(keySize, new SecureRandom(seed));
        }
        else
        {
            kg.initialize(keySize);
        }

        return kg.generateKeyPair();
    }

    /**
     * @return 随机产生的密钥
     */
    public static Key genKey(String keyAlgorithm, int keySize, byte[] seed) throws NoSuchAlgorithmException
    {
        KeyGenerator kg = null;
        try
        {
            kg = KeyGenerator.getInstance(keyAlgorithm);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        
        if (seed != null)
        {
            kg.init(keySize, new SecureRandom(seed));
        }
        else
        {
            kg.init(keySize);
        }
        
        return kg.generateKey();
    }

    /**
     * 生成私钥及其对应的证书
     */
    public static Object[] genPrivateKeyAndCert(String keyType, String sigAlgorithm, int length, String name, long validTime) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, CertificateException, SignatureException, IOException
    {
        CertAndKeyGen keyGen = new CertAndKeyGen(keyType, sigAlgorithm);
        keyGen.generate(length);
        
        PrivateKey privateKey = keyGen.getPrivateKey();
        X509Certificate certificate = keyGen.getSelfCertificate(new X500Name("CN=" + name), validTime);
        
        return new Object[] { privateKey, certificate };
    }

    /**
     * 用issuerCertificate和issuerPrivateKey为cetrificate签名. 如果要赋予cetrificate继续签名的能力, 将isCA置为true;
     */
    public static X509Certificate signCertificate(X509Certificate cetrificate, X509Certificate issuerCertificate, PrivateKey issuerPrivateKey, boolean isCA)
    {
        try
        {
            Principal issuer = issuerCertificate.getSubjectDN();
            String issuerSigAlg = issuerCertificate.getSigAlgName();
            
            byte[] inCertBytes = cetrificate.getTBSCertificate();
            X509CertInfo info = new X509CertInfo(inCertBytes);
            info.set(X509CertInfo.ISSUER, issuer);
            
            CertificateExtensions exts = new CertificateExtensions();
            BasicConstraintsExtension bce = new BasicConstraintsExtension(isCA, -1);
            exts.set(BasicConstraintsExtension.NAME, new BasicConstraintsExtension(false, bce.getExtensionValue()));
            info.set(X509CertInfo.EXTENSIONS, exts);
            
            X509CertImpl outCert = new X509CertImpl(info);
            outCert.sign(issuerPrivateKey, issuerSigAlg);
            
            return outCert;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
}
