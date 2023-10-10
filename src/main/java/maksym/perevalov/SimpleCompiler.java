package maksym.perevalov;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "run")
public class SimpleCompiler implements Callable<Integer> {
    private static final List<Character> OPERATIONS = List.of('-', '+', '*', '/');

    @Option(names = {"-e", "--expression"})
    private String expression = "SHA-256";

    @Override
    public Integer call()  {
        System.out.println("Your expression - "  + expression);
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new SimpleCompiler()).execute(args);
        System.exit(exitCode);
    }
}
