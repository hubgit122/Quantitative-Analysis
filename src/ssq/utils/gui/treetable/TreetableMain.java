/*
 * Created on 2004-10-18
 * @author michaell
 */
package ssq.utils.gui.treetable;

import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

public class TreetableMain
{
    public static void main(String[] args)
    {
        new TreetableMain();
    }
    
    public TreetableMain()
    {
        JFrame frame = new JFrame("Tree Table");
        
        /**
         * step2: Set column names and types. This should be according to MyObject.setValueAt() method. Note: cTypes[0] can be set to any type because it will be set "cTypes[0] = TreeTableModel.class" in AbstractTreetableUserObject constructor.
         * */
        
        //step2-2: create DefaultMutableTreeNode include the MyObject
        String[] cNames = { "name", "size", "isFile" };
        //        Class[] cTypes = {String.class,Integer.class,Boolean.class};
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new MyObject(new File("c:\\")));
        root.add(new DefaultMutableTreeNode(new MyObject(new File("c:\\myjava"))));
        root.add(new DefaultMutableTreeNode(new MyObject(new File("c:\\myjava\\aaa.txt"))));
        /** or create DefaultMutableTreeNode include String */
        //        String[] cNames = {"name"};
        //        Class[] cTypes = null;
        //        DefaultMutableTreeNode root = new DefaultMutableTreeNode("c:\\");
        //        root.add(new DefaultMutableTreeNode("myjava"));
        //        root.add(new DefaultMutableTreeNode("myjava\\aaa.txt"));

        JComboBox combos = new JComboBox();
        combos.addItem("abc");
        combos.addItem("edf");
        
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(true);
        
        JTreetable treeTable = new JTreetable(new DefaultTreetableModel(cNames, root));
        treeTable.setColumnCheckBox(1, checkBox);

        JScrollPane pane = new JScrollPane(treeTable);
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.show();
    }
    
    //step1: create MyObject extend from AbstractTreetableUserObject
    class MyObject extends TreetableUserClass
    {
        public MyObject(File file)
        {
            super();
            //step1-1: set the value to every column
            this.setValueAt(0, file.getName());
            this.setValueAt(1, new Integer((int) file.length()));
            this.setValueAt(2, new Boolean(file.isFile()));
        }
        
        @Override
        public DefaultMutableTreeNode wrapOneNode(DefaultMutableTreeNode src)
        {
            return src;
        }
    }
}
