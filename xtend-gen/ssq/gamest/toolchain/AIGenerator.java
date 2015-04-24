package ssq.gamest.toolchain;

import java.io.File;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;

import ssq.stock.AIWeightExpression;
import ssq.stock.GeneratorSettings;
import ssq.utils.FileUtils;

@SuppressWarnings("all")
public class AIGenerator {
  public String template(final String weightExpression) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package ssq.gamest.game.");
    _builder.append(this.settings.gameName, "");
    _builder.append(".ai;");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("import ssq.gamest.game.ai.PokerAI;");
    _builder.newLine();
    _builder.append("import ssq.gamest.game.ai.PokerAiSettings;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public class ");
    _builder.append(this.settings.gameName, "");
    _builder.append("AI extends PokerAI");
    _builder.newLineIfNotEmpty();
    _builder.append("{");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("public ");
    _builder.append(this.settings.gameName, "    ");
    _builder.append("AI(PokerAiSettings s)");
    _builder.newLineIfNotEmpty();
    _builder.append("    ");
    _builder.append("{");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("super(s);");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("    ");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("@Override");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("protected double getTotalWeight(double sum, double hands, double residual)");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("{");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("return ");
    _builder.append(weightExpression, "        ");
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    _builder.append("    ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("    ");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("@Override");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("public Object act(Object status)");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("{");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("return null;");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    return _builder.toString();
  }
  
  private GeneratorSettings settings;
  
  public AIGenerator(final GeneratorSettings settings) {
    this.settings = settings;
  }
  
  public void run(final String weightExpression) {
    try {
      AIWeightExpression _aIWeightExpression = new AIWeightExpression();
      boolean _test = _aIWeightExpression.test(weightExpression);
      if (_test) {
        String code = this.template(weightExpression);
        final File dir = new File(((this.settings.genDir + this.settings.gameName) + "/ai"));
        dir.mkdirs();
        File _file = new File(dir, (this.settings.gameName + "AI.java"));
        FileUtils.saveString2File(_file, code);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
