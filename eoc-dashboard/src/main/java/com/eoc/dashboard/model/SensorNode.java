package com.eoc.dashboard.model;

import javafx.beans.property.*;

public class SensorNode {

    public enum NodeType {
        HEAD_EOC, LOCAL, NEIGHBOR;

        public String toCssClass() {
            return switch (this) {
                case HEAD_EOC -> "node-head";
                case LOCAL    -> "node-local";
                case NEIGHBOR -> "node-neighbor";
            };
        }

        public String toColorHex() {
            return switch (this) {
                case HEAD_EOC -> "#00ff88";
                case LOCAL    -> "#56b6c2";
                case NEIGHBOR -> "#e5a840";
            };
        }
    }

    public enum NodeStatus { OK, WARNING, ERROR, OFFLINE }

    private final StringProperty  id          = new SimpleStringProperty();
    private final StringProperty  nameEn      = new SimpleStringProperty();
    private final StringProperty  nameVn      = new SimpleStringProperty();
    private final ObjectProperty<NodeType>   type   = new SimpleObjectProperty<>();
    private final ObjectProperty<NodeStatus> status = new SimpleObjectProperty<>();
    private final DoubleProperty  latPx       = new SimpleDoubleProperty();
    private final DoubleProperty  lngPx       = new SimpleDoubleProperty();
    private final IntegerProperty battery     = new SimpleIntegerProperty();
    private final IntegerProperty signal      = new SimpleIntegerProperty();
    private final StringProperty  lastSeen    = new SimpleStringProperty();

    public SensorNode() {}

    public SensorNode(String id, String nameEn, String nameVn, NodeType type,
                      double latPx, double lngPx, NodeStatus status,
                      int battery, int signal, String lastSeen) {
        this.id.set(id);
        this.nameEn.set(nameEn);
        this.nameVn.set(nameVn);
        this.type.set(type);
        this.latPx.set(latPx);
        this.lngPx.set(lngPx);
        this.status.set(status);
        this.battery.set(battery);
        this.signal.set(signal);
        this.lastSeen.set(lastSeen);
    }

    // --- Property accessors ---
    public StringProperty  idProperty()       { return id; }
    public StringProperty  nameEnProperty()   { return nameEn; }
    public StringProperty  nameVnProperty()   { return nameVn; }
    public ObjectProperty<NodeType>   typeProperty()   { return type; }
    public ObjectProperty<NodeStatus> statusProperty() { return status; }
    public DoubleProperty  latPxProperty()    { return latPx; }
    public DoubleProperty  lngPxProperty()    { return lngPx; }
    public IntegerProperty batteryProperty()  { return battery; }
    public IntegerProperty signalProperty()   { return signal; }
    public StringProperty  lastSeenProperty() { return lastSeen; }

    // --- Getters ---
    public String     getId()       { return id.get(); }
    public String     getNameEn()   { return nameEn.get(); }
    public String     getNameVn()   { return nameVn.get(); }
    public NodeType   getType()     { return type.get(); }
    public NodeStatus getStatus()   { return status.get(); }
    public double     getLatPx()    { return latPx.get(); }
    public double     getLngPx()    { return lngPx.get(); }
    public int        getBattery()  { return battery.get(); }
    public int        getSignal()   { return signal.get(); }
    public String     getLastSeen() { return lastSeen.get(); }

    // --- Setters ---
    public void setStatus(NodeStatus s)   { status.set(s); }
    public void setBattery(int b)         { battery.set(b); }
    public void setSignal(int s)          { signal.set(s); }
    public void setLastSeen(String t)     { lastSeen.set(t); }
}
