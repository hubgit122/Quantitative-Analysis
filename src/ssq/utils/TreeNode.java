package ssq.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TreeNode<T> implements Serializable
{
    protected T              element;
    protected TreeNode       parentNode = null;
    protected List<TreeNode> childList  = new ArrayList<>();

    public TreeNode(T element)
    {
        this.element = element;
    }

    public boolean isLeaf()
    {
        return childList.isEmpty();
    }

    /* 插入一个child节点到当前节点中 */
    public void addChildNode(TreeNode treeNode)
    {
        treeNode.setParentNode(this);
        childList.add(treeNode);
    }
    
    public void addChild(T element)
    {
        addChildNode(new TreeNode<T>(element));
    }
    
    /** 返回当前节点的祖先节点集合 */
    public List<TreeNode> getAncestors()
    {
        List<TreeNode> ancestors = new ArrayList<TreeNode>();

        for (TreeNode parentNode = this.getParentNode(); parentNode != null; parentNode = this.getParentNode())
        {
            ancestors.add(parentNode);
        }

        return ancestors;
    }

    /** 返回当前节点的父辈节点集合 */
    public List<TreeNode> getElders()
    {
        List<TreeNode> elderList = new ArrayList<TreeNode>();
        TreeNode parentNode = this.getParentNode();
        if (parentNode == null)
        {
            return elderList;
        }
        else
        {
            elderList.add(parentNode);
            elderList.addAll(parentNode.getSiblings());
            return elderList;
        }
    }
    
    /** 返回当前节点的同辈节点集合 */
    public List<TreeNode> getSiblings()
    {
        TreeNode parentNode = this.getParentNode();

        if (parentNode == null)
        {
            return new ArrayList<TreeNode>();
        }
        else
        {
            return parentNode.childList;
        }
    }

    /**
     * 返回当前节点的晚辈集合<br>
     * 把晚辈森林线序化
     */
    public List<TreeNode> getJuniors()
    {
        List<TreeNode> juniorList = new ArrayList<TreeNode>();
        List<TreeNode> childList = this.getChildList();
        
        if (childList == null)
        {
            return juniorList;
        }
        else
        {
            int childNumber = childList.size();
            for (int i = 0; i < childNumber; i++)
            {
                TreeNode junior = childList.get(i);
                juniorList.add(junior);
                juniorList.addAll(junior.getJuniors());
            }
            return juniorList;
        }
    }

    /** 返回当前节点的孩子集合 */
    public List<TreeNode> getChildList()
    {
        return childList;
    }

    /** 删除节点和它下面的晚辈 */
    public void deleteNode()
    {
        TreeNode parentNode = this.getParentNode();

        if (parentNode != null)
        {
            parentNode.deleteChildNode(this);
        }
    }

    /** 删除当前节点的某个子节点 */
    public void deleteChildNode(TreeNode child)
    {
        List<TreeNode> childList = this.getChildList();
        int childNumber = childList.size();

        for (int i = 0; i < childNumber; i++)
        {
            TreeNode that = childList.get(i);
            if (that == child)
            {
                childList.remove(i);
                return;
            }
        }
    }
    
    public static abstract class TraverseExecuter
    {
        public abstract void doSth(TreeNode node);
    }
    
    /** 深度优先遍历 */
    public void traverse(TraverseExecuter exec)
    {
        exec.doSth(this);

        if (!childList.isEmpty())
        {
            int childNumber = childList.size();
            for (int i = 0; i < childNumber; i++)
            {
                TreeNode child = childList.get(i);
                child.traverse(exec);
            }
        }
    }

    public void setChilds(List<TreeNode> childList)
    {
        this.childList.addAll(childList);
    }

    public void setChildList(List<TreeNode> childList)
    {
        this.childList = childList;
    }

    public TreeNode getParentNode()
    {
        return parentNode;
    }

    public void setParentNode(TreeNode parentNode)
    {
        this.parentNode = parentNode;
    }

    public T getElement()
    {
        return element;
    }

    public void setElement(T obj)
    {
        this.element = obj;
    }
}