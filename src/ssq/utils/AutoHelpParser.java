package ssq.utils;

import java.util.Vector;

public class AutoHelpParser extends CmdLineParser
{
    private String          projectName;
    Vector<String>          options     = new Vector<String>();
    Vector<String>          helpStrings = new Vector<String>();
    Vector<String>          defaultVals = new Vector<String>();
    
    private Option<Boolean> helpO       = addHelp(addBooleanOption('h', "help"), "Show this help message");
    private boolean         parseCalled = false;

    int                     maxoptionl  = 0;
    int                     maxhelpl    = 0;
    int                     maxdefvall  = 0;
    
    public AutoHelpParser(String name)
    {
        projectName = name;
    }
    
    public <T> Option<T> addHelp(Option<T> option, String helpString, String defaultVal)
    {
        String optionStr = " -" + option.shortForm() + "/--" + option.longForm();
        maxoptionl = Math.max(maxoptionl, optionStr.length());
        options.add(optionStr);
        maxhelpl = Math.max(maxhelpl, helpString.length());
        helpStrings.add(helpString);
        maxdefvall = Math.max(maxdefvall, defaultVal.length());
        defaultVals.add(defaultVal);
        return option;
    }
    
    public <T> Option<T> addHelp(Option<T> option, String helpString)
    {
        return addHelp(option, helpString, "");
    }
    
    public void printUsage()
    {
        LogUtils.logWarningString("usage: " + projectName + " [options]", null, false);
        for (int i = 0; i < options.size(); ++i)
        {
            String def = defaultVals.get(i);
            def = StrUtils.noContent(def) ? StrUtils.pad("", maxdefvall + 3, true) : "[" + StrUtils.pad(def, maxdefvall, true) + "] ";
            LogUtils.logWarningString(StrUtils.pad(options.get(i), maxoptionl + 1, true)
                    + ": " + def
                    + StrUtils.pad(helpStrings.get(i), maxhelpl, true)
                    , null, false);
        }
    }

    @Override
    public void parse(String[] args)
    {
        try
        {
            super.parse(args);
            parseCalled = true;
            if (getOptionValue(helpO, false))
            {
                printUsage();
                System.exit(0);
            }
        }
        catch (CmdLineParser.OptionException e)
        {
            System.err.println(e.getMessage());
            printUsage();
            System.exit(2);
        }

    }
    
    @Override
    public <T> T getOptionValue(Option<T> o)
    {
        if (!parseCalled)
        {
            LogUtils.logWarningString("parse not called!", "args parse", false);
        }
        return super.getOptionValue(o);
    }
    
    @Override
    public <T> T getOptionValue(Option<T> o, T def)
    {
        if (!parseCalled)
        {
            LogUtils.logWarningString("parse not called!", "args parse", false);
        }
        return super.getOptionValue(o, def);
    }

}