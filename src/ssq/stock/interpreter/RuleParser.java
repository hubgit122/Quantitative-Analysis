package ssq.stock.interpreter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import ssq.stock.interpreter.ReflectTreeBuilder.Rules;
import ssq.utils.DirUtils;
import ssq.utils.FileUtils;
import fri.patterns.interpreter.parsergenerator.Parser;
import fri.patterns.interpreter.parsergenerator.builder.SerializedParser;
import fri.patterns.interpreter.parsergenerator.semantics.TreeBuilderSemantic;
import fri.patterns.interpreter.parsergenerator.semantics.TreeBuilderSemantic.Node;

public class RuleParser
{
    Parser parser;

    public RuleParser()
    {
    }
    
    protected Parser iniParser() throws Exception
    {
        // read the syntax from EBNF file
        parser = new SerializedParser().get(null, FileUtils.openAssetsString("rules.syntax"), "stock");

        File dump = new File(DirUtils.getTmpRoot() + "parsers/stock.dump.txt");
        if (!dump.exists())
        {
            parser.getParserTables().dump(new PrintStream(dump));
        }

        //
        //        SyntaxBuilder builder = new SyntaxBuilder(FileUtils.openAssetsString("rules.syntax"));
        //        Lexer lexer = builder.getLexer();
        //        lexer.setDebug(true);
        //        ParserTables tables = new LALRParserTables(builder.getParserSyntax());
        //        parser = new Parser(tables);
        //        parser.setLexer(lexer);
        //
        //        parser.getParserTables().dump(System.out);
        
        return parser;
    }
    
    protected Rules getRoot(String input)
    {
        try
        {
            parser.setInput(input);
            
            if (parser.parse(new ReflectTreeBuilder()))
            {
                return (Rules) parser.getResult();
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
    
    protected Node getOldRoot(String input)
    {
        try
        {
            parser.setInput(input);
            
            if (parser.parse(new TreeBuilderSemantic()))
            {
                return (Node) parser.getResult();
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