package maksym.perevalov.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

public class Model {

    public static void main(String[] args) {
        var firstLayer = new ReadLayer(new ExecutionLayer(new ExecutionLayer(new WriteLayer())));
        var layers = firstLayer.toList();
        int tick = 0;
        int circle = 0;
        Operation last = null;
        int length = 0;
        printHeaders();
        while (tick < 10) {
            var list = layers.stream()
                  .map(Layer::tick)
                  .map(o -> o == null ? " " : o.complexity)
                  .toList();
            if (firstLayer.curr != null && firstLayer.curr != last) {
                last = firstLayer.curr;
                length = Math.max(length, firstLayer.curr.complexity);
                circle = 0;
            }
            print(tick, list);
            if (circle == length) {
                firstLayer.next(null);
                circle = 0;
            }
            circle++;
            tick++;
        }
    }

    private static void printHeaders() {
        System.out.printf("%-5s %-2s %-2s %-2s %-2s%n", "Tick", "R", "L1", "L2", "W");
    }

    private static void print(int tick, List<?> layer) {
        System.out.printf("%-5s %-2s %-2s %-2s %-2s%n", tick, layer.get(0), layer.get(1), layer.get(2), layer.get(3));
    }

    static class Operation {
        public int id;
        public int complexity;
        public int status;

        public Operation(int id, int complexity, int status) {
            this.id = id;
            this.complexity = complexity;
            this.status = status;
        }
    }

    enum OperationType {
        Addition(1, "+"),
        Subtraction(1, "-"),
        Multiplication(2, "*"),
        Division(2, "/");

        public final int complexity;
        public final String symbol;

        OperationType(int complexity, String symbol) {
            this.complexity = complexity;
            this.symbol = symbol;
        }
    }


    static abstract class Layer {
        protected final Layer next;
        protected Operation curr;

        protected Layer(Layer next) {
            this.next = next;
        }

        abstract public boolean next(Operation op);

        abstract public Operation tick();

        public List<Layer> toList() {
            return Stream.concat(Stream.of(this), next.toList().stream())
                  .toList();
        }

        public Operation current() {
            return curr;
        }

        boolean has() {
            return curr != null;
        }
    }

    static class ReadLayer extends Layer {
        private final Queue<Operation> storage = new LinkedList<>(List.of(
              new Operation(1, 1, 0),
              new Operation(2, 1, 0)
        ));

        ReadLayer(Layer next) {
            super(next);
        }

        @Override
        public boolean next(Operation op) {
            boolean result = next.next(curr);
            if (result) {
                curr = null;
            }
            return true;
        }

        @Override
        public Operation tick() {
            if (!this.has()) {
                curr = storage.poll();
            }
            return curr;
        }
    }

    static class ExecutionLayer extends Layer {
        ExecutionLayer(Layer next) {
            super(next);
        }

        @Override
        public boolean next(Operation op) {
            if (next.next(curr)) {
                curr = op;
                return true;
            }
            return false;
        }

        @Override
        public Operation tick() {
            return curr;
        }
    }

    static class WriteLayer extends Layer {
        protected WriteLayer() {
            super(null);
        }

        @Override
        public boolean next(Operation op) {
            curr = op;
            return true;
        }

        @Override
        public Operation tick() {
            return curr;
        }

        @Override
        public List<Layer> toList() {
            return List.of(this);
        }
    }
}
