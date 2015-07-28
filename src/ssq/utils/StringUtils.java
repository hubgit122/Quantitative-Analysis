package ssq.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
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
    
    public static String join(String d, Object[] contents)
    {
        boolean flag = false;
        StringBuilder sb = new StringBuilder();
        if (contents == null)
        {
            return "";
        }

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
            
            sb.append(o);
        }
        return sb.toString();
    }
    
    public static String join(String d, List<?> contents)
    {
        return join(d, contents.toArray());
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

    public static Object join(String d, float[] vals)
    {
        return join(d, Arrays.asList(vals));
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

    public static String pad(String s, int l, char fillChar, boolean toTail)
    {
        String result;

        if (s.length() < l)
        {
            StringBuffer sb = new StringBuffer(l);

            if (toTail)
            {
                sb.append(s);
            }
            
            for (int i = s.length(); i < l; i++)
            {
                sb.append(fillChar);
            }

            if (!toTail)
            {
                sb.append(s);
            }
            result = sb.toString();
        }
        else
        {
            result = s.substring(0, l);
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

    public static String convertStreamToString(InputStream is, String encoding) throws IOException
    {
        if (encoding == null)
        {
            encoding = "UTF-8";
        }

        if (is != null)
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null)
            {
                buffer.append(line);
            }
            
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return buffer.toString();
        }
        else
        {
            return "";
        }
    }
}
