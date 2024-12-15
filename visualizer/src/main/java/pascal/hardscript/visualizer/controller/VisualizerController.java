package pascal.hardscript.visualizer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/")
class VisualizerController {
    private final ObjectMapper objectMapper;
    @Value("classpath:${circuit.path}")
    private Resource circuitPath;
    private JsonNode circuit;

    VisualizerController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void initializeCircuit() {
        try {
            circuit = objectMapper.readTree(circuitPath.getInputStream());

        } catch (IOException e) {
            throw new IllegalStateException("Failed to load circuit from path: " + circuitPath, e);
        }
    }

    @GetMapping("/graph")
    @ResponseBody
    public JsonNode getGraph() {
        return circuit;
    }
}
