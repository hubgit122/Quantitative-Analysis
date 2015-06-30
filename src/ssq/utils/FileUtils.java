package ssq.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FileUtils
{
    public static File assertFileExists(File file)
    {
        if (!file.isDirectory())
        {
            if (!file.exists())
            {
                file.getParentFile().mkdirs();

                try
                {
                    file.createNewFile();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }
    
    public static File assertDirExists(File file)
    {
        if (!file.isFile())
        {
            if (!file.exists())
            {
                file.mkdirs();
            }
        }
        return file;
    }

    public static InputStream downloadFile(String urlString) throws IOException
    {
        URL url = new URL(urlString);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        InputStream inStream = conn.getInputStream();
        return inStream;
    }
    
    /**
     * 使用文件通道的方式复制文件
     *
     * @param s
     *            源文件
     * @param t
     *            复制到的新文件
     */
    public static void copyFile(File s, File t)
    {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        
        try
        {
            fi = new FileInputStream(s);
            fo = new FileOutputStream(t);
            in = fi.getChannel();//得到对应的文件通道
            out = fo.getChannel();//得到对应的文件通道
            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                fi.close();
                in.close();
                fo.close();
                out.close();
                
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * change the content to a string.
     *
     * @param file
     * @return the content of file.
     */
    public static final String fileToString(File file)
    {
        return fileToString(file, null);
    }
    
    public static final String fileToString(File file, String encoding)
    {
        if (file.canRead() && file.isFile())
        {
            try
            {
                return inputStream2String(new FileInputStream(file), encoding, 1, true);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return "";
            }
        }
        else
        {
            return "";
        }
    }
    
    public static boolean fileToStream(File file, OutputStream o)
    {
        if (file.canRead() && file.isFile())
        {
            FileInputStream fin;
            try
            {
                fin = new FileInputStream(file);
            }
            catch (FileNotFoundException e)
            {
                return false;
            }
            BufferedInputStream bfin = new BufferedInputStream(fin);
            BufferedOutputStream bfout = new BufferedOutputStream(o);
            byte[] buffer = new byte[4096];
            
            try
            {
                while ((bfin.read(buffer)) != -1)
                {
                    bfout.write(buffer);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }
            finally
            {
                try
                {
                    bfin.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return false;
                }
                try
                {
                    bfout.flush();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * 读取输入流中的文本
     *
     * @param input
     *            输入流
     * @param encoding
     *            编码格式(默认为UTF-8)
     * @param beginLine
     *            从第几行开始读(默认从第1行开始)
     * @param asLine
     *            读完一行是否写入换行符
     * @return String
     * @throws Exception
     */
    public static String inputStream2String(InputStream input, String encoding, int beginLine, boolean asLine) throws Exception
    {
        StringBuilder result = new StringBuilder("");
        beginLine = (beginLine <= 0 ? 1 : beginLine);
        encoding = (encoding == null ? "UTF-8" : encoding);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, encoding));
        String line;
        int i = 1;
        while ((line = reader.readLine()) != null)
        {
            if (i >= beginLine)
            {
                result.append(line + (asLine ? "\r\n" : ""));
            }
            i++;
        }
        reader.close();
        input.close();
        return result.toString();
    }
    
    /**
     * save string to a file(recover).
     *
     * @param file
     * @param content
     * @return success flag.
     */
    public static final boolean saveString2File(File file, String content)
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            bw.write(content);
            bw.close();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    private final static String        SYS_TEMP_FILE = System.getProperty("java.io.tmpdir") + File.separator + "gamest.properties";
    private static Map<String, String> loadedProp    = new HashMap<String, String>();
    static
    {
        if (!new File(SYS_TEMP_FILE).exists())
        {
            try
            {
                new File(SYS_TEMP_FILE).createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public static final String getAPropertyFromSysTempFile(String key)
    {
        loadPropertiesFromSysTempFile();
        String val = loadedProp.get(key);
        return null == val ? "" : val;
    }
    
    private static final boolean loadPropertiesFromSysTempFile()
    {
        try
        {
            Properties props = new Properties();
            InputStream in = new BufferedInputStream(new FileInputStream(SYS_TEMP_FILE));
            props.load(in);
            Enumeration<?> en = props.propertyNames();
            while (en.hasMoreElements())
            {
                String key = (String) en.nextElement();
                String prop = props.getProperty(key);
                loadedProp.put(key, prop);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    public static final int saveAPropertyToSysTempFile(String parameterName, String parameterValue)
    {
        Properties prop = new Properties();
        try
        {
            InputStream fis = new FileInputStream(SYS_TEMP_FILE);
            prop.load(fis);
            
            OutputStream fos = new FileOutputStream(SYS_TEMP_FILE);
            prop.setProperty(parameterName, parameterValue);
            prop.store(fos, "Update '" + parameterName + "' value");
            
            loadedProp.put(parameterName, parameterValue);
            return loadedProp.size();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return -1;
        }
    }
    
    public static ArrayList<File> getFilteredListOf(File dirFile, boolean filesNotDirs, String filter)//列出目录下所有的文件&文件夹
    {
        File[] files = dirFile.listFiles();
        ArrayList<File> result = new ArrayList<File>();
        
        for (File file : files)
        {
            if ((file.isDirectory() ^ filesNotDirs) && (filter == null || file.getName().matches(filter)))
            {
                result.add(file);
            }
        }
        
        return result;
    }
    
    public static boolean delAllFile(String dirName)//删除指定文件夹下所有文件
    {
        boolean flag = true;
        //如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dirName.endsWith(File.separator))
        {
            dirName = dirName + File.separator;
            
        }
        File dirFile = new File(dirName);
        //如果dir对应的文件不存在，或者不是一个文件夹则退出
        if (!dirFile.exists() || !dirFile.isDirectory())
        {
            LogUtils.logWarningString("List失败！找不到目录：" + dirName, FileUtils.class.getName(), false);
            return false;
        }
        
        //列出文件夹下所有的文件,listFiles方法返回目录下的所有文件（包括目录）的File对象
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isFile())
            {
                boolean success = files[i].delete();
                flag &= success;
                if (!success)
                {
                    LogUtils.logWarningString(files[i].getAbsolutePath() + " 删除失败\n", FileUtils.class.getName(), false);
                }
                else
                {
                    LogUtils.logWarningString(files[i].getAbsolutePath() + " 删除成功\n", FileUtils.class.getName(), true);
                }
            }
            else if (files[i].isDirectory())
            {
                LogUtils.logWarningString(files[i].getAbsolutePath() + " 是目录, 递归删除...", FileUtils.class.getName(), true);
                delAllFile(files[i].getAbsolutePath());
            }
        }
        return flag;
    }
    
    //##else
    public static String openAssetsString(String path)
    {
        return fileToString(new File(DirUtils.getProjectRoot() + "assets/", path));
    }
    
    //##endif
    
    public static String getExt(String path)
    {
        try
        {
            return path.substring(path.lastIndexOf('.') + 1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }
    
    public static final String fileToString(String string)
    {
        return fileToString(new File(string));
    }

    public static Object getNameWithoutExt(String file)
    {
        try
        {
            return file.substring(0, file.lastIndexOf('.'));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }
}
