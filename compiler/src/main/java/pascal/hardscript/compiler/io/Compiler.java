package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface Compiler {
    JsonNode compile() throws IOException;
}
