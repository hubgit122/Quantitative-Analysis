package ssq.stock.analyser;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import ssq.stock.DateData;
import ssq.stock.Stock;
import ssq.stock.analyser.ReflectTreeBuilder.RuleLevel;
import ssq.stock.analyser.ReflectTreeBuilder.Val;
import ssq.stock.analyser.ReflectTreeBuilder.ValueType;
import ssq.stock.gui.GUI;
import ssq.utils.TextExcelUtils;

public class FormulaTester extends Analyzer {
  final String formula;
  final float minAcceptable;
  final LinkedList<String> output = new LinkedList<>();
  final private RuleLevel AST;

  public FormulaTester(String formula, float minAcceptable) throws Exception {
    this.formula = formula.replace("\t", " ").replace("#", "").trim();
    this.AST = parser.getRoot(formula);
    this.minAcceptable = minAcceptable / 100.0f;
  }

  public static void main(String[] args) throws Exception {
    new FormulaTester(
        "max(1,1).highest == max(1,1).lowest @ 20 && max(1,1) - max(2,2) > max(2,2) * 0.1 && max(2,2).highest < max(1,1).lowest && max(30,2).highest < max(1,1)",
        90).scan(Stock.loadDayLineHistory(1));
  }

  @Override
  public void run() throws Exception {
    output.add("股票回测\t" + formula);
    output.add("股票编号\t成立时间\t分值\t目标收益%\t当日开盘\t当日收盘\t第二日开盘\t第二日收盘\t第三日开盘\t第三日收盘");
    super.run();

    GUI.statusText("写入回测excel");
    FileOutputStream out = new FileOutputStream(new File("股票回测 " + new Date().getTime()) + ".xls");
    TextExcelUtils.toExcel(output, "\t").write(out);
    out.close();
  }

  @Override
  public void scan(Stock s) {
    try {
      int historyLen = s.history.size();

      for (int i = 0; i < historyLen; i++) {
        Float result = evaluate(s, AST, new HashMap<Val, Float>(), historyLen - i).getElement();
        if (result > minAcceptable) {
          try {
            DateData thisDay = s.history.get(i - 1), theNextDay = s.history.get(i), nextNextDay = s.history.get(i + 1);
            StringBuilder builder = new StringBuilder();

            builder.append(s.getCodeString()).append('\t').append(thisDay.date).append("\t").append(result * 100)
                .append("\t");
            builder.append((nextNextDay.getScaledVal(ValueType.opening) - theNextDay.getScaledVal(ValueType.opening))
                / theNextDay.getScaledVal(ValueType.opening) * 100).append("\t");

            builder.append(thisDay.getVal(ValueType.opening)).append('\t');
            builder.append(thisDay.getVal(ValueType.closing)).append('\t');
            builder.append(theNextDay.getVal(ValueType.opening)).append('\t');
            builder.append(theNextDay.getVal(ValueType.closing)).append('\t');

            builder.append(nextNextDay.getVal(ValueType.opening)).append('\t');
            builder.append(nextNextDay.getVal(ValueType.closing)).append('\t');

            synchronized (output) {
              output.add(builder.toString());
            }
          } catch (Exception e) {
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}