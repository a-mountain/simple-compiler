package maksym.perevalov.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import maksym.perevalov.tree.MathElement;
import maksym.perevalov.tree.TreeNode;

public class InstructionBuilder {

    private int id;
    private final Map<String, Integer> complexity;

    public InstructionBuilder(Map<String, Integer> complexity) {
        this.complexity = complexity;
    }

    public List<List<Instruction>> buildInstructions(TreeNode expression) {
        List<List<TreeNode>> operators = Stream.<List<TreeNode>>generate(ArrayList::new)
              .limit(expression.height())
              .collect(Collectors.toList());
        collectOperations(operators, 0, expression);
        return operators.stream()
              .filter(Predicate.not(List::isEmpty))
              .map(this::mapOperators)
              .toList();
    }

    private List<Instruction> mapOperators(List<TreeNode> nodes) {
        return nodes.stream()
              .map(node -> new Instruction(getId(), getComplexity(node.value()), node.toExpressionString()))
              .toList();
    }

    private void collectOperations(List<List<TreeNode>> order, int level, TreeNode node) {
        if (node == null) return;
        if (node.value() instanceof MathElement.Operator) {
            order.get(level).add(node);
        }
        collectOperations(order, level + 1, node.left());
        collectOperations(order, level + 1, node.right());
    }

    private int getId() {
        return id++;
    }

    private int getComplexity(MathElement element) {
        return complexity.get(element.value());
    }
}
