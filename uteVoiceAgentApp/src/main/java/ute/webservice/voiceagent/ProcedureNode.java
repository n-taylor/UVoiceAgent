package ute.webservice.voiceagent;

import java.util.ArrayList;
import java.util.List;

/**
 * The node to build a tree of procedures and categories.
 * Created by Nathan Taylor on 3/26/2018.
 */

public class ProcedureNode {

    private String code;
    private String description;
    private List<ProcedureNode> children;
    private ProcedureNode parent;

    /**
     * Defines this node as a procedure, not a category.
     * @param code
     * @param description
     */
    public ProcedureNode(String code, String description){
        this.code = code;
        this.description = description;
        children = null;
    }

    /**
     * Defines this node as a category, not a procedure.
     * @param categoryName
     */
    public ProcedureNode(String categoryName){
        this.code = "";
        this.description = categoryName;
        children = new ArrayList<>();
    }

    /**
     * Determines whether this node describes a procedure or a category.
     * @return True if this node is a procedure. False otherwise.
     */
    public boolean isProcedure(){ return !code.isEmpty(); }

    /**
     * If this node is the root, returns null. Otherwise, returns the name of this node's parent.
     */
    public String getEnclosingCategoryName(){
        if (parent != null)
            return parent.description;
        else
            return null;
    }

    /**
     * Returns a list of all the children of this node.
     */
    public List<ProcedureNode> getChildren(){
        return children;
    }

    /**
     * If this node is a procedure, does nothing. Otherwise, adds a procedure to this node's children.
     * @param childCode The code of the procedure to add.
     * @param childDescription The description of the procedure to add.
     */
    public void addChild(String childCode, String childDescription){
        if (isProcedure())
            return;
        ProcedureNode toAdd = new ProcedureNode(childCode, childDescription);
        toAdd.parent = this;
        children.add(toAdd);

    }

    /**
     * If this node is a procedure, does nothing. Otherwise, adds a subcategory to this node's children.
     * @param categoryName The name of the new category to add.
     */
    public void addChild(String categoryName){
        if (isProcedure())
            return;
        ProcedureNode toAdd = new ProcedureNode(categoryName);
        toAdd.parent = this;
        children.add(toAdd);
    }

    /**
     * Sets the name or description of this node's parent.
     * @param parentName The new name of this node's parent.
     */
    public void setParentName(String parentName){
        parent.description = parentName;
    }

    /**
     * If this node is not a procedure, returns null. Otherwise returns this procedure's code.
     */
    public String getCode(){
        if (!isProcedure())
            return null;
        else
            return code;
    }

    /**
     * Returns this procedure's description or category name.
     */
    public String getDescription(){
        return description;
    }

    /**
     * Determines if this node has a child with the given category name or description.
     * @param identifier The category name or description to look for.
     * @return True if the given identifier is found.
     */
    public boolean containsChild(String identifier){
        for (ProcedureNode child : children){
            if (child.description.equals(identifier))
                return true;
        }
        return false;
    }

    /**
     * If the specified direct child does not exist, returns null. Otherwise, returns the child
     * @param identifier The category name or procedure description.
     */
    public ProcedureNode goTo(String identifier){
        for (ProcedureNode child : children){
            if (child.description.equals(identifier))
                return child;
        }
        return null;
    }

    /**
     * If this node is a procedure, does nothing. Otherwise, if this node does not contain a child
     * with the given category name, creates one.
     * @param categoryName
     */
    public void ensureChildCategory(String categoryName){
        if(isProcedure())
            return;
        if (!containsChild(categoryName))
            addChild(categoryName);
    }

    /**
     * Retrieves the code associated with the first found instance of the given description.
     * @param description The description of the procedure to find.
     * @return The code of the given procedure, or null if the given description is not a registered procedure.
     */
    public String findCode(String description){
        return findCode(this, description);
    }

    private String findCode(ProcedureNode node, String description){
        if (node.description.equals(description))
            return node.code;
        if (node.children != null) {
            for (ProcedureNode child : node.children) {
                String code = findCode(child, description);
                if (code != null)
                    return code;
            }
        }
        return null;
    }

    /**
     * Prints the tree to the console in pre-order.
     */
    public void printTree(){
        printTree(this, 0);
    }

    private void printTree(ProcedureNode node, int level){
        for (int i = 0; i < level; i++)
            System.out.print("- ");
        System.out.println(node.description);
        if (node.children == null)
            return;
        for (ProcedureNode child : node.children){
            printTree(child, level+1);
        }
    }

}
