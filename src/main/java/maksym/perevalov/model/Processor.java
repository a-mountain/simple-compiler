package maksym.perevalov.model;

import java.util.List;

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
