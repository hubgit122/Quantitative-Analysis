package ssq.stock.simulator;

import java.util.Date;
import java.util.LinkedList;

import ssq.stock.simulator.Deal.Type;

public class Deals extends LinkedList<Deal>
{
    private static final long serialVersionUID = 1L;
    
    public int add(Date time, float price, int quantity, Type type, Simulator simulator)
    {
        int num = simulator.getNumber();
        super.add(new Deal(time, price, quantity, type, num));
        return num;
    }
}
