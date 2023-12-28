package maksym.perevalov.tree;

import static maksym.perevalov.tree.MathElement.*;

import java.util.Comparator;
import java.util.List;

public class TreeOptimizer {
    private TreeNode root;

    public TreeOptimizer(TreeNode root) {
        this.root = root;
    }

    public TreeNode optimize() {
        this.root = foldNumbers(this.root);
        this.root = optimizeZeroExpressions(this.root);
        this.root = optimizeOneExpressions(this.root);
        this.root = transformSubtractionToAddition(root);
        this.root = transformDivisionToMultiplication(this.root);
        this.root = balanceAdditions(this.root);
        this.root = optimizeWhenRightIsNegativeInAddition(this.root); // should be after balanceAdditions
        this.root = balanceMultiplications(this.root);
        this.root = optimizeWhenRightIsNegativeInAddition(this.root);
        this.root = optimizeZeroExpressions(this.root);
        this.root = optimizeOneExpressions(this.root);
        this.root = foldNumbers(this.root);
        return root;
    }

    private TreeNode foldNumbers(TreeNode node) {
        if (node == null) return null;
        node = node
              .withLeft(foldNumbers(node.left()))
              .withRight(foldNumbers(node.right()));
        if (isNumber(node.left()) && isNumber(node.right())) {
            var compute = node.compute(new MathContext(List.of()));
            return new TreeNode(new MNumber(Double.toString(compute)), null, null);
        }
        return node;
    }

    private TreeNode optimizeWhenRightIsNegativeInAddition(TreeNode node) {
        if (node == null) return null;
        node = node
              .withLeft(optimizeWhenRightIsNegativeInAddition(node.left()))
              .withRight(optimizeWhenRightIsNegativeInAddition(node.right()));
        if (node.value() instanceof MathElement.Plus) {
            if (node.right().value() instanceof MathElement.MNumber n && n.isNegative()) {
                return node
                      .withValue(new Minus())
                      .withRight(negate(node.right()));
            }

            if (node.right().value() instanceof MathElement.Varaible n && n.isNegative()) {
                return node
                      .withValue(new Minus())
                      .withRight(negate(node.right()));
            }
        }
        return node;
    }

    private TreeNode optimizeZeroExpressions(TreeNode node) {
        if (node == null) return null;
        node = node
              .withLeft(optimizeZeroExpressions(node.left()))
              .withRight(optimizeZeroExpressions(node.right()));
        switch (node.value()) {
            case Multiply _ -> {
                if (isZero(node.left()) || isZero(node.right())) {
                    return new TreeNode(MNumber.zero());
                }
            }
            case Divide _ -> {
                if (isZero(node.left())) {
                    return new TreeNode(MNumber.zero());
                }
            }
            case Minus _ -> {
                if (isZero(node.left())) {
                    return negate(node.right());
                }
                if (isZero(node.right())) return node.left();
            }
            case Plus _ -> {
                if (isZero(node.left())) return node.right();
                if (isZero(node.right())) return node.left();
            }
            default -> {
            }
        }
        return node;
    }

    public static boolean isNumber(TreeNode node) {
        if (node == null) return false;
        return node.value() instanceof MNumber;
    }

    private TreeNode optimizeOneExpressions(TreeNode node) {
        if (node == null) return null;
        node = node
              .withLeft(optimizeOneExpressions(node.left()))
              .withRight(optimizeOneExpressions(node.right()));
        switch (node.value()) {
            case Multiply _ -> {
                if (isPositiveOne(node.left())) return node.right();
                if (isNegativeOne(node.left())) {
                    return negate(node.right());
                }

                if (isPositiveOne(node.right())) return node.left();
                if (isNegativeOne(node.right())) {
                    return negate(node.left());
                }
            }
            case Divide _ -> {
                if (isPositiveOne(node.right())) return node.left();
                if (isNegativeOne(node.right())) {
                    return negate(node.left());
                }
            }
            default -> {
            }
        }
        return node;
    }

    public boolean isZero(TreeNode node) {
        if (node == null) return false;
        if (!(node.value() instanceof MNumber)) return false;
        var strings = List.of("-0", "0", "0.0", "-0.0");
        return strings.contains(node.value().value());
    }

    public boolean isPositiveOne(TreeNode node) {
        if (node == null) return false;
        if (!(node.value() instanceof MNumber)) return false;
        var strings = List.of("1", "1.0");
        return strings.contains(node.value().value());
    }

    public boolean isNegativeOne(TreeNode node) {
        if (node == null) return false;
        if (!(node.value() instanceof MNumber)) return false;
        var strings = List.of("-1", "-1.0");
        return strings.contains(node.value().value());
    }

    private static TreeNode balanceAdditions(TreeNode node) {
        if (node == null) return null;
        if (!(node.value() instanceof Plus)) {
            return node
                  .withLeft(balanceAdditions(node.left()))
                  .withRight(balanceAdditions(node.right()));
        }
        var result = node.collectPluses();
        var root = new TreeNode(new Plus(), null, null);
        root.setBrackets(result.brackets());
        for (int i = 1; i < result.total(); i++) {
            root = root.insert(new TreeNode(new Plus(), null, null));
        }
        var balancedLeafs = result.leafs().stream()
              .map(leaf -> leaf
                    .withLeft(balanceAdditions(leaf.left()))
                    .withRight(balanceAdditions(leaf.right()))
              )
              .sorted(Comparator.comparing(TreeNode::weight).reversed())
              .toList();
        for (TreeNode balancedLeaf : balancedLeafs) {
            root = root.insert(balancedLeaf);
        }
        return root;
    }

    private static TreeNode balanceMultiplications(TreeNode node) {
        if (node == null) return null;
        if (!(node.value() instanceof Multiply)) {
            return node
                  .withLeft(balanceMultiplications(node.left()))
                  .withRight(balanceMultiplications(node.right()));
        }
        var result = node.collectMultiplications();
        var root = new TreeNode(new Multiply(), null, null);
        root.setBrackets(result.brackets());
        for (int i = 1; i < result.total(); i++) {
            root = root.insert(new TreeNode(new Multiply(), null, null));
        }
        var balancedLeafs = result.leafs().stream()
              .map(leaf -> leaf
                    .withLeft(balanceMultiplications(leaf.left()))
                    .withRight(balanceMultiplications(leaf.right()))
              )
              .sorted(Comparator.comparing(TreeNode::weight).reversed())
              .toList();
        for (TreeNode balancedLeaf : balancedLeafs) {
            root = root.insert(balancedLeaf);
        }
        return root;
    }

    private static TreeNode transformSubtractionToAddition(TreeNode node) {
        if (node == null) return null;
        node = node
              .withLeft(transformSubtractionToAddition(node.left()))
              .withRight(transformSubtractionToAddition(node.right()));
        if (node.value() instanceof Minus && (isSubtractionOrAddition(node.left()) || isSubtractionOrAddition(node.right()))) {
            node = node
                  .withRight(negate(node.right()))
                  .withValue(new Plus());
        }
        return node;
    }

    private static boolean isSubtractionOrAddition(TreeNode node) {
        return node != null && (node.value() instanceof Minus || node.value() instanceof Plus);
    }

    private static TreeNode transformDivisionToMultiplication(TreeNode node) {
        if (node == null) return null;
        var oldRight = node.right();
        if (node.value() instanceof Divide && (isDivisionOrMultiplication(node.left()) || isDivisionOrMultiplication(node.right()))) {
            var newRight = new TreeNode(new Divide(), TreeNode.ofNumber(1.0), transformDivisionToMultiplication(oldRight));
            node = node
                  .withRight(newRight)
                  .withValue(new Multiply());
        }
        return node
              .withLeft(transformDivisionToMultiplication(node.left()));
    }

    private static boolean isDivisionOrMultiplication(TreeNode node) {
        var isNull = node != null;
        var isDivisionOrMultiplication = node.value() instanceof Divide || node.value() instanceof Multiply;
        return isNull && isDivisionOrMultiplication;
    }

    private static TreeNode negate(TreeNode node) {
        return switch (node.value()) {
            case MNumber number -> {
                var d = Double.parseDouble(number.value());
                yield node.withValue(new MNumber(Double.toString(-1 * d)));
            }
            case Varaible v -> {
                if (v.value().charAt(0) == '-') {
                    yield node.withValue(new Varaible(v.value().substring(1)));
                } else {
                    yield node.withValue(new Varaible("-" + v.value()));
                }
            }
            case Plus _, Minus _ -> node
                  .withLeft(negate(node.left()))
                  .withRight(negate(node.right()));
            case Divide _, Multiply _ -> node.withLeft(negate(node.left()));
            default -> throw new IllegalStateException("Unexpected value: " + node.value());
        };
    }
}
