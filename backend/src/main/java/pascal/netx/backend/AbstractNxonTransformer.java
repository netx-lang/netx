package pascal.netx.backend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.netx.util.print.NotepadWriter;

public abstract class AbstractNxonTransformer {
    protected final NotepadWriter notepad;
    protected final JsonNode json;

    protected AbstractNxonTransformer(NotepadWriter notepad, JsonNode json) {
        this.notepad = notepad;
        this.json = json;
    }

    public abstract void parse();
}
