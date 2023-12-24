package maksym.perevalov.tree;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private MathElement value;
    private TreeNode left, right;
    private TreeNode notCompleted;

    public TreeNode(MathElement value, TreeNode left, TreeNode right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }

    public TreeNode(MathElement value) {
        this.value = value;
    }

    public static TreeNode ofNumber(Double number) {
        return new TreeNode(new MathElement.MNumber(number.toString()));
    }

    public boolean isCompleted() {
        return left != null && right != null;
    }

    public static boolean hasFree(TreeNode node) {
        if (node == null) return false;
        if (node.value() instanceof MathElement.Value) return false;
        if (!node.hasLeft() || !node.hasRight()) return true;
        return hasFree(node.left) || hasFree(node.right);
    }

    public void insert(TreeNode node) {
        if (notCompleted != null) {
            if (!notCompleted.hasRight()) {
                notCompleted.setRight(node);
            }
            if (!notCompleted.hasLeft()) {
                notCompleted.setLeft(node);
            }
            notCompleted = null;
            return;
        }
        insert(this, node, this);
    }

    private static void insert(TreeNode current, TreeNode target, TreeNode root) {
        if (!current.hasLeft()) {
            current.left = target;
            if (!current.isCompleted()) {
                root.setNotCompleted(current);
            }
            return;
        }
        if (!current.hasRight()) {
            current.right = target;
            if (!current.isCompleted()) {
                root.setNotCompleted(current);
            }
            return;
        }
        if (hasFree(current.left) && weight(current.left) <= weight(current.right)) {
            insert(current.left, target, root);
        } else if (hasFree(current.right) && weight(current.right) < weight(current.left)) {
            insert(current.right, target, root);
        } else if (hasFree(current.left)) {
            insert(current.left, target, root);
        } else if (hasFree(current.right)) {
            insert(current.right, target, root);
        } else {
            throw new RuntimeException("Can't insert value = " + current);
        }

    }

    public Result collectPluses() {
        var leafs = new ArrayList<TreeNode>();
        int total = collectPluses(leafs, this);
        return new Result(leafs, total);
    }

    private int collectPluses(List<TreeNode> leafs, TreeNode node) {
        if (node != null && node.value() instanceof MathElement.Plus) {
            return 1 + collectPluses(leafs, node.right) + collectPluses(leafs, node.left);
        } else {
            leafs.add(node);
            return 0;
        }
    }

    public Result collectMultiplications() {
        var leafs = new ArrayList<TreeNode>();
        int total = collectMultiplications(leafs, this);
        return new Result(leafs, total);
    }

    private static int collectMultiplications(List<TreeNode> leafs, TreeNode node) {
        if (node != null && node.value() instanceof MathElement.Multiply) {
            return 1 + collectMultiplications(leafs, node.right) + collectMultiplications(leafs, node.left);
        } else {
            leafs.add(node);
            return 0;
        }
    }

    public int height() {
        return height(this);
    }

    private static int height(TreeNode node) {
        if (node == null) {
            return 0;
        } else {
            int leftHeight = height(node.left);
            int rightHeight = height(node.right);
            return Math.max(leftHeight, rightHeight) + 1;
        }
    }

    public int weight() {
        return weight(this);
    }

    private static int weight(TreeNode node) {
        if (node == null) {
            return 0;
        } else {
            int left = weight(node.left);
            int right = weight(node.right);
            return 1 + left + right;
        }
    }

    public double compute(MathContext context) {
        return compute(this, context);
    }

    private double compute(TreeNode node, MathContext context) {
        if (node == null)
            return 0;
        return switch (node.value()) {
            case MathElement.MNumber v -> Double.parseDouble(v.value());
            case MathElement.Plus _ -> compute(node.left(), context) + compute(node.right(), context);
            case MathElement.Minus _ -> compute(node.left(), context) - compute(node.right(), context);
            case MathElement.Multiply _ -> compute(node.left(), context) * compute(node.right(), context);
            case MathElement.Divide _ -> compute(node.left(), context) / compute(node.right(), context);
            case MathElement.Function function -> 0.0;
            case MathElement.Varaible v -> context.readVariable(v.value());
            case null -> 0.0;
        };
    }

    public boolean isValue() {
        return value instanceof MathElement.Value;
    }

    public boolean hasLeft() {
        return left != null;
    }

    public boolean hasRight() {
        return right != null;
    }

    public MathElement value() {
        return value;
    }

    public TreeNode left() {
        return left;
    }

    public TreeNode right() {
        return right;
    }

    public void setValue(MathElement value) {
        this.value = value;
    }

    public void setLeft(TreeNode left) {
        this.left = left;
    }

    public void setRight(TreeNode right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return value.value();
    }

    public void setNotCompleted(TreeNode notCompleted) {
        this.notCompleted = notCompleted;
    }

    record Result(List<TreeNode> leafs, int total) {

    }
}
