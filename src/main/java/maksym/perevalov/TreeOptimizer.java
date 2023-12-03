package maksym.perevalov;

import static maksym.perevalov.MathElement.*;

import java.util.Collections;
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
        transformSubtractionToAddition(root);
        transformDivisionToMultiplication(this.root);
        this.root = balanceAdditions(this.root);
        this.root = balanceMultiplications(this.root);
        this.root = optimizeZeroExpressions(this.root);
        this.root = optimizeOneExpressions(this.root);
        this.root = foldNumbers(this.root);
        return root;
    }

    private TreeNode foldNumbers(TreeNode node) {
        if (node == null) return null;
        node.setLeft(foldNumbers(node.left()));
        node.setRight(foldNumbers(node.right()));
        if (isNumber(node.left()) && isNumber(node.right())) {
            var compute = node.compute(new MathContext(List.of()));
            return new TreeNode(new MNumber(Double.toString(compute)));
        }
        return node;
    }

    private TreeNode optimizeZeroExpressions(TreeNode node) {
        if (node == null) return null;
        node.setLeft(optimizeZeroExpressions(node.left()));
        node.setRight(optimizeZeroExpressions(node.right()));
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
                    negate(node.right());
                    return node.right();
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
        node.setLeft(optimizeOneExpressions(node.left()));
        node.setRight(optimizeOneExpressions(node.right()));
        switch (node.value()) {
            case Multiply _ -> {
                if (isPositiveOne(node.left())) return node.right();
                if (isNegativeOne(node.left())) {
                    negate(node.right());
                    return node.right();
                }

                if (isPositiveOne(node.right())) return node.left();
                if (isNegativeOne(node.right())) {
                    negate(node.left());
                    return node.left();
                }
            }
            case Divide _ -> {
                if (isPositiveOne(node.right())) return node.left();
                if (isNegativeOne(node.right())) {
                    negate(node.left());
                    return node.left();
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
        if (node == null) {
            return node;
        }
        if (!(node.value() instanceof Plus)) {
            node.setLeft(balanceAdditions(node.left()));
            node.setRight(balanceAdditions(node.right()));
            return node;
        }
        var result = node.collectPluses();
        var root = new TreeNode(new Plus(), null, null);
        for (int i = 1; i < result.total(); i++) {
            root.insert(new TreeNode(new Plus(), null, null));
        }
        result.leafs().stream()
              .peek(leaf -> {
                  leaf.setLeft(balanceAdditions(leaf.left()));
                  leaf.setRight(balanceAdditions(leaf.right()));
              })
              .sorted(Comparator.comparing(TreeNode::weight).reversed())
              .forEach(root::insert);
        return root;
    }

    private static TreeNode balanceMultiplications(TreeNode node) {
        if (node == null || !(node.value() instanceof Multiply)) {
            return node;
        }
        var result = node.collectMultiplications();
        var root = new TreeNode(new Multiply(), null, null);
        for (int i = 1; i < result.total(); i++) {
            root.insert(new TreeNode(new Multiply(), null, null));
        }
        result.leafs().stream()
              .peek(leaf -> {
                  leaf.setLeft(balanceMultiplications(leaf.left()));
                  leaf.setRight(balanceMultiplications(leaf.right()));
              })
              .sorted(Comparator.comparing(TreeNode::weight).reversed())
              .forEach(root::insert);
        return root;
    }

    private static void transformSubtractionToAddition(TreeNode node) {
        if (node == null) return;
        if (node.value() instanceof Minus && (isSubtractionOrAddition(node.left()) || isSubtractionOrAddition(node.right()))) {
            negate(node.right());
            node.setValue(new Plus());
        }
        transformSubtractionToAddition(node.left());
        transformSubtractionToAddition(node.right());
    }

    private static boolean isSubtractionOrAddition(TreeNode node) {
        return node != null && (node.value() instanceof Minus || node.value() instanceof Plus);
    }

    private static void transformDivisionToMultiplication(TreeNode node) {
        if (node == null) return;
        var oldRight = node.right();
        if (node.value() instanceof Divide && (isDivisionOrMultiplication(node.left()) || isDivisionOrMultiplication(node.right()))) {
            var newRight = new TreeNode(new Divide(), TreeNode.ofNumber(1.0), oldRight);
            node.setRight(newRight);
            node.setValue(new Multiply());
        }
        transformDivisionToMultiplication(node.left());
        transformDivisionToMultiplication(oldRight);
    }

    private static boolean isDivisionOrMultiplication(TreeNode node) {
        var isNull = node != null;
        var isDivisionOrMultiplication = node.value() instanceof Divide || node.value() instanceof Multiply;
        return isNull && isDivisionOrMultiplication;
    }

    private static void negate(TreeNode node) {
        switch (node.value()) {
            case MNumber number -> {
                var d = Double.parseDouble(number.value());
                node.setValue(new MNumber(Double.toString(-1 * d)));
            }
            case Varaible v -> {
                if (v.value().charAt(0) == '-') {
                    node.setValue(new Varaible(v.value().substring(1)));
                } else {
                    node.setValue(new Varaible("-" + v.value()));
                }
            }
            case Plus _, Minus _ -> {
                negate(node.left());
                negate(node.right());
            }
            case Divide _, Multiply _ -> negate(node.left());
            default -> throw new IllegalStateException("Unexpected value: " + node.value());
        }
    }
}
