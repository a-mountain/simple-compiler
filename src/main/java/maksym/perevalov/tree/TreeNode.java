package maksym.perevalov.tree;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private final MathElement value;
    private final TreeNode left, right;
    private final boolean isBrackets;

    public TreeNode(MathElement value) {
        this(value, null, null);
    }

    public TreeNode(MathElement value, TreeNode left, TreeNode right) {
        this(value, left, right, false);
    }

    public TreeNode(MathElement value, TreeNode left, TreeNode right, boolean isBrackets) {
        this.value = value;
        this.left = left;
        this.right = right;
        this.isBrackets = isBrackets;
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

    public TreeNode insert(TreeNode node) {
        return insert(this, node, this);
    }

    private static TreeNode insert(TreeNode current, TreeNode target, TreeNode root) {
        if (!current.hasLeft()) {
            if (!current.isCompleted()) {
                root.setNotCompleted(current);
            }
            return current.withLeft(target);
        }
        if (!current.hasRight()) {
            if (!current.isCompleted()) {
                root.setNotCompleted(current);
            }
            return current.withRight(target);
        }
        if (hasFree(current.left) && weight(current.left) <= weight(current.right)) {
            return current.withLeft(insert(current.left, target, root));
        } else if (hasFree(current.right) && weight(current.right) < weight(current.left)) {
            return current.withRight(insert(current.right, target, root));
        } else if (hasFree(current.left)) {
            return current.withLeft(insert(current.left, target, root));
        } else if (hasFree(current.right)) {
            return current.withRight(insert(current.right, target, root));
        } else {
            throw new RuntimeException("Can't insert value = " + current);
        }
    }

    public Result collectPluses() {
        var leafs = new ArrayList<TreeNode>();
        int total = collectPluses(leafs, this);
        return new Result(leafs, total, this.isBrackets);
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
        return new Result(leafs, total, this.isBrackets);
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
            case MathElement.Function function -> {
                System.out.println("Compute (function)");
                yield 0.0;
            }
            case MathElement.Varaible v -> context.readVariable(v.value());
            case null -> {
                System.out.println("Compute (null)");
                yield 0.0;
            }
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

    @Override
    public String toString() {
        return value.value();
    }

    public String toExpressionString() {
        var s = switch (value) {
            case MathElement.Function operator ->
                  "%s(%s, %s)".formatted(operator.value(), left().toExpressionString(), right().toExpressionString());
            case MathElement.Operator operator ->
                  "%s %s %s".formatted(left().toExpressionString(), operator.value(), right().toExpressionString());
            case MathElement.Value v -> v.value();
        };
        return isBrackets ? "(" + s + ")" : s;
    }

    public TreeNode withLeft(TreeNode newLeft) {
        return new TreeNode(this.value, newLeft, this.right, this.isBrackets);
    }

    public TreeNode withRight(TreeNode newRight) {
        return new TreeNode(this.value, this.left, newRight, this.isBrackets);
    }

    public TreeNode withValue(MathElement newValue) {
        return new TreeNode(newValue, this.left, this.right, this.isBrackets);
    }

    public TreeNode withBrackets(boolean newIsBrackets) {
        return new TreeNode(this.value, this.left, this.right, newIsBrackets);
    }

    public void setNotCompleted(TreeNode notCompleted) {
    }

    public boolean isBrackets() {
        return isBrackets;
    }

    public record Result(List<TreeNode> leafs, int total, boolean brackets) { }
}
