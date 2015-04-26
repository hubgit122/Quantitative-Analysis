package ssq.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class MathUtils
{
    
    public static int getNextInt(InputStream in) throws IOException
    {
        int ch = 0;
        int d = 0;
        
        for (int i = 0; i < 32; i += 8)
        {
            ch = in.read();
            if (ch < 0)
                throw new EOFException();
            
            d += ch << i;
        }
        return d;
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

            d += ch << i;
        }
        return d;
    }
    
}
