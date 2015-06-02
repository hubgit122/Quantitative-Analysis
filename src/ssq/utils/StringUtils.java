package ssq.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

public class StringUtils
{
    private static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    
    public static boolean noContent(String s)
    {
        return s == null || s.trim().length() == 0;
    }
    
    public static String getHex(byte[] data)
    {
        int j = data.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++)
        {
            byte byte0 = data[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
    }
    
    public static String join(String d, List<?> contents)
    {
        boolean flag = false;
        StringBuilder sb = new StringBuilder();

        for (Object o : contents)
        {
            if (flag)
            {
                sb.append(d);
            }
            else
            {
                flag = true;
            }

            sb.append(o.toString());
        }
        
        return sb.toString();
    }
    
    private static int hex2Dec(char c)
    {
        if (c <= '9')
        {
            return c - '0';
        }
        else
        {
            return c - 'a' + 10;
        }
    }
    
    /**
     *
     * @param s
     * @return
     */
    public static byte[] hexToBytes(String s)
    {
        if ((s.length() & 1) == 1)
        {
            s = "0" + s;
        }
        s = s.toLowerCase();
        
        byte[] result = new byte[s.length() >> 1];
        
        for (int i = 0; i < result.length; i++)
        {
            result[i] = (byte) (hex2Dec(s.charAt(i << 1)) << 4 | hex2Dec(s.charAt((i << 1) + 1)));
        }
        return result;
    }
    
    public static String getPaddedHex(long i, int len)
    {
        char[] str = new char[len];
        for (int j = len - 1; j >= 0; j--)
        {
            str[j] = hexDigits[(int) (i & 15)];
            i >>>= 4;
        }
        return new String(str);
    }
    
    public static byte[] encode(String s, String format) throws UnsupportedEncodingException
    {
        if (format == null)
        {
            format = "UTF-8";
        }
        return s.getBytes(format);
    }
    
    public static String pad(String s, int l, boolean addSpacesToTail)
    {
        String result;
        
        if (s.length() < l)
        {
            StringBuffer sb = new StringBuffer(l);
            
            if (addSpacesToTail)
            {
                sb.append(s);
            }
            for (int i = s.length(); i < l; i++)
            {
                sb.append(' ');
            }
            
            if (!addSpacesToTail)
            {
                sb.append(s);
            }
            result = sb.toString();
        }
        else if (s.length() > l)
        {
            result = s.substring(0, l);
        }
        else
        {
            result = s;
        }
        
        return result;
    }
    
    public static String decode(byte[] bytes, String format) throws UnsupportedEncodingException
    {
        if (format == null)
        {
            format = "UTF-8";
        }
        return new String(bytes, format);
    }
    
    public static final String changeFirstCharacterToLowerCase(String upperCaseStr)
    {
        char[] chars = new char[1];
        chars[0] = upperCaseStr.charAt(0);
        String temp = new String(chars);
        if (chars[0] >= 'A' && chars[0] <= 'Z')
        {
            upperCaseStr = upperCaseStr.replaceFirst(temp, temp.toLowerCase());
        }
        return upperCaseStr;
    }
    
    public static String convertStreamToString(InputStream is)
    {
        /*
         * To convert the InputStream to String we use the Reader.read(char[]
         * buffer) method. We iterate until the Reader return -1 which means there's
         * no more data to read. We use the StringWriter class to produce the
         * string.
         */
        if (is != null)
        {
            Writer writer = new StringWriter();
            
            char[] buffer = new char[1024];
            try
            {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1)
                {
                    writer.write(buffer, 0, n);
                }
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            
            return writer.toString();
        }
        else
        {
            return "";
        }
    }
}
