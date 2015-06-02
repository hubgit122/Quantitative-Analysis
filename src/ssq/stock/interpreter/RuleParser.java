package ssq.stock.interpreter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import ssq.stock.interpreter.ReflectTreeBuilder.RuleLevel;
import ssq.utils.DirUtils;
import ssq.utils.FileUtils;
import fri.patterns.interpreter.parsergenerator.Lexer;
import fri.patterns.interpreter.parsergenerator.Parser;
import fri.patterns.interpreter.parsergenerator.ParserTables;
import fri.patterns.interpreter.parsergenerator.parsertables.LALRParserTables;
import fri.patterns.interpreter.parsergenerator.syntax.builder.SyntaxBuilder;

public class RuleParser
{
    Parser parser;

    public RuleParser()
    {
    }

    public Parser iniParser(String rule)
    {
        try
        {
            SyntaxBuilder builder = new SyntaxBuilder(rule);
            Lexer lexer = builder.getLexer();
            lexer.setDebug(true);
            ParserTables tables = new LALRParserTables(builder.getParserSyntax());
            parser = new Parser(tables);
            parser.setLexer(lexer);
            
            //        parser.getParserTables().dump(System.out);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return parser;
    }

    protected Parser iniParser() throws Exception
    {
        SyntaxBuilder builder = new SyntaxBuilder(FileUtils.openAssetsString("rules.syntax"));
        Lexer lexer = builder.getLexer();
        lexer.setDebug(true);
        ParserTables tables = new LALRParserTables(builder.getParserSyntax());
        parser = new Parser(tables);
        parser.setLexer(lexer);

        File dump = new File(DirUtils.getTmpRoot() + "parsers/stock.dump.txt");
        FileUtils.assertFileExists(dump);
        parser.getParserTables().dump(new PrintStream(dump));

        return parser;
    }
    
    public RuleLevel getRoot(String input)
    {
        try
        {
            parser.setInput(input);
            
            if (parser.parse(new ReflectTreeBuilder()))
            {
                return (RuleLevel) parser.getResult();
            }
            else
            {
                return null;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}