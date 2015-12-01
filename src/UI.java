import javafx.animation.TranslateTransition;
import javafx.animation.TranslateTransitionBuilder;
import javafx.application.Application;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Deviltech on 01.12.2015.
 */
public class UI extends Application{

    double orgSceneX;
    double orgSceneY;
    double orgTranslateX;
    double orgTranslateY;
    boolean isAnimated;

    Pane drawPane;
    ArrayList<Circle> circleList = new ArrayList<>();

    Graph myGraph;
    final double[][][] coordsRepresentation = {new double[1][2]};


    @Override
    public void start(Stage primaryStage) throws Exception {

        VBox mainBox = new VBox();
        TextField sequenceField =  new TextField(myLabels.TEXTAREA_SEQUENCE);
        TextField bracketField = new TextField(myLabels.TEXTAREA_BRACKET);
        HBox buttonBox = new HBox();
        Button computeButton = new Button(myLabels.BUTTON_COMPUTE);
        Button drawButton = new Button(myLabels.BUTTON_DRAW);
        CheckBox animateChecker = new CheckBox(myLabels.CHECKBOX_ANIMATE);
        drawPane = new Pane();

        Canvas canvas = new Canvas(400, 400);

        buttonBox.getChildren().addAll(computeButton, drawButton, animateChecker);
        mainBox.getChildren().addAll(sequenceField, bracketField, buttonBox, drawPane);

        animateChecker.setOnAction((value) ->{
            if (animateChecker.isSelected() != isAnimated){
                isAnimated = animateChecker.isSelected();
                for(Circle currentCircle: circleList){
                    currentCircle.setOnMousePressed(isAnimated ?  circleOnMousePressedEventHandler : null);
                    currentCircle.setOnMouseDragged(isAnimated ?  circleOnMouseDraggedEventHandler : null);
                    currentCircle.setOnMouseReleased(isAnimated ?  circleOnMouseReleasedEventHandler : null);
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
                e.printStackTrace();
            }
            coordsRepresentation[0] = SpringEmbedder.computeSpringEmbedding(100, myGraph.getNumberOfNodes(), myGraph.getEdges(), null );
            SpringEmbedder.centerCoordinates(coordsRepresentation[0], 10, 400, 10, 400);
            drawShapes(drawPane, coordsRepresentation[0], myGraph.getEdges(), myGraph.getNumberOfNodes());
        });

        Scene scene = new Scene(mainBox, 600, 800);


        mainBox.getChildren().add(canvas);


        primaryStage.setScene(scene);
        primaryStage.setTitle(myLabels.CAPTION);

        // show scene
        primaryStage.show();



    }

    private void drawShapes(Pane drawPane, double[][] coords, int[][] edges, int startIndex) {
        drawPane.getChildren().clear();
        circleList = new ArrayList<>();

        // generate Circles
        for (int i = 0; i < coords.length; i++){
            Circle currentCircle = new Circle(coords[i][0],coords[i][1], 5, Color.BLACK);
            Tooltip.install(
                    currentCircle,
                    new Tooltip(Integer.toString(i+1))
            );
            if(isAnimated){
                currentCircle.setOnMousePressed(circleOnMousePressedEventHandler);
                currentCircle.setOnMouseDragged(circleOnMouseDraggedEventHandler);
                currentCircle.setOnMouseReleased(circleOnMouseReleasedEventHandler);
            }

            circleList.add(currentCircle);
        }


        // generate  basic Lines
        ArrayList<Line> lineList = new ArrayList<>();
        for (int i = 0; i < circleList.size()-1; i++) {
            Line line = new Line();
            line.setStroke(Color.BLACK);
            line.setFill(Color.BLACK);
            Circle circle1 = circleList.get(i);
            Circle circle2 = circleList.get(i+1);


            // bind ends of line:
            line.startXProperty().bind(circle1.centerXProperty().add(circle1.translateXProperty()));
            line.startYProperty().bind(circle1.centerYProperty().add(circle1.translateYProperty()));
            line.endXProperty().bind(circle2.centerXProperty().add(circle2.translateXProperty()));
            line.endYProperty().bind(circle2.centerYProperty().add(circle2.translateYProperty()));

            lineList.add(line);
        }

        // generate edges
        for (int i = startIndex-1; i< edges.length; i++){
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

    EventHandler<MouseEvent> circleOnMousePressedEventHandler =
            new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent t) {
                    orgSceneX = t.getSceneX();
                    orgSceneY = t.getSceneY();
                    orgTranslateX = ((Circle)(t.getSource())).getTranslateX();
                    orgTranslateY = ((Circle)(t.getSource())).getTranslateY();
                }
            };

    EventHandler<MouseEvent> circleOnMouseDraggedEventHandler =
            new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent t) {
                    double offsetX = t.getSceneX() - orgSceneX;
                    double offsetY = t.getSceneY() - orgSceneY;
                    double newTranslateX = orgTranslateX + offsetX;
                    double newTranslateY = orgTranslateY + offsetY;

                    ((Circle)(t.getSource())).setTranslateX(newTranslateX);
                    ((Circle)(t.getSource())).setTranslateY(newTranslateY);
                }
            };

    EventHandler<MouseEvent> circleOnMouseReleasedEventHandler =
            new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent t) {

                    TranslateTransition move = new TranslateTransition(new Duration(200), (Circle) (t.getSource()));
                    move.setToX(orgTranslateX);
                    move.setToY(orgTranslateY);

                    move.playFromStart();
                }
            };
}
