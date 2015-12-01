import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Deviltech on 01.12.2015.
 */
public class UI extends Application {

    // Drag & Drop coordinates
    double originX;
    double originY;
    double translateX;
    double translateY;

    // Checkbox boolean
    boolean isAnimated;

    // Public IU fields
    TextField sequenceField;
    TextField bracketField;
    Pane drawPane;

    ArrayList<Circle> circleList = new ArrayList<>();

    final double[][][] coordsRepresentation = {new double[1][2]};

    // Circle Parameters
    double nodeSize = 10;
    int iterations = 20;


    @Override
    public void start(Stage primaryStage) throws Exception {

        // UI fields
        VBox mainBox = new VBox();
        sequenceField = new TextField(myLabels.TEXTAREA_SEQUENCE);
        bracketField = new TextField(myLabels.TEXTAREA_BRACKET);
        HBox buttonBox = new HBox();
        Button computeButton = new Button(myLabels.BUTTON_COMPUTE);
        Button drawButton = new Button(myLabels.BUTTON_DRAW);
        CheckBox animateChecker = new CheckBox(myLabels.CHECKBOX_ANIMATE);
        drawPane = new Pane();

        buttonBox.getChildren().addAll(computeButton, drawButton, animateChecker);
        mainBox.getChildren().addAll(sequenceField, bracketField, buttonBox, drawPane);

        // Compute Button disable
        sequenceField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.length() == 0) {
                        computeButton.setDisable(true);
                    } else {
                        computeButton.setDisable(false);
                    }
                }
        );

        // Animate Checkbox
        animateChecker.setOnAction((value) -> {
            if (animateChecker.isSelected() != isAnimated) {
                isAnimated = animateChecker.isSelected();
                // set Eventhandler für Drag & Drop
                for (Circle currentCircle : circleList) {
                    currentCircle.setOnMousePressed(isAnimated ? circleOnMousePressedEventHandler : null);
                    currentCircle.setOnMouseDragged(isAnimated ? circleOnMouseDraggedEventHandler : null);
                    currentCircle.setOnMouseReleased(isAnimated ? circleOnMouseReleasedEventHandler : null);
                }

            }
        });

        // computeButton
        computeButton.setOnAction((value) -> {
            bracketField.setText(new Nussinov(sequenceField.getText()).getBracketNotation());
        });


        // drawButton
        drawButton.setOnAction((value) -> {
            Graph myGraph = new Graph();
            try {
                myGraph.parseNotation(bracketField.getText());
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(myLabels.ALERT_TITLE);
                alert.setHeaderText(myLabels.ALTERT_MESSAGE);
                alert.setContentText(bracketField.getText());
                alert.showAndWait();
            }
            coordsRepresentation[0] = SpringEmbedder.computeSpringEmbedding(iterations, myGraph.getNumberOfNodes(), myGraph.getEdges(), null);
            SpringEmbedder.centerCoordinates(coordsRepresentation[0], 50, 550, 50, 550);
            drawShapes(drawPane, coordsRepresentation[0], myGraph.getEdges(), myGraph.getNumberOfNodes());
        });

        // prepare scene
        Scene scene = new Scene(mainBox, 600, 800);

        primaryStage.setScene(scene);
        primaryStage.setTitle(myLabels.CAPTION);

        // show scene
        primaryStage.show();


    }

    /**
     * Drawing function for Circles and Lines
     * @param drawPane
     * @param coords Circle Coordinates
     * @param edges array of edges
     * @param startIndex NumberOfNodes
     */
    private void drawShapes(Pane drawPane, double[][] coords, int[][] edges, int startIndex) {

        // clear previous pane
        drawPane.getChildren().clear();
        circleList = new ArrayList<>();

        // generate Circles
        for (int i = 0; i < coords.length; i++) {
            Circle currentCircle = new Circle(coords[i][0], coords[i][1], nodeSize, Color.BLACK);
            String toolTipText = "Node " + Integer.toString(i + 1);
            // expand toolTip text with nucleotide and Circle color, if possible
            if(sequenceField.getText().length() > i){
                toolTipText += ": " + sequenceField.getText().charAt(i);
                currentCircle.setFill(getNodeColor(sequenceField.getText().charAt(i)));
            }
            Tooltip.install(
                    currentCircle,
                    new Tooltip(toolTipText)
            );
            if (isAnimated) {
                currentCircle.setOnMousePressed(circleOnMousePressedEventHandler);
                currentCircle.setOnMouseDragged(circleOnMouseDraggedEventHandler);
                currentCircle.setOnMouseReleased(circleOnMouseReleasedEventHandler);
            }

            circleList.add(currentCircle);
        }


        // generate  basic Lines
        ArrayList<Line> lineList = new ArrayList<>();
        for (int i = 0; i < circleList.size() - 1; i++) {
            Line line = new Line();
            line.setStroke(Color.BLACK);
            line.setFill(Color.BLACK);
            Circle circle1 = circleList.get(i);
            Circle circle2 = circleList.get(i + 1);


            // bind ends of line:
            line.startXProperty().bind(circle1.centerXProperty().add(circle1.translateXProperty()));
            line.startYProperty().bind(circle1.centerYProperty().add(circle1.translateYProperty()));
            line.endXProperty().bind(circle2.centerXProperty().add(circle2.translateXProperty()));
            line.endYProperty().bind(circle2.centerYProperty().add(circle2.translateYProperty()));

            lineList.add(line);
        }

        // generate edges
        for (int i = startIndex - 1; i < edges.length; i++) {
            Line line = new Line();
            line.setStroke(Color.ORANGE);
            Circle circle1 = circleList.get(edges[i][0]);
            Circle circle2 = circleList.get(edges[i][1]);

            line.startXProperty().bind(circle1.centerXProperty().add(circle1.translateXProperty()));
            line.startYProperty().bind(circle1.centerYProperty().add(circle1.translateYProperty()));
            line.endXProperty().bind(circle2.centerXProperty().add(circle2.translateXProperty()));
            line.endYProperty().bind(circle2.centerYProperty().add(circle2.translateYProperty()));

            lineList.add(line);
        }


        drawPane.getChildren().addAll(lineList);
        drawPane.getChildren().addAll(circleList);


    }

    /**
     * Generate Color according to nucleotide
     * @param c nucleotide
     * @return nucleotide color
     */
    private Color getNodeColor(char c){
        switch (Character.toLowerCase(c)){
            case 'a': return Color.LIGHTSEAGREEN;
            case 'u': return Color.DARKBLUE;
            case 'c': return Color.LAWNGREEN;
            case 'g': return Color.DARKGREEN;
            default: return Color.LIGHTGRAY;
        }
    }


    /**
     * Eventhandler for mouse pressed for circle drag
     */
    EventHandler<MouseEvent> circleOnMousePressedEventHandler =
            new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent t) {
                    // set origin coordinates
                    originX = t.getSceneX();
                    originY = t.getSceneY();
                    translateX = ((Circle) (t.getSource())).getTranslateX();
                    translateY = ((Circle) (t.getSource())).getTranslateY();
                }
            };

    /**
     * Eventhandler for mouse follow on drag
     */
    EventHandler<MouseEvent> circleOnMouseDraggedEventHandler =
            new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent t) {
                    // calculate offset
                    double offsetX = t.getSceneX() - originX;
                    double offsetY = t.getSceneY() - originY;
                    double newTranslateX = translateX + offsetX;
                    double newTranslateY = translateY + offsetY;
                    // follow mouse
                    ((Circle) (t.getSource())).setTranslateX(newTranslateX);
                    ((Circle) (t.getSource())).setTranslateY(newTranslateY);
                }
            };

    /**
     * Eventhandler for on mouse release
     */
    EventHandler<MouseEvent> circleOnMouseReleasedEventHandler =
            new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent t) {
                    // move circle back to origin
                    TranslateTransition move = new TranslateTransition(new Duration(200), (Circle) (t.getSource()));
                    move.setToX(translateX);
                    move.setToY(translateY);
                    move.playFromStart();
                }
            };
}
