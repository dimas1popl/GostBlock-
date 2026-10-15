package net.creater120.ghostblock.client.utils;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager {
    private static UndoManager instance;
    private final Deque<UndoAction> undoStack = new ArrayDeque<>();
    private final int maxSize;

    private UndoManager() {
        maxSize = ConfigManager.getInstance().getInt("maxUndo", 50);
    }

    public static UndoManager getInstance() {
        if (instance == null) {
            instance = new UndoManager();
        }
        return instance;
    }

    public void addAction(BlockPos pos, BlockState original, BlockState ghost) {
        if (undoStack.size() >= maxSize) {
            undoStack.removeLast();
        }
        undoStack.push(new UndoAction(pos, original, ghost));
    }

    public UndoAction popLast() {
        if (!undoStack.isEmpty()) {
            return undoStack.pop();
        }
        return null;
    }

    public int getSize() {
        return undoStack.size();
    }

    public static class UndoAction {
        public final BlockPos pos;
        public final BlockState original;
        public final BlockState ghost;

        public UndoAction(BlockPos pos, BlockState original, BlockState ghost) {
            this.pos = pos;
            this.original = original;
            this.ghost = ghost;
        }
    }
}