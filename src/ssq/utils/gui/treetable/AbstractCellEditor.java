package ssq.utils.gui.treetable;

import java.util.EventObject;

import javax.swing.CellEditor;
import javax.swing.event.CellEditorListener;
import javax.swing.event.EventListenerList;

/*
 * Created on 2004-10-19
 * @author michaell
 */

public class AbstractCellEditor implements CellEditor
{
    protected EventListenerList listenerList = new EventListenerList();
    
    @Override
    public Object getCellEditorValue()
    {
        return null;
    }
    
    @Override
    public boolean shouldSelectCell(EventObject eo)
    {
        return false;
    }
    
    @Override
    public boolean isCellEditable(EventObject eo)
    {
        return true;
    }
    
    @Override
    public boolean stopCellEditing()
    {
        return true;
    }
    
    @Override
    public void cancelCellEditing()
    {
    }

    @Override
    public void addCellEditorListener(CellEditorListener l)
    {
        listenerList.add(CellEditorListener.class, l);
    }
    
    @Override
    public void removeCellEditorListener(CellEditorListener l)
    {
        listenerList.remove(CellEditorListener.class, l);
    }
}
