package maksym.perevalov.model;

import java.util.List;
import java.util.Map;

import maksym.perevalov.parser.BracketsContext;
import maksym.perevalov.parser.ErrorCollector;
import maksym.perevalov.parser.SyntaxParser;
import maksym.perevalov.parser.Tokenizer;
import maksym.perevalov.tree.InfixToPostfixTransformer;
import maksym.perevalov.tree.MathContext;
import maksym.perevalov.tree.TreeBuilder;
import maksym.perevalov.tree.TreeNode;
import maksym.perevalov.tree.TreeOptimizer;

public class App {

    public static void main(String[] args) {
        var context = new MathContext(List.of("fn"));
        var complexity = Map.ofEntries(
              Map.entry("+", 1),
              Map.entry("-", 2),
              Map.entry("*", 4),
              Map.entry("/", 8)
        );
        var expression = expression("a-b*(k-t+(f-g)*(f*5.9-q)+(w-y*(m-1))/p)-(x-3)*(x+3)/(d+q-w)", context);
        var instructionBuilder = new InstructionBuilder(complexity);
        var instructions = instructionBuilder.buildInstructions(expression);
        int sequentialSpeed = sequentialComplexity(instructions);
        printInstructions(instructions);
        var processor = new Processor();
        for (List<Instruction> instructionSet : instructions.reversed()) {
            processor.run(instructionSet);
        }
        System.out.println("### Statistics");
        System.out.println("sequential speed = " + sequentialSpeed);
        var parallelSpeed = processor.tick;
        System.out.println("parallel speed = " + parallelSpeed);
        System.out.println("speedup = " + (double) sequentialSpeed / parallelSpeed);
        System.out.println("pipeline load = " + (double) sequentialSpeed / (2.0 * parallelSpeed));
    }

    private static int sequentialComplexity(List<List<Instruction>> instructions) {
        var sum = instructions.stream().flatMap(List::stream).mapToInt(i -> i.complexity).sum();
        return sum * 4;
    }

    public static TreeNode expression(String input, MathContext context) {
        var errorCollector = new ErrorCollector();
        var tokenizer = new Tokenizer(context, errorCollector);
        var tokens = tokenizer.tokenize(input);
        var parser = new SyntaxParser(tokens, new BracketsContext(errorCollector), errorCollector);
        var syntaxTokens = parser.parse();
        var transformer = new InfixToPostfixTransformer();
        var postfix = transformer.transform(syntaxTokens);
        var treeBuilder = new TreeBuilder();
        var tree = treeBuilder.buildTree(postfix);
        var optimizer = new TreeOptimizer(treeBuilder.buildTree(postfix));
        return optimizer.optimize();
    }

    private static void printInstructions(List<List<Instruction>> instructions) {
        var flatten = instructions.stream().flatMap(List::stream).toList();
        System.out.println("### Instructions");
        for (Instruction instruction : flatten) {
            System.out.printf("id = %s, expr = '%s', complexity = %s%n", instruction.id, instruction.value, instruction.complexity);
        }
        System.out.println("### Computation order");
        var order = instructions.reversed().stream()
              .map(list -> list.stream()
                    .map(i -> i.id)
                    .toList())
              .toList();
        System.out.println(order);
    }
}
