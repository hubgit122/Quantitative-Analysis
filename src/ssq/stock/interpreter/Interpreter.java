package ssq.stock.interpreter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import ssq.stock.DateData;
import ssq.stock.Stock;
import ssq.stock.analyser.Analyzer;
import ssq.stock.analyser.ReflectTreeBuilder.CompositeRule;
import ssq.stock.analyser.ReflectTreeBuilder.Val;
import ssq.stock.gui.GUI;
import ssq.stock.gui.RecordHistory;
import ssq.utils.DirUtils;
import ssq.utils.Pair;
import ssq.utils.TreeNode;

public class Interpreter extends Analyzer {
  File outFile;
  int maxInfo;
  /**
   * 回溯的交易日数或者回溯到的日期(yyyyMMdd)<br>
   * 如果大于19000000, 认为是回溯到的日期, 否则为回溯的交易日数<br>
   * 按交易日回溯时, 股票停牌的交易日不计入回溯日数, 也就是说, 会回溯到停牌之前. 而按日期回溯后的选股结果则会还原在当天收盘后回溯0日选股的结果
   */
  int back = 0;
  float minGrade;
  public final CompositeRule AST;
  String instruction;
  String outputDir;

  /**
   * 初始化选股器
   *
   * @param max
   * @param min
   * @param days
   * @throws IOException
   */
  public Interpreter(Integer max, Float min, Integer days, String insturction) throws IOException {
    this(max, min, days, insturction, "assets/query_history", Stock.filter);
  }

  /**
   * 初始化规定了输出文件和股票代码过滤器的选股器
   *
   * @param max
   * @param min
   * @param days
   * @param outDir
   * @throws IOException
   */
  public Interpreter(Integer max, Float min, Integer date, String insturction, String outDir, String filter)
      throws IOException {
    super(filter);

    this.outputDir = outDir;
    this.maxInfo = max;
    this.minGrade = min / 100;

    this.back = date;
    this.instruction = insturction;
    AST = (CompositeRule) parser.getRoot(instruction);
  }

  @Override
  public void run() throws Exception {
    evals.clear();

    super.run();

    if (evals.size() > maxInfo) {
      evals = new Evaluations(evals.subList(0, maxInfo));
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");

    outFile = new File(DirUtils.getXxRoot(outputDir), simpleDateFormat.format(new Date()) + "@" + back);

    print();
  }

  @Override
  public void scan(Stock s) {
    boolean exclude = false;

    if (!AST.includeStop) {
      try {
        if (s.history.getLastStoredDate() < DateData.dateToNumber(s.history.getLastDownloadableDate())) //如果历史数据
        {
          String tmp = s.queryLatest();
          if (tmp.indexOf(',') == -1 || Float.valueOf(tmp.split(",")[1]) == 0f) //开盘价是0说明今天停牌了
          {
            exclude = true;
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    HashMap<Val, Float> memory = new HashMap<>();
    TreeNode<Float> result = evaluate(s, AST, memory, back);

    if (exclude) {
      result.setElement(-Math.abs(result.getElement())); //如果要排除, 变成负分
    }

    if (result.getElement() >= minGrade) {
      if (result.getElement() < 0) {
        System.err.println(result);
      }
      synchronized (evals) {
        evals.add(new Pair<Integer, TreeNode<Float>>(s.getCode(), result));
      }
    }
    memory.clear();
  }

  private void print() throws IOException {
    ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
    try {
      o.writeObject(new RecordHistory(AST, evals));
    } catch (IOException e) {
      e.printStackTrace();
      GUI.statusText(e.getLocalizedMessage());
    }

    try {
      o.close();
    } catch (IOException e) {
    }
  }

  public static class Evaluations extends LinkedList<Pair<Integer, TreeNode<Float>>> implements Serializable {
    private static final long serialVersionUID = 1L;

    public Evaluations(List<Pair<Integer, TreeNode<Float>>> subList) {
      super(subList);
    }

    public Evaluations() {
    }

    @Override
    public boolean add(Pair<Integer, TreeNode<Float>> e) {

      if (this.size() == 0) {
        addFirst(e);
      }
      if (e.getValue().getElement() > this.get(0).getValue().getElement()) {
        addFirst(e);
        return true;
      }

      for (ListIterator<Pair<Integer, TreeNode<Float>>> iterator = listIterator(); iterator.hasNext();) {
        Pair<Integer, TreeNode<Float>> node = iterator.next();

        if (e.getValue().getElement() > node.getValue().getElement()) {
          iterator.previous();
          iterator.add(e);
          return true;
        }
      }

      addLast(e);
      return true;
    };
  }

  public Evaluations evals = new Evaluations();
}
