package maksym.perevalov.model;

import java.util.List;
import java.util.stream.Stream;

public class InputLayer implements Layer {
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
