package ssq.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class GraphViz
{
    /**
     * Where is your dot program located? It will be called externally.
     */
    private static String DOT         = "./graphviz/bin/dot"; //#ifdef Windows
                                                              
    private static File   TEMP_DIR    = new File("./tmp");
    static
    {
        if (!TEMP_DIR.isDirectory())
        {
            TEMP_DIR.mkdirs();
        }
    }
    
    private StringBuilder graphSource = new StringBuilder();
    
    public GraphViz()
    {
    }
    
    public GraphViz(String str)
    {
        readSource(str);
    }
    
    public String getDotSource()
    {
        return graphSource.toString();
    }
    
    public void add(String line)
    {
        graphSource.append(line);
    }
    
    public void addln(String line)
    {
        graphSource.append(line + "\n");
    }
    
    public void addln()
    {
        addln("");
    }
    
    public byte[] getGraph(String dot_source, String type) throws IOException
    {
        File dot;
        byte[] img_stream = null;
        
        try
        {
            dot = writeDotSourceToFile(dot_source);
            if (dot != null)
            {
                img_stream = get_img_stream(dot, type);
                if (dot.delete() == false)
                    System.err.println("Warning: " + dot.getAbsolutePath() + " could not be deleted!");
                return img_stream;
            }
            return null;
        }
        catch (java.io.IOException ioe)
        {
            ioe.printStackTrace();
            return null;
        }
    }
    
    public int writeGraphToFile(byte[] img, String file)
    {
        File to = new File(file);
        return writeGraphToFile(img, to);
    }
    
    public int writeGraphToFile(byte[] img, File to)
    {
        try
        {
            if (!to.exists())
            {
                to.createNewFile();
            }
            
            FileOutputStream fos = new FileOutputStream(to);
            fos.write(img);
            fos.close();
        }
        catch (java.io.IOException ioe)
        {
            ioe.printStackTrace();
            return -1;
        }
        return 1;
    }
    
    private byte[] get_img_stream(File dot, String type)
    {
        File img;
        byte[] img_stream = null;
        
        try
        {
            img = File.createTempFile("graph_", "." + type, TEMP_DIR);
            Runtime rt = Runtime.getRuntime();
            
            String[] args = { DOT, "-T" + type, dot.getAbsolutePath(), "-o", img.getAbsolutePath() };
            try
            {
                Process p = rt.exec(args);
                p.waitFor();
            }
            catch (java.lang.InterruptedException ie)
            {
                System.err.println("Error: the execution of the external program was interrupted");
                ie.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            
            FileInputStream in = new FileInputStream(img.getAbsolutePath());
            img_stream = new byte[in.available()];
            in.read(img_stream);
            // Close it if we need to
            if (in != null)
                in.close();
            
            if (img.delete() == false)
                System.err.println("Warning: " + img.getAbsolutePath() + " could not be deleted!");
        }
        catch (java.io.IOException ioe)
        {
            System.err.println("Error:    in I/O processing of tempfile in dir " + GraphViz.TEMP_DIR + "\n");
            System.err.println("       or in calling external command");
            ioe.printStackTrace();
        }
        
        return img_stream;
    }
    
    private File writeDotSourceToFile(String str) throws java.io.IOException
    {
        File temp;
        try
        {
            temp = File.createTempFile("graph_", ".dot.tmp", TEMP_DIR);
            FileWriter fout = new FileWriter(temp);
            fout.write(str);
            fout.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Error: I/O error while writing the dot source to temp file!");
            return null;
        }
        return temp;
    }
    
    public String start_graph()
    {
        return "digraph G {";
    }
    
    public String end_graph()
    {
        return "}";
    }
    
    public void setSource(String s)
    {
        graphSource = new StringBuilder(s);
    }
    
    public void readSource(String input)
    {
        StringBuilder sb = new StringBuilder();
        
        try
        {
            FileInputStream fis = new FileInputStream(input);
            DataInputStream dis = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            String line;
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
            dis.close();
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
        
        this.graphSource = sb;
    }
}
