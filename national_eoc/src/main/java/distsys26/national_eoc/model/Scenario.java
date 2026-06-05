package distsys26.national_eoc.model;

import java.util.List;

public class Scenario {

    public enum Severity { CRITICAL, HIGH, MEDIUM, LOW }

    private final String   id;
    private final String   name;
    private final String   description;
    private final Severity severity;
    private final String   targetNodeId;
    private final String   kafkaTopic;
    private final String   colorHex;
    private List<String>   logTemplates;

    public Scenario(String id, String name, String description,
                    Severity severity, String targetNodeId,
                    String kafkaTopic, String colorHex) {
        this.id           = id;
        this.name         = name;
        this.description  = description;
        this.severity     = severity;
        this.targetNodeId = targetNodeId;
        this.kafkaTopic   = kafkaTopic;
        this.colorHex     = colorHex;
    }

    public String   getId()          { return id; }
    public String   getName()        { return name; }
    public String   getDescription() { return description; }
    public Severity getSeverity()    { return severity; }
    public String   getTargetNodeId(){ return targetNodeId; }
    public String   getKafkaTopic()  { return kafkaTopic; }
    public String   getColorHex()    { return colorHex; }

    public List<String> getLogTemplates()               { return logTemplates; }
    public void         setLogTemplates(List<String> t) { logTemplates = t; }

    public String getIcon() {
        return switch (severity) {
            case CRITICAL -> "⬤";
            case HIGH     -> "⬤";
            case MEDIUM   -> "⚡";
            case LOW      -> "ℹ";
        };
    }

    public String getSeverityLabel() {
        return switch (severity) {
            case CRITICAL -> "CRITICAL";
            case HIGH     -> "HIGH";
            case MEDIUM   -> "MEDIUM";
            case LOW      -> "LOW";
        };
    }
}
