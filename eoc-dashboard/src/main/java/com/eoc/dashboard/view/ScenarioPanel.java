package com.eoc.dashboard.view;

import com.eoc.dashboard.model.Scenario;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;

import java.util.List;
import java.util.function.Consumer;

public class ScenarioPanel extends VBox {

    private Consumer<Scenario> onScenarioSelected;
    private HBox     activeCard     = null;
    private Scenario activeScenario = null;

    public ScenarioPanel() {
        setSpacing(8);
        setPadding(new Insets(20, 20, 16, 20));
        setStyle("-fx-background-color: #0a1628;");
        setBorder(new Border(new BorderStroke(
            Color.web("#1a2e42"), BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY, new BorderWidths(0, 0, 1, 0)
        )));
        getChildren().add(sectionHeader("RUN SCENARIO"));
    }

    public void setOnScenarioSelected(Consumer<Scenario> h) { onScenarioSelected = h; }

    public void loadScenarios(List<Scenario> scenarios) {
        getChildren().subList(1, getChildren().size()).clear();
        scenarios.forEach(s -> getChildren().add(buildCard(s)));
    }

    private HBox buildCard(Scenario scenario) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setCursor(Cursor.HAND);
        card.setMaxWidth(Double.MAX_VALUE);
        styleCard(card, scenario.getColorHex(), false);

        Circle icon = new Circle(7);
        icon.setFill(Color.web(scenario.getColorHex()));

        VBox text = new VBox(4);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label name = new Label(scenario.getName());
        name.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        name.setTextFill(Color.web(scenario.getColorHex()));

        Label desc = new Label(scenario.getDescription());
        desc.setFont(Font.font("Monospace", 11));
        desc.setTextFill(Color.web("#5a8090"));

        text.getChildren().addAll(name, desc);

        Label badge = new Label(scenario.getSeverityLabel());
        badge.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web(scenario.getColorHex()));
        badge.setPadding(new Insets(3, 8, 3, 8));
        badge.setStyle(
            "-fx-background-color: " + scenario.getColorHex() + "22;" +
            "-fx-border-color: "     + scenario.getColorHex() + "66;" +
            "-fx-border-radius: 4; -fx-background-radius: 4;"
        );

        card.getChildren().addAll(icon, text, badge);

        card.setOnMouseEntered(e -> { if (card != activeCard) styleCard(card, scenario.getColorHex(), true); });
        card.setOnMouseExited (e -> { if (card != activeCard) styleCard(card, scenario.getColorHex(), false); });
        card.setOnMouseClicked(e -> {
            if (activeCard != null && activeScenario != null)
                styleCard(activeCard, activeScenario.getColorHex(), false);
            activeCard = card; activeScenario = scenario;
            styleCard(card, scenario.getColorHex(), true);
            if (onScenarioSelected != null) onScenarioSelected.accept(scenario);
        });

        return card;
    }

    private void styleCard(HBox card, String hex, boolean active) {
        card.setStyle(
            "-fx-background-color: " + (active ? hex + "22" : "#0d1e2e") + ";" +
            "-fx-border-color: "     + (active ? hex + "88" : "#1a2e42") + ";" +
            "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;"
        );
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        l.setTextFill(Color.web("#4a8fa8"));
        l.setPadding(new Insets(0, 0, 6, 0));
        return l;
    }
}
