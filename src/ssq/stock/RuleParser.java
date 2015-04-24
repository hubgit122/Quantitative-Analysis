package ssq.stock;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.Test;

import ssq.utils.DirUtils;
import ssq.utils.FileUtils;
import fri.patterns.interpreter.parsergenerator.Parser;
import fri.patterns.interpreter.parsergenerator.builder.SerializedParser;
import fri.patterns.interpreter.parsergenerator.semantics.PrintSemantic;
import fri.patterns.interpreter.parsergenerator.semantics.TreeBuilderSemantic;
import fri.patterns.interpreter.parsergenerator.semantics.TreeBuilderSemantic.Node;

public class RuleParser
{
    Parser parser;

    @Test
    public void test() throws Exception
    {
        Parser parser = new RuleParser().iniParser();
        
        @SuppressWarnings("resource")
        Scanner s = new Scanner(System.in);
        
        for (String input = s.nextLine();; input = s.nextLine())
        {
            parser.setInput(input); // set file input to lexer
            parser.parse(new PrintSemantic()); // parse input
            
            parser.setInput(input); // set file input to lexer
            if (parser.parse(new TreeBuilderSemantic()))
            {
                Node n = (Node) parser.getResult();
                System.err.println("got result: " + n);
                System.out.println(n.toString(0));
            }
        }
    }
    
    public RuleParser() throws Exception
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
        
        //        SyntaxBuilder builder = new SyntaxBuilder(FileUtils.openAssetsString(".syntax"));
        //        Lexer lexer = builder.getLexer();
        //        lexer.setDebug(true);
        //        ParserTables tables = new LALRParserTables(builder.getParserSyntax());
        //        parser = new Parser(tables);
        //        parser.setLexer(lexer);
        //
        //        parser.getParserTables().dump(System.out);
        
        return parser;
    }
    
    protected ReflectTreeBuilder.Node getRoot(String input)
    {
        try
        {
            parser.setInput(input);
            
            if (parser.parse(new ReflectTreeBuilder()))
            {
                return (ReflectTreeBuilder.Node) parser.getResult();
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