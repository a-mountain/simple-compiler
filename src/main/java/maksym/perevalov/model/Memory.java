package maksym.perevalov.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Memory {
    private final Queue<Instruction> storage;

    public Memory(List<Instruction> storage) {
        this.storage = new LinkedList<>(storage);
    }

    public Instruction read() {
        return storage.poll();
    }

    public void write() {
        // do nothing :)
    }

    public boolean isEmpty() {
        return storage.isEmpty();
    }

}
