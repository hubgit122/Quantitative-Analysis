package ssq.stock.interpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import ssq.stock.Stock;
import ssq.stock.interpreter.ReflectTreeBuilder.BiExpression;
import ssq.stock.interpreter.ReflectTreeBuilder.Expression;
import ssq.stock.interpreter.ReflectTreeBuilder.Rule;
import ssq.stock.interpreter.ReflectTreeBuilder.RuleTerm;
import ssq.stock.interpreter.ReflectTreeBuilder.Rules;
import ssq.stock.interpreter.ReflectTreeBuilder.Val;
import ssq.utils.FileUtils;
import ssq.utils.LogUtils;
import ssq.utils.Pair;

public class Interpreter
{
    HashMap<Val, Float>              memory = new HashMap<>();
    LinkedList<Pair<Integer, Float>> evals  = new LinkedList<>();
    File                             outFile;
    
    public static void main(String[] args) throws Exception
    {
        LogUtils.logString("选股命令是" + args[0], "功能提示", false);
        LogUtils.logString("在路径[" + args[1] + "]处放置了光大证券超强版", "功能提示", false);
        LogUtils.logString("结果输出到: " + args[2], "功能提示", false);
        
        new Interpreter(args[2]).run(args[0], args[1]);
    }

    public Interpreter(String fout) throws IOException
    {
        outFile = new File(fout);
    }

    public void run(String insturction, String root) throws Exception
    {
        RuleParser parser = new RuleParser();
        parser.iniParser();
        
        LinkedList<File> files = FileUtils.getListOf(new File(root, "vipdoc/sh/lday/"), true);
        files.addAll(FileUtils.getListOf(new File(root, "vipdoc/sz/lday/"), true)); //先上海再深圳

        scan(parser.getRoot(insturction), files);
    }
    
    private void scan(Rules AST, LinkedList<File> files)
    {
        LogUtils.logString("开始扫描", "进度信息", false);

        int i = 0;
        
        for (File f : files)
        {
            try
            {
                Stock s = new Stock(f, -1, -1);

                if (s.history.size() == 0)
                {
                    throw new FileNotFoundException();
                }

                Pair<Integer, Float> result = new Pair<Integer, Float>((s.number << 1) + (s.isShangHai ? 0 : 1), evaluate(s, AST));
                evals.add(result);
                memory.clear();
            }
            catch (FileNotFoundException e1)
            {
            }
            catch (Exception e)
            {
            }

            if (++i % 100 == 0) //每扫描1000支可能的股票更新显示
            {
                LogUtils.logString("扫描总数: " + i + ", 扫描百分比: " + (100.0 * i / files.size()), "进度信息", false);
            }
        }

        Collections.sort(evals, new Comparator<Pair<Integer, Float>>()
                {
            @Override
            public int compare(Pair<Integer, Float> o1, Pair<Integer, Float> o2)
            {
                float v1 = o1.getValue(), v2 = o2.getValue();
                return v1 == v2 ? 0 : v1 > v2 ? -1 : 1;
            }
                });

        if (evals.size() > 100)
        {
            evals = (LinkedList<Pair<Integer, Float>>) evals.subList(0, 100);
        }
        
        LogUtils.logString("扫描结束", "进度信息", false);
        print(evals);
    }
    
    private void print(List<Pair<Integer, Float>> evals)
    {
        FileWriter fileWriter;
        try
        {
            fileWriter = new FileWriter(outFile);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
            return;
        }

        try
        {
            for (Pair<Integer, Float> element : evals)
            {
                int nn = element.getKey();
                int num = nn >> 1;

                fileWriter.write((((nn & 1) == 0) ? "上海" : "深圳") + num + " 得分 " + element.getValue() * 100 + "\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            fileWriter.close();
        }
        catch (IOException e)
        {
        }
    }

    private float evaluate(Stock s, Rules AST)
    {
        float result = 1f;
        
        for (Rule rule : AST.rules)
        {
            result *= evaluate(s, rule);

            if (result == 0 || !evals.isEmpty() && result < evals.getLast().getValue())
            {
                break;
            }
        }
        
        return result;
    }

    private float evaluate(Stock s, Rule rule)
    {
        float result = 0f;
        
        for (RuleTerm term : rule.terms)
        {
            result = Math.max(result, evaluate(s, term));
            
            if (result == 1f)
            {
                break;
            }
        }
        
        return result;
    }

    private float evaluate(Stock s, RuleTerm term)
    {
        float lExp = evaluate(s, term.lexpr), rExp = evaluate(s, term.rexpr);
        int order = term.inequality.ordinal();
        
        if (order < 2) // < or <=
        {
            return saturate(rExp / lExp);
        }
        else if (order > 2)
        {
            return saturate(lExp / rExp);
        }
        else
        {
            return Math.min(rExp / lExp, lExp / rExp);
        }

    }
    
    private static float saturate(float f)
    {
        if (f > 1f)
        {
            return 1f;
        }
        else if (f < 0f)
        {
            return 0f;
        }
        else
        {
            return f;
        }
    }
    
    private float evaluate(Stock s, Expression expr)
    {
        if (expr instanceof BiExpression)
        {
            BiExpression biExpr = (BiExpression) expr;
            return biExpr.operator.doOp(evaluate(s, biExpr.lExpression), evaluate(s, biExpr.rExpression));
        }
        //        else if (expr instanceof UniExpression)
        //        {
        //            return - evaluate(s, ((UniExpression)expr)....);
        //        }
        else
        { // Val
            Val val = (Val) expr;
            
            if (val.isFloat)
            {
                return ((Val) expr).val;
            }
            else
            {
                //                if (memory.keySet().size() == 5)
                //                {
                //                    System.out.println();
                //                }
                
                Float f = memory.get(val);

                if (f != null)
                {
                    return f;
                }
                else
                {
                    Vector<Float> args = new Vector<>();
                    
                    for (Expression e : val.args)
                    {
                        args.add(evaluate(s, e));
                    }

                    float result = s.history.func(val.func, args);

                    //                    if (memory.keySet().size() == 5)
                    //                    {
                    //                        System.out.println();
                    //                    }

                    memory.put(val, result);

                    return result;
                }
            }
        }
    }
}
