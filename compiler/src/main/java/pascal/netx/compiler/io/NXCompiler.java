package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface NXCompiler {
    JsonNode compile() throws IOException;
}
