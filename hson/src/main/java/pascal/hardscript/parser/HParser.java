package pascal.hardscript.parser;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.exception.TodoException;
import pascal.hardscript.model.HDesign;

public class HParser {
    private static final Logger log = LoggerFactory.getLogger(HParser.class);

    public HDesign parse(JsonNode node) {
        log.error("HSON parsing not implemented");
        throw new TodoException("HSON parsing");
    }
}
