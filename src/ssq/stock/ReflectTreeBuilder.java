package ssq.stock;

import java.awt.Color;
import java.awt.Point;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.java_websocket.framing.Framedata.Opcode;
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
                "min(250 ->1) < min(750 -> 251) && max(5 -> 1) > max(300 -> 6)",
                "min(250 ->1) < min(750 -> 251) && max(5 -> 1) > max(300 -> 6) && max(250 -> 1)/min(250 -> 1) <= 1.5"
        })
        {
            System.out.println(string);
            Rules node = parser.getRoot(string);
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
        ((Rules) rules).add((Rule) rule);
        return rules;
    }

    public Object rule(Object ruleTerm)
    {
        Rule result = new Rule();
        result.add((RuleTerm) ruleTerm);
        return result;
    }

    public Object rule(Object rule, Object and, Object ruleTerm)
    {
        ((Rule) rule).add((RuleTerm) ruleTerm);
        return rule;
    }
    
    public Object ruleterm(Object lexpr, Object inequ, Object rexpr) throws Exception
    {
        RuleTerm result = new RuleTerm();
        result.lexpr = (Expression) lexpr;
        result.rexpr = (Expression) rexpr;
        result.c = Inequality.myValueOf((String) inequ);

        return result;
    }

    public Object EXPRESSION(Object term)
    {
        return term;
    }

    public Object EXPRESSION(Object l, Object op, Object r) throws Exception
    {
        BiExpression biExpression = new BiExpression();
        biExpression.lExpression = (Expression) l;
        biExpression.rExpression = (Expression) r;
        biExpression.operator = BinaryOperator.myValueOf((String) op);
        
        return biExpression;
    }

    public Object TERM(Object term)
    {
        return term;
    }

    public Object TERM(Object l, Object op, Object r) throws Exception
    {
        BiExpression biExpression = new BiExpression();
        biExpression.lExpression = (Expression) l;
        biExpression.rExpression = (Expression) r;
        biExpression.operator = BinaryOperator.myValueOf((String) op);
        
        return biExpression;
    }

    public Object FACTOR(Object f)
    {
        Val result = new Val();
        result.val = Float.valueOf((String) f);

        return result;
    }

    public Object args(Object lp, Object le, Object eli, Object re, Object rp)
    {
        Vector<Expression> expressions = new Vector<>();
        
        expressions.add((Expression) le);
        expressions.add((Expression) re);
        
        return expressions;
    }
    
    public Object FACTOR(Object f, Object s)
    {
        if (!f.equals("-")) //是一个函数
        {
            Val val = new Val();
            val.isFloat = false;
            val.func = (String) f;
            
            val.args = (List<Expression>) s;
            
            return val;
        }
        else
        {
            BiExpression biExpression = new BiExpression();
            Val val = new Val();
            val.val = 0;
            
            biExpression.lExpression = val;
            biExpression.rExpression = (Expression) s;
            biExpression.operator = BinaryOperator.SUB;

            return biExpression;
        }

    }

    public Object FACTOR(Object l, Object op, Object r) throws Exception
    {
        BiExpression biExpression = new BiExpression();
        biExpression.lExpression = (Expression) l;
        biExpression.rExpression = (Expression) r;
        biExpression.operator = BinaryOperator.myValueOf((String) op);
        
        return biExpression;
    }

    public static class Rules extends LinkedList<Rule>
    {
    }
    
    public static class Rule extends LinkedList<RuleTerm>
    {
        
    }
    
    public static interface Expression
    {

    }

    public static class BiExpression implements Expression
    {
        BinaryOperator operator;
        Expression     lExpression, rExpression;
    }
    
    public static class UniExpression implements Expression
    {
        
    }
    
    public static class Val implements Expression
    {
        boolean          isFloat = true;
        float            val;
        
        String           func;
        List<Expression> args;
    }
    
    public static class RuleTerm
    {
        Expression lexpr, rexpr;
        Inequality c;
    }

    public enum BinaryOperator
    {
        ADD("+"), SUB("-"), MUL("*"), DIV("/");
        
        private String nameString;

        BinaryOperator(String name)
        {
            nameString = name;
        }

        @Override
        public String toString()
        {
            return nameString;
        }
        
        public static BinaryOperator myValueOf(String s) throws Exception
        {
            switch (s.charAt(0))
            {
                case '+':
                    return ADD;

                case '-':
                    return SUB;

                case '*':
                    return MUL;

                case '/':
                    return DIV;

                default:
                    throw new Exception("暂不支持的运算符: " + s);
            }
        }
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
