package maksym.perevalov;

import java.util.List;
import java.util.Map;

import maksym.perevalov.parser.BracketsContext;
import maksym.perevalov.parser.ErrorCollector;
import maksym.perevalov.parser.SyntaxParser;
import maksym.perevalov.parser.Tokenizer;
import maksym.perevalov.tree.Display;
import maksym.perevalov.tree.InfixToPostfixTransformer;
import maksym.perevalov.tree.MathContext;
import maksym.perevalov.tree.TreeBuilder;
import maksym.perevalov.tree.TreeOptimizer;

public class Lab2 {
    /**
     * 1)+ a+b+c+d+e+f+g+h | h = 3
     * 2)+ a-b-c-d-e-f-g-h
     * 3)+ a+(b+c+d+(e+f)+g)+h
     * 4)+ a-((b-c-d)-(e-f)-g)-h
     * 5)+ a/b/c/d/e/f/g/h
     * 6)+? a*b - b*c - c*d - a*c*(b-d/e/f/g) - (g - h) - (i-j)
     * 7)+ 5040/8/7/6/5/4/3/2 (0.125)
     * 8)+ 10-9-8-7-6-5-4-3-2-1 (-35)
     * 9)+ 64-(32-16)-8-(4-2-1) (39)
     * 10) 3^3^3^3^3
     * 11)+? -i/1.0 + 0 - 0*k*h + 2 - 4.8/2 + 1*e/2
     * 12) a*2/0 + b/(b+b*0-1*b) - 1/(c*2*4.76*(1-2+1))
     * @param args
     */
    public static void main(String[] args) {
        var app = new Lab2();
        var context = new MathContext(List.of("sin", "cos"), Map.ofEntries(
              Map.entry("a", 1.0),
              Map.entry("b", 2.0),
              Map.entry("c", 3.0),
              Map.entry("d", 4.0),
              Map.entry("e", 5.0),
              Map.entry("f", 6.0),
              Map.entry("g", 7.0),
              Map.entry("h", 8.0),
              Map.entry("i", 9.0),
              Map.entry("k", 10.0),
              Map.entry("j", 11.0)
        )
        );
//        "5040/8/7/6/5/4/3/2"
        app.run("5040/8/7/6/5/4/3/2", context, true, true, true);
    }

    public void run(String input, MathContext context, boolean optimize, boolean showSimple, boolean showOptimized) {
        if (input.length() > 500) {
            System.out.println("Input string is too long");
            return;
        }
        if (input.isEmpty()) {
            System.out.println("Input string is empty");
            return;
        }
        var errorCollector = new ErrorCollector();
        var tokenizer = new Tokenizer(context, errorCollector);
        var tokens = tokenizer.tokenize(input);
        var parser = new SyntaxParser(tokens, new BracketsContext(errorCollector), errorCollector);
        var syntaxTokens = parser.parse();
        if (errorCollector.hasErrors()) {
            System.out.println("---Errors---");
            errorCollector.report().forEach(System.out::println);
        } else {
            System.out.println("---Syntax tokens---");
            syntaxTokens.forEach(System.out::println);
        }
        var transformer = new InfixToPostfixTransformer();
        var postfix = transformer.transform(syntaxTokens);
        System.out.println("---Postfix---");
        postfix.forEach(System.out::println);
        var treeBuilder = new TreeBuilder();
        var tree = treeBuilder.buildTree(postfix);
        var optimizer = new TreeOptimizer(treeBuilder.buildTree(postfix));
        if (showSimple) {
            System.out.println("---Default tree---");
            Display.displayTree(tree);
        }
        var optimizedTree = optimizer.optimize();
        if (showOptimized) {
            System.out.println("---Optimized tree---");
            Display.displayTree(optimizedTree);
        }
        System.out.printf("Computation result: Default = '%s' | Optimized = '%s'%n", tree.compute(context), optimizedTree.compute(context));
        System.out.printf("Tree height: Default = '%s' | Optimized = '%s'%n", tree.height(), optimizedTree.height());
    }
}
