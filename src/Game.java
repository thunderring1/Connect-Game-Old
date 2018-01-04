import javafx.application.*;
import javafx.scene.Scene;
import javafx.stage.*;
import javafx.animation.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.scene.shape.*;
import javafx.scene.paint.Color;
import javafx.scene.control.*;
import javafx.event.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.geometry.Bounds;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.effect.*;
import javafx.scene.text.Font;
import javafx.geometry.Pos;
import javafx.animation.Interpolator;
import javafx.scene.Group;
import javafx.scene.text.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.collections.*;
import javafx.scene.Node;
import java.util.concurrent.ThreadLocalRandom;
import javafx.concurrent.Task;
import java.util.HashMap;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BackgroundImage;
import javax.imageio.ImageIO;
import java.net.URL;


interface Functional {
  void activate();
}

public class Game extends Application {

  private Pane gamePane;
  private VBox menuPane;
  private VBox statusPane;
  private FlowPane perkPane;
  private Scene mainScene;

  private ShapeFactory shapeFactory;
  private ItemFactory itemFactory;
  private double oldX, oldY;

  private List<Shape> chainList;
  private List<Transition> transitionList;
  private ObservableList<Node> nodeList;
  private String[] itemList;
  private Functional[] perkList;
  private int actives;
  private int totalPerks;
  private Tooltip[] tooltipList;
  //private HashMap<String, Integer> perkMap;

  private List<Integer> chosenPerks;
  private int perkLimit;
  private int currentChosen;

  private double baseScoreMultiplier;
  private double targetSizeMultiplier;
  private double durationMultiplier;
  private double itemRateIncrease;

  private double paneWidth, paneHeight;
  private Color fillColor, strokeColor;
  private Color lineColor;
  protected int targetDuration;
  protected int itemDuration;
  protected double playRate;
  private boolean chaining;

  private double currentScore;
  protected double chainMultiplier;
  protected double speedMultiplier;
  protected double baseScore;
  protected double initialBaseScore;
  private Label scoreLabel;

  protected int currentLife;
  private Label lifeLabel;

  private String gameStatus;
  private Button startButton;
  private Button stopButton;
  private Button pauseButton;

  protected double gameSpeed;
  private Label gameSpeedLabel;

  private Timer timer;
  private Label timerLabel;

  private Text gameoverText;


  public static void main(String[] args) {
    Application.launch(args);
  }



  public void init() {

    paneWidth = 1024;
    paneHeight = 768;


    fillColor = Color.GREEN;
    strokeColor = Color.WHITE;
    lineColor = Color.GREEN;

    shapeFactory = new ShapeFactory();
    shapeFactory.setRadius(20);
    shapeFactory.setDimensions(paneWidth, paneHeight);
    shapeFactory.setColors(fillColor, strokeColor);

    itemFactory = new ItemFactory();
    itemFactory.setDimensions(paneWidth, paneHeight);
    itemFactory.setItem("hourglass.png");

    oldX = oldY = -1.0;

    chainList = new ArrayList<Shape>();
    transitionList = new ArrayList<Transition>();

    itemList = new String[]{"hourglass", "heart", "x2", "clear","random",
                            "scoreup", "slowdown", "sizeup", "lucky", "randomall"};

    perkList = new Functional[10];
    perkList[0] = (() -> perkHourGlass());
    perkList[1] = (() -> perkExtraLife());
    perkList[2] = (() -> perkDoubleScore());
    perkList[3] = (() -> perkFullClear());
    perkList[4] = (() -> perkRandom());
    perkList[5] = (() -> perkScoreUp());
    perkList[6] = (() -> perkSlowDown());
    perkList[7] = (() -> perkSizeUp());
    perkList[8] = (() -> perkLucky());
    perkList[9] = (() -> perkRandomAll());

    actives = 5;
    totalPerks = itemList.length;

    tooltipList = new Tooltip[10];

    tooltipList[0] = new Tooltip("Hourglass (Active) - Targets are 50% slower for the next 6s");
    tooltipList[1] = new Tooltip("Extra life (Hybrid) - One extra life (start with 1 less heart)");
    tooltipList[2] = new Tooltip("Double score (Active) - Double the score gained for the next 12 seconds");
    tooltipList[3] = new Tooltip("Full clear (Active) - Use up 15% of your score and clear all targets on screen");
    tooltipList[4] = new Tooltip("Fortune favors the brave (Active) - Randomly gives one of the other active and hybrid items");
    tooltipList[5] = new Tooltip("Score up (Passive) - Gain 10% more score for each stack - base 10%");
    tooltipList[6] = new Tooltip("Slow down (Passive) - Targets last 5% longer for each stack - base 10%");
    tooltipList[7] = new Tooltip("Size up (Passive) - Targets 5% bigger for each stack - base 10%");
    tooltipList[8] = new Tooltip("Lucky (Passive) - 1% more chance for an item to appear - base 5%");
    tooltipList[9] = new Tooltip("Jesus takes the Wheel (Active) - Random gives one of the other items");

    //perkMap = new HashMap<String, Integer>(5);

    perkLimit = 4;
    chosenPerks = new ArrayList<Integer>();
    currentChosen = 0;

    perkPane = new FlowPane(5, 5);
    perkPane.setAlignment(Pos.CENTER);
    perkPane.setStyle("-fx-border-color: blue;" +
                      "-fx-border-radius: 5;" +
                      "-fx-border-width: 2;");

    /*for (String s : itemList) {
      Label i = new Label(s, new ImageView(new Image("./game/" + s + ".png", 50, 50, true, false)));
      i.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      i.addEventHandler(MouseEvent.MOUSE_CLICKED, (ae) -> {
        i.setStyle("-fx-border-color: red;");
      });
      perkPane.getChildren().add(i);
    }*/
    setupPerkList();

    baseScoreMultiplier = 1.0;
    durationMultiplier = 1.0;
    targetSizeMultiplier = 1.0;
    itemRateIncrease = 0.0;

    targetDuration = 2000;
    itemDuration = 5000;

    chaining = false;

    currentScore = 0;
    chainMultiplier = 1.1;
    speedMultiplier = 1.5;
    baseScore = initialBaseScore = 100.0;


    currentLife = 0;

    gameStatus = "Stopped";
    gameSpeed = 0.0;
    playRate = 1.0;

    timer = new Timer();

    scoreLabel = new Label("Score: " + currentScore);
    scoreLabel.setFont(Font.font(20));

    lifeLabel = new Label("Life: " + currentLife);
    lifeLabel.setFont(Font.font(20));

    timerLabel = new Label(timer.toString());
    timerLabel.setFont(Font.font(50));

    gameSpeedLabel = new Label("Current speed: " + gameSpeed);
    gameSpeedLabel.setFont(Font.font(15));

    startButton = new Button("Start");
    startButton.setFont(Font.font(30));
    startButton.setOnAction(e -> {
      if (currentChosen < perkLimit) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setHeaderText("Chose your perks!");
        alert.setContentText("Please choose " + perkLimit + " perks!");
        alert.showAndWait();
      }
      else {
        if (gameStatus == "Stopped")
          startGame();
        //else if (gameStatus == "Paused")
          //resumeGame();
      }
    });

    stopButton = new Button("Stop");
    stopButton.setFont(Font.font(30));
    stopButton.setOnAction(e -> {
        if (gameStatus != "Stopped")
          endGame();
    });

    /*pauseButton = new Button("Pause");
    pauseButton.setFont(Font.font(30));
    pauseButton.setOnAction(e -> {
      pauseGame();
    });*/

    gameoverText = new Text("Game Over");
    gameoverText.setFont(Font.font(50));
    gameoverText.setEffect(new DropShadow());
    gameoverText.setLayoutX((paneWidth - gameoverText.getLayoutBounds().getWidth())/2);
    gameoverText.setLayoutY((paneHeight + gameoverText.getLayoutBounds().getHeight())/2);
  }



  public void start(Stage mainStage) {
    gamePane = new Pane();
    gamePane.setPrefSize(paneWidth, paneHeight);
    gamePane.setStyle("-fx-background-color: white;" +
                      "-fx-border-style: solid inside;" +
                      "-fx-background-radius: 5;" +
                      "-fx-border-width: 2;" +
                      "-fx-border-radius: 5;" +
                      "-fx-border-color: black;");


    /*BackgroundImage bi = new BackgroundImage(new Image("./game/background.png", 1024, 768, true, false),
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);*/

    nodeList = gamePane.getChildren();

    //gamePane.setBackground(new Background(bi));

    gamePane.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> mouseReleased(e));

    menuPane = new VBox(10.0);
    menuPane.setPrefSize(240, paneHeight);
    menuPane.setStyle("-fx-background-color: silver;");
    menuPane.setAlignment(Pos.CENTER);

    statusPane = new VBox(5.0);
    statusPane.setPrefSize(240, paneHeight);
    statusPane.setStyle("-fx-background-color: silver;");
    statusPane.setAlignment(Pos.CENTER);

    menuPane.getChildren().addAll(startButton, stopButton, perkPane);
    statusPane.getChildren().addAll(timerLabel, lifeLabel, scoreLabel, gameSpeedLabel);

    HBox root = new HBox(menuPane, gamePane, statusPane);
    root.setStyle("-fx-background-color: silver");
    //root.setStyle("-fx-padding: 10");
    //root.setPrefSize(1000, paneHeight + 100);

    mainScene = new Scene(root);
    //mainScene.setBackground(new Background(bi));

    mainStage.setScene(mainScene);
    mainStage.setTitle("Connect");
    mainStage.getIcons().add(new Image(getClass().getResourceAsStream("clear.png")));
    mainStage.show();
  }



  public void startGame() {
    resetGame();

    /*Runnable game = () -> runGame();
    Thread gameThread = new Thread(game);
    gameThread.setDaemon(true);


    gameThread.start();*/

    /*Runnable speedController = () -> controlSpeed();
    Thread speedThread = new Thread(speedController);
    speedThread.setPriority(Thread.MAX_PRIORITY);
    speedThread.setDaemon(true);

    speedThread.start();

    Runnable clock = () -> controlClock();
    Thread clockThread = new Thread(clock);
    clockThread.setDaemon(true);

    clockThread.start();*/

    createNewThread(() -> runGame());
    createNewThread(() -> controlSpeed());
    createNewThread(() -> controlClock());
    createNewThread(() -> itemEventControl());
  }

  public void runGame() {
    while (true) {
      try {
        Shape pol = shapeFactory.makeShape();


        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis( (long) targetDuration * durationMultiplier));
        scaleTransition.setFromX(0);
        scaleTransition.setToX(2 * targetSizeMultiplier);
        scaleTransition.setFromY(0);
        scaleTransition.setToY(2 * targetSizeMultiplier);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setInterpolator(Interpolator.LINEAR);



        RotateTransition rotateTransition = new RotateTransition(Duration.millis( (long) targetDuration * durationMultiplier));
        rotateTransition.setFromAngle(0.0);
        rotateTransition.setToAngle(360.0);
        rotateTransition.setCycleCount(2);
        rotateTransition.setInterpolator(Interpolator.LINEAR);

        ParallelTransition pt = new ParallelTransition(pol, scaleTransition, rotateTransition);


        pol.addEventFilter(MouseEvent.DRAG_DETECTED, e -> {
          mouseDragDetected(e);
          pt.setRate(playRate * 0.5);
        });

        pol.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> mouseClicked(e));

        pol.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, e -> {
          mouseDragEntered(e);
          pt.setRate(playRate * 0.5);
        });

        scaleTransition.setOnFinished(ae -> {
          if (nodeList.contains(pol)) {
            if (chainList.contains(pol)) {
              this.removeAllChains();
              chaining = false;
            }
            else
              nodeList.remove(pol);

            setLife();
          }

          transitionList.remove(pt);
        });

        Platform.runLater(() -> {
          nodeList.add(pol);
          pt.setRate(playRate);
          pt.play();
          transitionList.add(pt);
        });

        //Thread.sleep((int) (1000/speed));

        synchronized (this) {
          //System.out.println(transitionList.size());
          wait((int) (1000 / (gameSpeed*playRate) ));
          //while (gameStatus == "Paused") {
            //wait();
          //}
          if (gameStatus == "Stopped") {
            break;
          }
        }

      } catch (InterruptedException ie) {

      }
    }
  }

  public void resetGame() {
    nodeList.clear();

    currentLife = 3;
    lifeLabel.setText("Life: " + currentLife);

    gameSpeed = 1.0;
    gameSpeedLabel.setText("Current speed: " + gameSpeed);

    timer.reset();
    timerLabel.setText(timer.toString());

    currentScore = 0;
    scoreLabel.setText("Score: " + currentScore);

    baseScore = initialBaseScore;

    baseScoreMultiplier = 1.0;
    durationMultiplier = 1.0;
    targetSizeMultiplier = 1.0;
    itemRateIncrease = 0.0;

    if (chosenPerks.contains(1))
      setLife();
    if (chosenPerks.contains(5))
      baseScoreMultiplier = 1.10;
    if (chosenPerks.contains(6))
      durationMultiplier = 1.1;
    if (chosenPerks.contains(7))
      targetSizeMultiplier = 1.1;
    if (chosenPerks.contains(8))
      itemRateIncrease = 0.05;

    gameStatus = "Started";
    gamePane.disableProperty().setValue(false);
    perkPane.disableProperty().setValue(true);
  }

  synchronized public void endGame() {
    gamePane.disableProperty().setValue(true);
    perkPane.disableProperty().setValue(false);
    gameStatus = "Stopped";
    notifyAll();

    Platform.runLater(() -> {
      nodeList.clear();
      nodeList.add(gameoverText);
      transitionList.clear();
      chainList.clear();
    });
  }
/*
  synchronized public void pauseGame() {
    gamePane.disableProperty().setValue(true);
    gameStatus = "Paused";
    notifyAll();
    for (Transition t : transitionList) {
      t.pause();
    }
  }

  synchronized public void resumeGame() {
    gamePane.disableProperty().setValue(false);
    gameStatus = "Started";
    notifyAll();
    for (Transition t : transitionList) {
      t.play();
    }
  }
*/
  public void controlSpeed() {
    while (true) {
      try {
        //Thread.sleep(30000);

        synchronized (this) {
          wait(15000);
          if (gameStatus == "Stopped")
            break;
          //while (gameStatus == "Paused") {
            //wait();
          //}
        }

        Platform.runLater(() -> {
          gameSpeed += 0.1;
          gameSpeedLabel.setText("Current speed: " + Math.round(gameSpeed * 100.0)/100.0);
        });

      } catch (InterruptedException ie) {}
    }
  }

  public void controlClock() {
    while (true) {
      try {
        synchronized (this) {
          wait(1000);
          if (gameStatus == "Stopped")
            break;
          //while (gameStatus == "Paused") {
            //wait();
          //}
        }

        Platform.runLater(() -> {
          timer.refreshTime();

          timerLabel.setText(timer.toString());
        });

      } catch (Exception e) {}
    }
  }

  public synchronized void itemEventControl() {
    while (true) {
      try {
        synchronized (this) {
          wait(10000);
          if (gameStatus == "Stopped")
            break;
        }

        int rand =  ThreadLocalRandom.current().nextInt(0, currentChosen);
        int perkIndex = chosenPerks.get(rand);

        triggerItemEvent(itemList[perkIndex] + ".png",
                         perkList[perkIndex], itemRateIncrease);

      } catch (InterruptedException ie) {}
    }
  }

  public synchronized void setupPerkList() {
    Task task = new Task<Void>() {
      @Override
      public Void call() {
        for (int i = 0; i < totalPerks; i++) {
          int j = i;
          String s = itemList[j];

          //perkMap.put(s, i);

          Label item = new Label(s, new ImageView(new Image(s + ".png", 50, 50, true, false)));

          item.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          item.addEventHandler(MouseEvent.MOUSE_CLICKED, (ae) -> {
            if (!chosenPerks.contains(j)) {
              if (currentChosen < perkLimit) {
                item.setStyle("-fx-border-color: red;" +
                           "-fx-border-radius: 5;" +
                           "-fx-border-width: 2;");
                chosenPerks.add(j);
                ++currentChosen;
                //System.out.println(chosenPerks);
              }
            }
            else {
              item.setStyle("");
              chosenPerks.remove(new Integer(j));
              --currentChosen;
            }
          });

          item.setTooltip(tooltipList[j]);
          perkPane.getChildren().add(item);
        }
        return null;
      }
    };

    new Thread(task).start();
  }

/*

Event Handling

*/



  private void mouseClicked(MouseEvent me) {
    setScore(baseScore*Math.pow(speedMultiplier, (gameSpeed-1)*2));
    nodeList.remove((Shape) me.getSource());
  }

  private void mouseDragEntered(MouseDragEvent me) {

    if (!chaining) return;

    Shape shape = (Shape) me.getSource();

    if (chainList.contains(shape)) return;

    //double tmpX = shape.getLayoutX() + shapeFactory.getWidth()/2.0;
    //double tmpY = shape.getLayoutY() + shapeFactory.getHeight()/2.0;

    double tmpX = shape.getLayoutX();
    double tmpY = shape.getLayoutY();

    Line line = new Line(oldX, oldY, tmpX, tmpY);
    line.setStrokeWidth(4.0);
    line.setStroke(lineColor);
    line.setEffect(new BoxBlur(5.0, 5.0, 2));

    nodeList.add(line);
    shape.setStroke(Color.BLACK);

    oldX = tmpX;
    oldY = tmpY;

    chainList.add(line);
    chainList.add(shape);
  }

  private void mouseDragDetected(MouseEvent me) {
    Shape shape = (Shape) me.getSource();
    shape.startFullDrag();
    chaining = true;

    //oldX = shape.getLayoutX() + shapeFactory.getWidth()/2.0;
    //oldY = shape.getLayoutY() + shapeFactory.getHeight()/2.0;

    oldX = shape.getLayoutX();
    oldY = shape.getLayoutY();

    shape.setStroke(Color.BLACK);
    chainList.add(shape);
  }

  private void mouseReleased(MouseEvent me) {
    getScoreFromChains();
    this.removeAllChains();
    chaining = false;
  }

/*

Utilities


*/

  public void createNewThread(Functional func) {
    Runnable runnable = () -> func.activate();
    Thread thread = new Thread(runnable);
    thread.setDaemon(true);
    thread.start();
  }

  private void removeAllChains() {
    //System.out.println("[ " + chainList + " ]");
    nodeList.removeAll(chainList);
    chainList.clear();
    oldX = oldY = -1.0;
  }

  private void setScore(double score) {
    //System.out.println(score * baseScoreMultiplier);
    currentScore += (score * baseScoreMultiplier);
    scoreLabel.setText("Score: " + (int) currentScore);
  }

  private void getScoreFromChains() {
    int chains = (int) (chainList.size() + 1)/2;

    if (chains >= 1) {
      setScore(baseScore *
               chains *
               Math.pow(chainMultiplier, chains-1) *
               Math.pow(speedMultiplier, (gameSpeed-1)*2));
    }
    if (chains >= 5) {
      int rand =  ThreadLocalRandom.current().nextInt(0, currentChosen);
      int perkIndex = chosenPerks.get(rand);

      triggerItemEvent(itemList[perkIndex] + ".png",
                       perkList[perkIndex], itemRateIncrease);
    }
  }

  private void setLife() {
    currentLife--;
    if (currentLife >= 0) {
      lifeLabel.setText("Life: " + currentLife);
    }

    if (currentLife == 0) {
      endGame();
    }
  }

  private synchronized void setAllPlayRate(double multi) {
    for (Transition t : transitionList) {
      t.setRate(t.getCurrentRate() * multi);
    }
  }

/*

Perks

*/

  private synchronized void triggerItemEvent(String url, Functional effect,
                                             double extraChance) {

    if (ThreadLocalRandom.current().nextDouble() > 0.3 + extraChance) return;

    itemFactory.setItem(url);

    ImageView item = itemFactory.makeItem();

    item.addEventHandler(MouseEvent.MOUSE_CLICKED,
        (ie) -> {
          nodeList.remove(item);
          createNewThread(() -> effect.activate());
        });

    ScaleTransition scaleTransition = new ScaleTransition(Duration.millis( (long) itemDuration * durationMultiplier));
    scaleTransition.setFromX(0);
    scaleTransition.setToX(2);
    scaleTransition.setFromY(0);
    scaleTransition.setToY(2);
    scaleTransition.setCycleCount(2);
    scaleTransition.setAutoReverse(true);
    scaleTransition.setInterpolator(Interpolator.LINEAR);

    RotateTransition rotateTransition = new RotateTransition(Duration.millis( (long) itemDuration * durationMultiplier));
    rotateTransition.setFromAngle(0.0);
    rotateTransition.setToAngle(360.0);
    rotateTransition.setCycleCount(2);
    rotateTransition.setInterpolator(Interpolator.LINEAR);

    ParallelTransition pt = new ParallelTransition(item, scaleTransition, rotateTransition);

    scaleTransition.setOnFinished(ae -> {
      nodeList.remove(item);
    });

    Platform.runLater(() -> {
      nodeList.add(item);
      pt.play();
    });
  }

  private synchronized void perkHourGlass() {
    try {
      setAllPlayRate(0.5);
      playRate = 0.5;
      wait(6000);
      setAllPlayRate(2);
      playRate = 1.0;
    } catch (InterruptedException ie) {}
  }

  private synchronized void perkExtraLife() {
    try {
      Platform.runLater(() -> {
        currentLife++;
        lifeLabel.setText("Life: " + currentLife);
      });
    } catch (Exception e) {}
  }

  private synchronized void perkDoubleScore() {
    try {
      baseScore = baseScore * 2;
      wait(12000);
      baseScore = (baseScore)/2;
    } catch (InterruptedException ie) {}
  }

  private synchronized void perkFullClear() {
    try {
      Platform.runLater(() -> {
        int targets = nodeList.size();
        nodeList.clear();
        currentScore = currentScore * 0.85;
        setScore(targets * baseScore * Math.pow(speedMultiplier, (gameSpeed-1)*2));
      });
    } catch (Exception e) {}
  }

  private synchronized void perkRandom() {
    int rand =  ThreadLocalRandom.current().nextInt(0, actives - 1);

    triggerItemEvent(itemList[rand] + ".png",
                      perkList[rand], 1);
  }

  private synchronized void perkScoreUp() {
    Platform.runLater( () -> {
      baseScoreMultiplier += 0.1;
      //baseScore = initialBaseScore * baseScoreMultiplier;
    });
  }

  private synchronized void perkSlowDown() {
    Platform.runLater(() -> {
      durationMultiplier += 0.05;
    });
  }

  private synchronized void perkSizeUp() {
    Platform.runLater(() -> {
      targetSizeMultiplier += 0.05;
    });
  }

  private synchronized void perkLucky() {
    Platform.runLater(() -> {
      itemRateIncrease += 0.01;
    });
  }

  private synchronized void perkRandomAll() {
    int rand =  ThreadLocalRandom.current().nextInt(0, totalPerks);

    triggerItemEvent(itemList[rand] + ".png",
                     perkList[rand], 1);
  }

}
/*
	Written by Nguyen Le
	First uploaded on 03/01/2018
*/
