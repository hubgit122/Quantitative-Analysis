package ssq.stock.interpreter;

import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import ssq.stock.interpreter.ReflectTreeBuilder.Val;

public class InterpreterTest
{
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    //    private Val getVal()
    //    {
    //        Val result = new Val();
    //        result.isFloat=false;
    //        result.args.add(e)
    //    }

    @Test
    public void test() throws Exception
    {
        //        HashMap<Val, Float> m = new Interpreter().memory;
        //        m.put(, value)
        Interpreter.main(new String[] { "min(250 ->1) < min(750 -> 251) && max(5 -> 1) > max(300 -> 6) && max(250 -> 1)/min(250 -> 1) <= 1.5" });
    }
}
