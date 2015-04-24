package ssq.utils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPUtils
{
    static Map<String, String> extToContentType = new HashMap<String, String>();
    static
    {
        extToContentType.put("ez", "application/andrew-inset");
        extToContentType.put("hqx", "application/mac-binhex40");
        extToContentType.put("cpt", "application/mac-compactpro");
        extToContentType.put("doc", "application/msword");
        extToContentType.put("bin", "application/octet-stream");
        extToContentType.put("dms", "application/octet-stream");
        extToContentType.put("lha", "application/octet-stream");
        extToContentType.put("lzh", "application/octet-stream");
        extToContentType.put("exe", "application/octet-stream");
        extToContentType.put("class", "application/octet-stream");
        extToContentType.put("so", "application/octet-stream");
        extToContentType.put("dll", "application/octet-stream");
        extToContentType.put("oda", "application/oda");
        extToContentType.put("pdf", "application/pdf");
        extToContentType.put("ai", "application/postscript");
        extToContentType.put("eps", "application/postscript");
        extToContentType.put("ps", "application/postscript");
        extToContentType.put("smi", "application/smil");
        extToContentType.put("smil", "application/smil");
        extToContentType.put("mif", "application/vnd.mif");
        extToContentType.put("xls", "application/vnd.ms-excel");
        extToContentType.put("ppt", "application/vnd.ms-powerpoint");
        extToContentType.put("wbxml", "application/vnd.wap.wbxml");
        extToContentType.put("wmlc", "application/vnd.wap.wmlc");
        extToContentType.put("wmlsc", "application/vnd.wap.wmlscriptc");
        extToContentType.put("bcpio", "application/x-bcpio");
        extToContentType.put("vcd", "application/x-cdlink");
        extToContentType.put("pgn", "application/x-chess-pgn");
        extToContentType.put("cpio", "application/x-cpio");
        extToContentType.put("csh", "application/x-csh");
        extToContentType.put("dcr", "application/x-director");
        extToContentType.put("dir", "application/x-director");
        extToContentType.put("dxr", "application/x-director");
        extToContentType.put("dvi", "application/x-dvi");
        extToContentType.put("spl", "application/x-futuresplash");
        extToContentType.put("gtar", "application/x-gtar");
        extToContentType.put("hdf", "application/x-hdf");
        extToContentType.put("js", "application/x-javascript");
        extToContentType.put("skp", "application/x-koan");
        extToContentType.put("skd", "application/x-koan");
        extToContentType.put("skt", "application/x-koan");
        extToContentType.put("skm", "application/x-koan");
        extToContentType.put("latex", "application/x-latex");
        extToContentType.put("nc", "application/x-netcdf");
        extToContentType.put("cdf", "application/x-netcdf");
        extToContentType.put("sh", "application/x-sh");
        extToContentType.put("shar", "application/x-shar");
        extToContentType.put("swf", "application/x-shockwave-flash");
        extToContentType.put("sit", "application/x-stuffit");
        extToContentType.put("SV4CPIO", "APPLICATION/X-SV4CPIO");
        extToContentType.put("SV4CRC", "APPLICATION/X-SV4CRC");
        extToContentType.put("tar", "application/x-tar");
        extToContentType.put("tcl", "application/x-tcl");
        extToContentType.put("tex", "application/x-tex");
        extToContentType.put("texinfo", "application/x-texinfo");
        extToContentType.put("texi", "application/x-texinfo");
        extToContentType.put("t", "application/x-troff");
        extToContentType.put("tr", "application/x-troff");
        extToContentType.put("roff", "application/x-troff");
        extToContentType.put("man", "application/x-troff-man");
        extToContentType.put("me", "application/x-troff-me");
        extToContentType.put("ms", "application/x-troff-ms");
        extToContentType.put("ustar", "application/x-ustar");
        extToContentType.put("src", "application/x-wais-source");
        extToContentType.put("XHTML", "APPLICATION/XHTML+XML");
        extToContentType.put("XHT", "APPLICATION/XHTML+XML");
        extToContentType.put("zip", "application/zip");
        extToContentType.put("au", "audio/basic");
        extToContentType.put("snd", "audio/basic");
        extToContentType.put("mid", "audio/midi");
        extToContentType.put("midi", "audio/midi");
        extToContentType.put("kar", "audio/midi");
        extToContentType.put("mpga", "audio/mpeg");
        extToContentType.put("MP2", "AUDIO/MPEG");
        extToContentType.put("MP3", "AUDIO/MPEG");
        extToContentType.put("aif", "audio/x-aiff");
        extToContentType.put("aiff", "audio/x-aiff");
        extToContentType.put("aifc", "audio/x-aiff");
        extToContentType.put("M3U", "AUDIO/X-MPEGURL");
        extToContentType.put("ram", "audio/x-pn-realaudio");
        extToContentType.put("rm", "audio/x-pn-realaudio");
        extToContentType.put("rpm", "audio/x-pn-realaudio-plugin");
        extToContentType.put("ra", "audio/x-realaudio");
        extToContentType.put("wav", "audio/x-wav");
        extToContentType.put("pdb", "chemical/x-pdb");
        extToContentType.put("xyz", "chemical/x-xyz");
        extToContentType.put("bmp", "image/bmp");
        extToContentType.put("gif", "image/gif");
        extToContentType.put("ief", "image/ief");
        extToContentType.put("jpeg", "image/jpeg");
        extToContentType.put("jpg", "image/jpeg");
        extToContentType.put("jpe", "image/jpeg");
        extToContentType.put("png", "image/png");
        extToContentType.put("tiff", "image/tiff");
        extToContentType.put("tif", "image/tiff");
        extToContentType.put("djvu", "image/vnd.djvu");
        extToContentType.put("djv", "image/vnd.djvu");
        extToContentType.put("wbmp", "image/vnd.wap.wbmp");
        extToContentType.put("ras", "image/x-cmu-raster");
        extToContentType.put("pnm", "image/x-portable-anymap");
        extToContentType.put("pbm", "image/x-portable-bitmap");
        extToContentType.put("pgm", "image/x-portable-graymap");
        extToContentType.put("ppm", "image/x-portable-pixmap");
        extToContentType.put("rgb", "image/x-rgb");
        extToContentType.put("xbm", "image/x-xbitmap");
        extToContentType.put("xpm", "image/x-xpixmap");
        extToContentType.put("xwd", "image/x-xwindowdump");
        extToContentType.put("igs", "model/iges");
        extToContentType.put("iges", "model/iges");
        extToContentType.put("msh", "model/mesh");
        extToContentType.put("mesh", "model/mesh");
        extToContentType.put("silo", "model/mesh");
        extToContentType.put("wrl", "model/vrml");
        extToContentType.put("vrml", "model/vrml");
        extToContentType.put("css", "text/css");
        extToContentType.put("html", "text/html");
        extToContentType.put("htm", "text/html");
        extToContentType.put("asc", "text/plain");
        extToContentType.put("txt", "text/plain");
        extToContentType.put("rtx", "text/richtext");
        extToContentType.put("rtf", "text/rtf");
        extToContentType.put("sgml", "text/sgml");
        extToContentType.put("sgm", "text/sgml");
        extToContentType.put("tsv", "text/tab-separated-values");
        extToContentType.put("wml", "text/vnd.wap.wml");
        extToContentType.put("wmls", "text/vnd.wap.wmlscript");
        extToContentType.put("etx", "text/x-setext");
        extToContentType.put("xsl", "text/xml");
        extToContentType.put("xml", "text/xml");
        extToContentType.put("mpeg", "video/mpeg");
        extToContentType.put("mpg", "video/mpeg");
        extToContentType.put("mpe", "video/mpeg");
        extToContentType.put("qt", "video/quicktime");
        extToContentType.put("mov", "video/quicktime");
        extToContentType.put("mxu", "video/vnd.mpegurl");
        extToContentType.put("avi", "video/x-msvideo");
        extToContentType.put("movie", "video/x-sgi-movie");
        extToContentType.put("ice", "x-conference/x-cooltalk");
        extToContentType.put("ico", "image/x-icon");
    }
    
    public static String guessContentTypeFromName(String name)
    {
        name = FileUtils.getExt(name);
        String ct = extToContentType.get(name);
        ct = ct == null ? "text/plain" : ct;
        return ct;
    }
    
    public static String getLocalIpAddress()
    {
        return getLocalIpAddress(true);
    }
    
    public static final String DELIM = "\\.";
    
    private static boolean isIPv4(String ip)
    {
        if (ip == null)
        {
            return false;
        }
        
        ip = ip.trim();
        String[] parts = ip.split(DELIM);
        
        if (parts.length != 4)
        {
            return false;
        }
        
        for (String part : parts)
        {
            try
            {
                int intVal = Integer.parseInt(part);
                if (intVal < 0 || intVal > 255)
                {
                    return false;
                }
                
            }
            catch (NumberFormatException nfe)
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get IP address from first non-localhost interface
     * 
     * @param ipv4
     *            true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getLocalIpAddress(boolean useIPv4)
    {
        try
        {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces)
            {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs)
                {
                    if (!addr.isLoopbackAddress())
                    {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = isIPv4(sAddr);
                        if (useIPv4)
                        {
                            if (isIPv4)
                                return sAddr;
                        }
                        else
                        {
                            if (!isIPv4)
                            {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
        } // for now eat exceptions
        return "";
    }
    
    /**
     * Checks to see if a specific port is available.
     *
     * @param port
     *            the port to check for availability
     */
    public static boolean portAvailable(int port)
    {
        if (port < 1 || port > 65535)
        {
            return false;// throw new IllegalArgumentException("Invalid start port: " + port);
        }
        
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try
        {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            /*
             * ds = new DatagramSocket(port); ds.setReuseAddress(true);
             */// UDP
            return true;
        }
        catch (IOException e)
        {
        }
        finally
        {
            if (ds != null)
            {
                ds.close();
            }
            
            if (ss != null)
            {
                try
                {
                    ss.close();
                }
                catch (IOException e)
                {
                    /* should not be thrown */
                }
            }
        }
        
        return false;
    }
}