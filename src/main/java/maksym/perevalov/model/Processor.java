package maksym.perevalov.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Processor {
    private final Memory memory;
    private final List<Layer> layers;
    int tick = 0;

    public Processor() {
        this.memory = new Memory();
        InputLayer layer = new InputLayer(memory, new ComputationLayer(new ComputationLayer(new ComputationLayer(new ComputationLayer(new OutputLayer(memory))))));
        this.layers = layer.collectLayers();
        printHeaders();
    }

    public void run(List<Instruction> instructions) {
        int length = instructions.stream()
              .map(i -> i.complexity)
              .max(Integer::compareTo)
              .get();
        for (Layer layer : layers) {
            layer.setInstructionLength(length);
        }
        memory.load(instructions);
        while (!memory.isEmpty()) {
            var states = layers.stream()
                  .map(Layer::toString)
                  .toList();
            print(tick, states);
            for (Layer layer : layers) {
                layer.tick();
            }
            int i = 0;
            while (layers.stream().map(Layer::state).anyMatch(s -> s.equals(State.Holding)) && i <= 50) {
                for (Layer layer : layers) {
                    layer.move();
                }
                i++;
            }
            tick++;
        }
        tick--;
    }

    public int getCurrentTick() {
        return tick;
    }

    public interface Layer {
        default boolean transmit(Instruction instruction) {
            return false;
        }

        default void setInstructionLength(int length) {
        }

        void tick();

        void move();

        List<Layer> collectLayers();

        State state();
    }


    public static class InputLayer implements Layer {
        private final Memory memory;
        private final Layer nextLayer;
        private State state = State.Reading;
        private Instruction instruction;

        public InputLayer(Memory memory, Layer nextLayer) {
            this.memory = memory;
            this.nextLayer = nextLayer;
        }

        @Override
        public void tick() {
            state = switch (state) {
                case Reading -> {
                    instruction = memory.read();
                    yield instruction == null ? State.Reading : State.Holding;
                }
                case Holding -> {
                    var isTransmitted = nextLayer.transmit(instruction);
                    if (isTransmitted) {
                        instruction = null;
                    }
                    yield isTransmitted ? State.Reading : State.Holding;
                }
                default -> throw new RuntimeException("not possible");
            };
        }

        @Override
        public void move() {
            if (state == State.Holding) {
                tick();
            }
        }

        @Override
        public List<Layer> collectLayers() {
            return Stream.concat(Stream.of(this), nextLayer.collectLayers().stream()).toList();
        }

        @Override
        public String toString() {
            return switch (state) {
                case Reading -> "R";
                case Empty -> "E";
                case Holding -> "H(%s)".formatted(instruction.toString());
                default -> throw new RuntimeException("not possible");
            };
        }

        @Override
        public State state() {
            return state;
        }
    }

    static class ComputationLayer implements Layer {
        private final Layer nextLayer;
        private State state = State.Empty;
        private Instruction instruction;
        private int currentProgress;
        private int instructionLength;

        public ComputationLayer(Layer nextLayer) {
            this.nextLayer = nextLayer;
        }

        @Override
        public void setInstructionLength(int length) {
            instructionLength = length;
        }

        @Override
        public boolean transmit(Instruction instruction) {
            var isEmpty = state == State.Empty;
            if (isEmpty) this.instruction = instruction;
            return isEmpty;
        }

        @Override
        public void tick() {
            state = switch (state) {
                case Empty -> instruction == null ? State.Empty : State.Computing;
                case Holding -> {
                    var isTransmitted = nextLayer.transmit(instruction);
                    if (isTransmitted) instruction = null;
                    yield isTransmitted ? State.Empty : State.Holding;
                }
                case Computing -> {
                    currentProgress++;
                    var nextState = currentProgress >= instructionLength ? State.Holding : State.Computing;
                    if (nextState == State.Holding) currentProgress = 0;
                    yield nextState;
                }
                default -> throw new RuntimeException("not possible");
            };
        }

        @Override
        public void move() {
            if (state == State.Holding || state == State.Empty) {
                tick();
            }
        }

        @Override
        public List<Layer> collectLayers() {
            return Stream.concat(Stream.of(this), nextLayer.collectLayers().stream()).toList();
        }

        @Override
        public String toString() {
            return switch (state) {
                case Empty -> "E";
                case Computing -> "C(%s)".formatted(instruction.toString());
                case Holding -> "H(%s)".formatted(instruction.toString());
                default -> throw new RuntimeException("not possible");
            };
        }

        @Override
        public State state() {
            return state;
        }
    }

    static class OutputLayer implements Layer {
        private final Memory memory;
        private State state = State.Empty;
        private Instruction instruction;

        public OutputLayer(Memory memory) {
            this.memory = memory;
        }

        @Override
        public boolean transmit(Instruction instruction) {
            var isEmpty = state == State.Empty;
            if (isEmpty) this.instruction = instruction;
            return isEmpty;
        }

        @Override
        public void tick() {
            state = switch (state) {
                case Empty -> instruction == null ? State.Empty : State.Writing;
                case Writing -> {
                    memory.write();
                    instruction = null;
                    yield State.Empty;
                }
                default -> throw new RuntimeException("not possible");
            };
        }

        @Override
        public void move() {
            if (state == State.Empty) {
                tick();
            }
        }

        @Override
        public List<Layer> collectLayers() {
            return List.of(this);
        }

        @Override
        public String toString() {
            return switch (state) {
                case Empty -> "E";
                case Writing -> "W(%s)".formatted(instruction.toString());
                default -> throw new RuntimeException("not possible");
            };
        }

        @Override
        public State state() {
            return state;
        }
    }

    public enum State {
        Empty,
        Writing,
        Computing,
        Reading,
        Holding
    }

    private static void printHeaders() {
        System.out.println("### Computation process");
        System.out.println("E - Empty, H - holding, W - writing, R - reading, C - computing");
        System.out.printf("%-5s %-4s %-4s %-4s %-4s %-4s %-4s%n", "Tick", "R", "L1", "L2", "L3", "L4", "W");
        System.out.println("----------------------");
    }

    private static void print(int tick, List<?> layer) {
        System.out.printf("%-5s %-4s %-4s %-4s %-4s %-4s %-4s%n", tick, layer.get(0), layer.get(1), layer.get(2), layer.get(3), layer.get(4), layer.get(5));
    }
}
