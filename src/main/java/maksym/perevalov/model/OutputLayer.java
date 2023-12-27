package maksym.perevalov.model;

import java.util.List;

class OutputLayer implements Layer {
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
