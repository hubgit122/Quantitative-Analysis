package ssq.stock.interpreter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ssq.utils.StringUtils;
import fri.patterns.interpreter.parsergenerator.semantics.ReflectSemantic;

public class ReflectTreeBuilder extends ReflectSemantic
{
    public Object ruleExpr(Object rule)
    {
        CompositeRule result = new CompositeRule();
        result.rules.add((RuleLevel) rule);
        result.op = BinaryRuleOperator.OR;
        return result;
    }
    
    public Object ruleExpr(Object ruleExpr, Object or, Object ruleTerm)
    {
        ((CompositeRule) ruleExpr).rules.add((RuleLevel) ruleTerm);

        return ruleExpr;
    }

    public Object ruleTerm(Object ruleFactor)
    {
        CompositeRule result = new CompositeRule();
        result.rules.add((RuleLevel) ruleFactor);
        result.op = BinaryRuleOperator.AND;
        return result;
    }

    public Object ruleTerm(Object ruleTerm, Object and, Object ruleFactor)
    {
        ((CompositeRule) ruleTerm).rules.add((RuleLevel) ruleFactor);

        return ruleTerm;
    }
    
    public Object ruleFactor(Object lexpr, Object inequOrRuleExpr, Object rexpr, Object opt_weight) throws Exception
    {
        AtomRule result = new AtomRule();
        result.lexpr = (Expression) lexpr;
        result.rexpr = (Expression) rexpr;
        result.inequality = Inequality.myValueOf((String) inequOrRuleExpr);
        
        try
        {
            result.weight = Float.valueOf((String) ((List<?>) opt_weight).get(1));
        }
        catch (Exception e)
        {
        }
        return result;
    }

    public Object ruleFactor(Object lexpr, Object inequOrRuleExpr, Object rexpr) throws Exception
    {
        return inequOrRuleExpr;
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

    public Object args(Object expr)
    {
        ArrayList<Expression> result = new ArrayList<>();
        result.add((Expression) expr);
        return result;
    }

    @SuppressWarnings("unchecked")
    public Object args(Object args, Object delimiter, Object expr)
    {
        ArrayList<Expression> result = (ArrayList<Expression>) args;
        result.add((Expression) expr);

        return result;
    }

    public Object FACTOR(Object f, Object s)
    {
        BiExpression biExpression = new BiExpression();
        Val val = new Val();
        val.val = 0;

        biExpression.lExpression = val;
        biExpression.rExpression = (Expression) s;
        biExpression.operator = BinaryOperator.SUB;

        return biExpression;
    }

    @SuppressWarnings("unchecked")
    public Object FACTOR(Object f, Object lp, Object args, Object rp, Object appendix, Object noRestoration)
    {
        Val val = new Val();
        val.isFloat = false;
        val.func = (String) f;
        
        val.args = (List<Expression>) args;

        try
        {
            val.type = ValueType.valueOf(((List) appendix).get(1).toString());
        }
        catch (Exception e)
        {
        }
        
        try
        {
            if (((List) noRestoration).size() > 0)
            {
                val.rest = false;
            }
        }
        catch (Exception e)
        {
        }

        return val;
    }

    public Object FACTOR(Object lRxprOrlPar, Object opOrExpr, Object rExpOrrPar) throws Exception
    {
        if (lRxprOrlPar.equals("("))
        {
            return opOrExpr;
        }
        else
        {
            BiExpression biExpression = new BiExpression();
            biExpression.lExpression = (Expression) lRxprOrlPar;
            biExpression.rExpression = (Expression) rExpOrrPar;
            biExpression.operator = BinaryOperator.myValueOf((String) opOrExpr);

            return biExpression;
        }
    }
    
    public static class Node
    {
    }

    public static class RuleLevel extends Node implements Serializable
    {
    }

    public static class CompositeRule extends RuleLevel implements Serializable
    {
        public LinkedList<RuleLevel> rules = new LinkedList<>();
        public BinaryRuleOperator    op;

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append('(');
            sb.append(StringUtils.join(op.toString(), rules));
            sb.append(')');
            
            return sb.toString();
        }
    }

    public static class AtomRule extends RuleLevel implements Serializable
    {
        Expression lexpr, rexpr;
        Inequality inequality;
        float      weight = 1f;
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder(lexpr.toString());
            sb.append(inequality).append(rexpr);
            if (weight != 1f)
            {
                sb.append('@').append(weight);
            }
            return sb.toString();
        }
    }

    public static abstract class Expression extends Node implements Serializable
    {
        @Override
        public abstract int hashCode();

        @Override
        public abstract boolean equals(Object obj);
    }
    
    public static class BiExpression extends Expression implements Serializable
    {
        public BinaryOperator operator;
        public Expression     lExpression, rExpression;

        @Override
        public int hashCode()
        {
            return lExpression.hashCode() ^ rExpression.hashCode() ^ operator.hashCode();
        }
        
        @Override
        public String toString()
        {
            return lExpression.toString() + operator.toString() + rExpression.toString();
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (obj == null || !(obj instanceof Val))
            {
                return false;
            }
            else
            {
                BiExpression that = (BiExpression) obj;
                return operator.equals(that.operator) && lExpression.equals(that.lExpression) && lExpression.equals(that.rExpression);
            }
        }
    }

    public static class Val extends Expression implements Serializable
    {
        boolean          isFloat = true;
        float            val;

        String           func;
        List<Expression> args;
        boolean          rest    = true;
        ValueType        type    = ValueType.closing;
        
        @Override
        public int hashCode()
        {
            if (isFloat)
            {
                return ((Float) val).hashCode();
            }
            
            return func.hashCode() ^ args.hashCode();
        }
        
        @Override
        public String toString()
        {
            if (isFloat)
            {
                return String.valueOf(val);
            }
            else
            {
                StringBuilder sb = new StringBuilder(func);
                sb.append('(').append(StringUtils.join(",", args)).append(')').append(type != ValueType.closing ? '.' + type.toString() : "").append(rest ? "" : "..norest");

                return sb.toString();
            }
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (obj == null || !(obj instanceof Val))
            {
                return false;
            }
            else
            {
                Val that = (Val) obj;
                if (isFloat)
                {
                    return that.isFloat && val == that.val;
                }
                else
                {
                    if (!func.equals(that.func))
                    {
                        return false;
                    }
                    else if (rest ^ that.rest || type != that.type)
                    {
                        return false;
                    }
                    else
                    {
                        return this.args.equals(that.args);
                    }
                }
            }
        }
    }
    
    public enum ValueType implements Serializable
    {
        opening, highest, closing, lowest, quantity, deal, scale;
    }
    
    public enum BinaryRuleOperator implements Serializable
    {
        OR("||"), AND("&&");

        private String nameString;
        
        BinaryRuleOperator(String name)
        {
            nameString = name;
        }
        
        public float doOp(float l, float r) throws Exception
        {
            switch (this)
            {
                case OR:
                    return l + r;
                case AND:
                    return l * r;

                default:
                    throw new Exception("暂不支持的运算符: " + this);
            }
        }
        
        @Override
        public String toString()
        {
            return nameString;
        }

        public static BinaryRuleOperator myValueOf(String s) throws Exception
        {
            assert s.length() == 2 && s.charAt(0) == s.charAt(1);
            
            switch (s.charAt(0))
            {
                case '|':
                    return OR;
                case '&':
                    return AND;
                default:
                    throw new Exception("暂不支持的运算符: " + s);
            }
        }
    }

    public enum BinaryOperator implements Serializable
    {
        ADD("+"), SUB("-"), MUL("*"), DIV("/");

        private String nameString;
        
        BinaryOperator(String name)
        {
            nameString = name;
        }
        
        public float doOp(float l, float r)
        {
            switch (this)
            {
                case ADD:
                    return l + r;
                case SUB:
                    return l - r;
                case MUL:
                    return l * r;
                default:
                    return l / r;
            }
        }
        
        @Override
        public String toString()
        {
            return nameString;
        }

        public static BinaryOperator myValueOf(String s) throws Exception
        {
            assert s.length() == 1;

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
    
    public enum Inequality implements Serializable
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
