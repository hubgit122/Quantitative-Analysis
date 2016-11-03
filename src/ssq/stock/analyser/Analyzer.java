package ssq.stock.analyser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import ssq.stock.Stock;
import ssq.stock.analyser.ReflectTreeBuilder.AtomRule;
import ssq.stock.analyser.ReflectTreeBuilder.BiExpression;
import ssq.stock.analyser.ReflectTreeBuilder.BinaryRuleOperator;
import ssq.stock.analyser.ReflectTreeBuilder.CompositeRule;
import ssq.stock.analyser.ReflectTreeBuilder.Expression;
import ssq.stock.analyser.ReflectTreeBuilder.RuleLevel;
import ssq.stock.analyser.ReflectTreeBuilder.Val;
import ssq.stock.gui.GUI;
import ssq.utils.LogUtils;
import ssq.utils.Pair;
import ssq.utils.TreeNode;
import ssq.utils.taskdistributer.Task;
import ssq.utils.taskdistributer.TaskDistributor;
import ssq.utils.taskdistributer.TaskList;

public abstract class Analyzer {
  public final String filter;

  public Analyzer() {
    filter = Stock.filter;
  }

  public Analyzer(String filter) {
    this.filter = filter;
  }

  public void run() throws Exception {
    GUI.statusText("开始分析");
    LogUtils.logString("开始分析", "进度信息", false);

    TaskList taskList = new TaskList();
    TaskDistributor distributor = new TaskDistributor(taskList, 40) {
      @Override
      public Task getNext(int lastFinished) {
        Task result = super.getNext(lastFinished);
        GUI.statusText(getProgressString());
        LogUtils.logString(getProgressString(), "进度信息", false);
        return result;
      }
    };

    int i = 0;

    for (Pair<Integer, String> pair : Stock.stockList) {
      final int index = pair.getKey();
      if (Stock.pad(index).matches(filter)) {
        taskList.add(new Task(i++) {
          @Override
          public void execute() {
            try {
              scan(index);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
      }
    }

    distributor.schedule();
    distributor.waitTasksDone();

    GUI.statusText("扫描结束");
    LogUtils.logString("扫描结束", "进度信息", false);
  }

  private void scan(Integer key) throws IOException {
    scan(Stock.loadDayLineHistory(key));
  }

  /**
   * 要有多线程安全
   *
   * @param stock
   * @throws IOException
   */
  abstract public void scan(Stock stock) throws IOException;

  public static RuleParser parser = new RuleParser();

  static {
    try {
      parser.iniParser();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected TreeNode<Float> evaluate(Stock s, RuleLevel AST, HashMap<Val, Float> memory, int back) {
    float grade;
    TreeNode<Float> result;

    if (AST instanceof CompositeRule) {
      CompositeRule expr = (CompositeRule) AST;
      result = new TreeNode<Float>(-1f);
      boolean error = false;

      if (expr.op == BinaryRuleOperator.AND) {
        grade = 1f;

        for (RuleLevel ruleLevel : expr.rules) {
          TreeNode<Float> tmp = evaluate(s, ruleLevel, memory, back);
          result.addChildNode(tmp);

          float thisGrade = tmp.getElement();
          if (thisGrade >= 0) {
            grade *= thisGrade;
          } else {
            error = true;
          }
        }
      } else {
        grade = 0f;

        for (RuleLevel ruleLevel : expr.rules) {
          TreeNode<Float> tmp = evaluate(s, ruleLevel, memory, back);
          result.addChildNode(tmp);

          float thisGrade = tmp.getElement();
          if (thisGrade >= 0) {
            grade = Math.max(grade, tmp.getElement());
          } else {
            error = true;
          }
        }
      }
      result.setElement(error ? -1 : grade);
    } else {
      try {
        AtomRule val = (AtomRule) AST;

        float lExp = evaluate(s, val.lexpr, memory, back), rExp = evaluate(s, val.rexpr, memory, back);
        int order = val.inequality.ordinal();

        if (order < 2) // < or <=
        {
          grade = saturate(rExp / lExp);
        } else if (order > 2) {
          grade = saturate(lExp / rExp);
        } else {
          grade = Math.min(rExp / lExp, lExp / rExp);
        }
        grade = 1 - (1 - grade) * val.weight;

        result = new TreeNode<Float>(grade);
        result.addChild(lExp);
        result.addChild(rExp);
      } catch (Exception e) {
        //                e.printStackTrace();

        return new TreeNode<Float>(-1f);
      }
    }

    return result;
  }

  private static float saturate(float f) {
    if (f > 1f) {
      return 1f;
    } else if (f < 0f) {
      return 0f;
    } else {
      return f;
    }
  }

  private float evaluate(Stock s, Expression expr, HashMap<Val, Float> memory, int back) {
    if (expr instanceof BiExpression) {
      BiExpression biExpr = (BiExpression) expr;
      return biExpr.operator.doOp(evaluate(s, biExpr.lExpression, memory, back),
          evaluate(s, biExpr.rExpression, memory, back));
    } else { // Val
      Val val = (Val) expr;

      if (val.isFloat) {
        return ((Val) expr).val;
      } else {
        Float f = memory.get(val);

        if (f != null) {
          return f;
        } else {
          ArrayList<Float> args = new ArrayList<>();

          for (Expression e : val.args) {
            args.add(evaluate(s, e, memory, back));
          }

          args.add((float) back);

          float result = s.history.func(val.func, args, val.type, val.rest);

          memory.put(val, result);

          return result;
        }
      }
    }
  }
}
