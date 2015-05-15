package ssq.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class MathUtils
{

    public static int readLittleEndianInt(InputStream in) throws IOException
    {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();

        if ((ch1 | ch2 | ch3 | ch4) < 0)
        {
            throw new IOException();
        }

        return (ch1 | (ch2 << 8) | (ch3 << 16) | (ch4 << 24));
    }
    
    public static int round(float f)
    {
        return (int) (f + 0.5f);
    }

    public static int round(double d)
    {
        return (int) (d + 0.5d);
    }
    
    public static long getNextLong(InputStream in) throws IOException
    {
        int ch = 0;
        long d = 0;
        
        for (int i = 0; i < 64; i += 8)
        {
            ch = in.read();
            if (ch < 0)
                throw new EOFException();
            
            d |= ch << i;
        }
        return d;
    }

}
