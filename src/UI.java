import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Deviltech on 01.12.2015.
 */
public class UI extends Application{


    @Override
    public void start(Stage primaryStage) throws Exception {

        VBox mainBox = new VBox();
        TextField sequenceField =  new TextField(myLabels.TEXTAREA_SEQUENCE);
        TextField bracketField = new TextField(myLabels.TEXTAREA_BRACKET);
        HBox buttonBox = new HBox();
        Button computeButton = new Button(myLabels.BUTTON_COMPUTE);
        Button drawButton = new Button(myLabels.BUTTON_DRAW);
        CheckBox animateChecker = new CheckBox(myLabels.CHECKBOX_ANIMATE);
        Pane drawPane = new Pane();

        Canvas canvas = new Canvas(400, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        buttonBox.getChildren().addAll(computeButton, drawButton, animateChecker);
        mainBox.getChildren().addAll(sequenceField, bracketField, buttonBox, drawPane);

        // computeButton
        computeButton.setOnAction((value) -> {
            bracketField.setText(new Nussinov(sequenceField.getText()).getBracketNotation());
        });

        final double[][][] coordsRepresentation = {new double[1][2]};

        // drawButton
        drawButton.setOnAction((value) -> {
            Graph myGraph = new Graph();
            try {
                myGraph.parseNotation(bracketField.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
            coordsRepresentation[0] = SpringEmbedder.computeSpringEmbedding(50, myGraph.getNumberOfNodes(), myGraph.getEdges(), null );
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
        ArrayList<Circle> circleList = new ArrayList<>();
        // generate Circles
        for (double[] currentCoord: coords){
            Circle currentCircle = new Circle(currentCoord[0],currentCoord[1], 5, Color.BLACK);
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
        System.out.println(Graph.ArrayToString(edges));


        drawPane.getChildren().addAll(lineList);
        drawPane.getChildren().addAll(circleList);


    }
}
