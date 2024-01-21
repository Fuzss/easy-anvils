package fuzs.easyanvils.client.gui.components;

import org.jetbrains.annotations.Nullable;

/**
 * This is not currently used, the idea is to create an undo history feature for the edit box
 * (not that it's too useful for the anvil screen, but more as a general experiment).
 */
public class TypeActionManager {
    private static final int MAX_STATES = 100;
    private final TypeState[] states = new TypeState[MAX_STATES];
    private int start, end, index;

    public void trySave(OpenEditBox editBox) {
        if (true) return;
        TypeState typeState = TypeState.from(editBox);
        if (typeState.hasChangedSignificantly(this.peek())) {
            this.push(typeState);
        }
    }

    public void undo(OpenEditBox editBox) {
        if (true) return;
        TypeState typeState = TypeState.from(editBox);
        if (typeState.hasChanged(this.peek())) {
            this.push(typeState);
            this.pop(editBox);
        }
        TypeState poppedState = this.pop(editBox);
        if (poppedState != null)
        poppedState.apply(editBox);
    }

    public void redo(OpenEditBox editBox) {
        if (true) return;
        TypeState typeState = this.pull();
        if (typeState != null) {
            typeState.apply(editBox);
        }
    }

    @Nullable
    public TypeState pull() {
        if (this.index == this.end) return null;
        this.index = cycleIndex(this.index, 1);
        return this.states[this.index];
    }

    public void push(TypeState typeState) {
        this.states[this.index] = typeState;
        this.index = this.end = cycleIndex(this.index, 1);
        if (this.start == this.end) {
            this.start = cycleIndex(this.start, 1);
        }
    }

    @Nullable
    public TypeState pop(OpenEditBox editBox) {
        if (this.start == this.index) return null;
        this.index = cycleIndex(this.index, -1);
        TypeState state = this.states[this.index];
        if (state != null && state.equals(TypeState.from(editBox))) return this.pop(editBox);
        return state;
    }

    public TypeState peek() {
        if (this.start == this.index) return TypeState.EMPTY;
        return this.states[cycleIndex(this.index, -1)];
    }

    private static int cycleIndex(int index, int adjustment) {
        return ((index + adjustment) % MAX_STATES + MAX_STATES) % MAX_STATES;
    }

    private record TypeState(String value, int displayPos, int cursorPos, int highlightPos) {

        public static final TypeState EMPTY = new TypeState("", 0, 0, 0);

        private static TypeState from(OpenEditBox editBox) {
            return new TypeState(editBox.value, editBox.displayPos, editBox.cursorPos, editBox.highlightPos);
        }

        boolean hasChanged(TypeState other) {
            return other == null || !this.value.equals(other.value);
        }

        boolean hasChangedSignificantly(TypeState other) {
            if (other == null) return true;
            if (this.value.equals(other.value)) return false;
            int valueDiff = this.value.length() - other.value.length();
            int cursorDiff = this.cursorPos - other.cursorPos;
            int highlightDiff = this.highlightPos - other.highlightPos;
            return valueDiff != cursorDiff || cursorDiff != highlightDiff;
        }

        void apply(OpenEditBox editBox) {
            editBox.value = this.value;
            editBox.displayPos = this.displayPos;
            editBox.cursorPos = this.cursorPos;
            editBox.highlightPos = this.highlightPos;
        }
    }
}
