package ssq.stock;

import java.awt.Color;
import java.awt.Point;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.junit.Test;

import fri.patterns.interpreter.parsergenerator.semantics.ReflectSemantic;

public class ReflectTreeBuilder extends ReflectSemantic
{
    @Test
    public void treeBuilderTest()
            throws Exception
    {
        RuleParser parser = new RuleParser();
        parser.iniParser();
        //        Scanner s = new Scanner(System.in);
        //
        //        for (String input = s.nextLine();; input = s.nextLine())
        //        {
        //            Node node = new RuleParser().getRoot(input);
        //            System.out.println(node);
        //        }
        for (String string : new String[] {
                "min(250...1) < min(750...251) && max(5..1) > max(300...6) && max(250..1)/min(250...1) <= 1.5"
        })
        {
            System.out.println(string);
            ssq.stock.ReflectTreeBuilder.Node node = parser.getRoot(string);
            System.out.println(node);
        }
    }
    
    public Object rules(Object rule)
    {
        Rules result = new Rules();
        result.add((Rule) rule);
        return result;
    }
    
    public Object rules(Object rules, Object and, Object rule)
    {
        return ((Rules) rules).add((Rule) rule);
    }
    
    public Object rule(Object ruleTerm)
    {
        Rule result = new Rule();
        result.add((RuleTerm) ruleTerm);
        return result;
    }
    
    public Object rule(Object rule, Object and, Object ruleTerm)
    {
        return ((Rule) rule).add((RuleTerm) ruleTerm);
    }

    public Object ruleterm(Object lexpr, Object inequ, Object rexpr)
    {
        RuleTerm result = new RuleTerm();
        result.lexprString = (String) lexpr;
        result.rexprString = (String) rexpr;
        result.c = (Inequality) inequ;
        return result;
    }

    @SuppressWarnings("unchecked")
    public Object S_EXPRESSION(Object exprList)
    {
        return exprList;
    }
    
    public Object EXPRESSION(Object term)
    {
        return term;
    }
    
    public Object EXPRESSION(Object l, Object op, Object r)
    {
        return (String) l + (String) op + (String) r;
    }
    
    public Object TERM(Object term)
    {
        return term;
    }
    
    public Object TERM(Object l, Object op, Object r)
    {
        return (String) l + (String) op + (String) r;
    }
    
    public Object FACTOR(Object f)
    {
        return f;
    }
    
    public Object FACTOR(Object f, Object s)
    {
        Vector<Integer> testIntegers = new Vector<>();

        if (!f.equals("-")) //是一个函数
        {

        }
        return (String) f + (String) s;
    }
    
    public Object FACTOR(Object f, Object s, Object t)
    {
        return (String) f + (String) s + (String) t;
    }
    
    public static class Node
    {
        
    }
    
    public static class Rules extends LinkedList<Rule>
    {
        private static final long serialVersionUID = -7855930076486711598L;
        LinkedList<String>        assertEquals     = new LinkedList<String>();
    }

    public static class Rule extends LinkedList<RuleTerm>
    {

    }
    
    public enum Func
    {
        MIN, MAX
    }
    
    public static class RuleTerm
    {
        String     lexprString, rexprString;
        Inequality c;
    }
    
    public enum Inequality
    {
        L("<"), LE("<="), E("=="), GE(">="), G(">");
        
        private String nameString;
        
        Inequality(String name)
        {
            nameString = name;
        }
        
        @Override
        public String toString()
        {
            return nameString;
        }
        
        public static Inequality myValueOf(String s) throws Exception
        {
            switch (s)
            {
                case "+=":
                case ">=":
                    return GE;
                    
                case "-=":
                case "<=":
                    return LE;
                    
                case "==":
                    return E;
                    
                case ">":
                case "+":
                    return G;
                    
                case "<":
                case "-":
                    return L;
                default:
                    throw new Exception();
            }
        }
    }
}
