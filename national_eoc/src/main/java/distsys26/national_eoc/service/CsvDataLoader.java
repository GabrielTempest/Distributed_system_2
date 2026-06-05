package distsys26.national_eoc.service;

import distsys26.national_eoc.model.*;
import distsys26.national_eoc.model.SensorNode.NodeType;
import distsys26.national_eoc.model.SensorNode.NodeStatus;
import distsys26.national_eoc.model.Scenario.Severity;
import distsys26.national_eoc.model.KafkaQueue.QueueType;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Reads sensor_data.csv and populates the in-memory model objects.
 * Section headers look like  [NODES], [SCENARIOS], etc.
 * Lines starting with # are comments; blank lines are ignored.
 */
public class CsvDataLoader {

    private final Path csvPath;

    // Parsed results
    private final List<SensorNode>  nodes         = new ArrayList<>();
    private final List<Scenario>    scenarios     = new ArrayList<>();
    private final List<KafkaQueue>  kafkaQueues   = new ArrayList<>();

    // Raw log templates keyed by scenario-key (e.g. "TYPHOON_ALERT")
    private final Map<String, List<String>> logTemplates = new LinkedHashMap<>();

    public CsvDataLoader(Path csvPath) {
        this.csvPath = csvPath;
    }

    // ------------------------------------------------------------------ //
    //  Public API
    // ------------------------------------------------------------------ //

    public void load() throws IOException {
        nodes.clear();
        scenarios.clear();
        kafkaQueues.clear();
        logTemplates.clear();

        List<String> lines = Files.readAllLines(csvPath);
        String currentSection = "";

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1).toUpperCase();
                continue;
            }

            switch (currentSection) {
                case "NODES"              -> parseNode(line);
                case "SCENARIOS"          -> parseScenario(line);
                case "KAFKA_QUEUES"       -> parseKafkaQueue(line);
                case "EVENT_LOG_TEMPLATES"-> parseLogTemplate(line);
            }
        }

        // Attach log templates to scenarios
        for (Scenario s : scenarios) {
            String key = deriveTemplateKey(s.getName());
            s.setLogTemplates(logTemplates.getOrDefault(key, List.of()));
        }
    }

    public List<SensorNode> getNodes()       { return Collections.unmodifiableList(nodes); }
    public List<Scenario>   getScenarios()   { return Collections.unmodifiableList(scenarios); }
    public List<KafkaQueue> getKafkaQueues() { return Collections.unmodifiableList(kafkaQueues); }

    // ------------------------------------------------------------------ //
    //  Parsers
    // ------------------------------------------------------------------ //

    private void parseNode(String line) {
        // id, name_en, name_vn, type, lat_px, lng_px, status, battery, signal, last_seen
        String[] p = splitCsv(line, 10);
        if (p == null) return;
        try {
            nodes.add(new SensorNode(
                p[0].trim(),
                p[1].trim(),
                p[2].trim(),
                NodeType.valueOf(p[3].trim()),
                Double.parseDouble(p[4].trim()),
                Double.parseDouble(p[5].trim()),
                NodeStatus.valueOf(p[6].trim()),
                Integer.parseInt(p[7].trim()),
                Integer.parseInt(p[8].trim()),
                p[9].trim()
            ));
        } catch (Exception e) {
            System.err.println("Skipping bad NODE line: " + line + " → " + e.getMessage());
        }
    }

    private void parseScenario(String line) {
        // id, name, description, severity, target_node, kafka_topic, color_hex
        String[] p = splitCsv(line, 7);
        if (p == null) return;
        try {
            scenarios.add(new Scenario(
                p[0].trim(),
                p[1].trim(),
                p[2].trim(),
                Severity.valueOf(p[3].trim()),
                p[4].trim(),
                p[5].trim(),
                p[6].trim()
            ));
        } catch (Exception e) {
            System.err.println("Skipping bad SCENARIO line: " + line + " → " + e.getMessage());
        }
    }

    private void parseKafkaQueue(String line) {
        // topic_pattern, description, queue_type, partition_key, cascade
        String[] p = splitCsv(line, 5);
        if (p == null) return;
        try {
            kafkaQueues.add(new KafkaQueue(
                p[0].trim(),
                p[1].trim(),
                QueueType.valueOf(p[2].trim()),
                p[3].trim(),
                Boolean.parseBoolean(p[4].trim())
            ));
        } catch (Exception e) {
            System.err.println("Skipping bad KAFKA line: " + line + " → " + e.getMessage());
        }
    }

    private void parseLogTemplate(String line) {
        // SCENARIO_KEY,template text...
        int comma = line.indexOf(',');
        if (comma < 0) return;
        String key      = line.substring(0, comma).trim();
        String template = line.substring(comma + 1).trim();
        logTemplates.computeIfAbsent(key, k -> new ArrayList<>()).add(template);
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    /** Split CSV respecting that last fields may contain commas (description). */
    private String[] splitCsv(String line, int expectedCols) {
        String[] parts = line.split(",", expectedCols);
        if (parts.length < expectedCols) {
            System.err.println("Skipping line (expected " + expectedCols +
                               " cols, got " + parts.length + "): " + line);
            return null;
        }
        return parts;
    }

    /** Convert a scenario name like "Typhoon Alert" → "TYPHOON_ALERT" */
    private String deriveTemplateKey(String name) {
        return name.toUpperCase().replace(' ', '_').replaceAll("[^A-Z_]", "");
    }
}
