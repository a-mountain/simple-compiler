package maksym.perevalov.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Memory {
    private final Queue<Instruction> storage;
    private int size;
    private int writes;

    public Memory() {
        this.storage = new LinkedList<>();
    }

    public Instruction read() {
        return storage.poll();
    }

    public void write() {
        writes++;
    }

    public boolean isEmpty() {
        return writes == size;
    }

    public void load(List<Instruction> instructions) {
        storage.addAll(instructions);
        size = storage.size();
        writes = 0;
    }
}
