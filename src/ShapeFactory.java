import javafx.scene.shape.Polygon;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.concurrent.ThreadLocalRandom;

public class ShapeFactory {
  private double radius;

  private double height, width;

  private double accessibleWidth, accessibleHeight;

  private Color fillColor, strokeColor;

  public ShapeFactory() {
    polCoor = null;
    height = width = 0;
    fillColor = strokeColor = null;
    radius = 0;
    accessibleWidth = accessibleHeight = 0;
  }

  public void setColors(Color fillColor, Color strokeColor) {
    this.fillColor = fillColor;
    this.strokeColor = strokeColor;
  }

  public Color getStrokeColor() { return strokeColor; }

  public Color getFillColor() { return fillColor; }

  public void setDimensions(double width, double height) {
    Circle shape = new Circle(radius);

    this.height = shape.getLayoutBounds().getHeight();
    this.width = shape.getLayoutBounds().getWidth();

    accessibleWidth = width - this.width;
    accessibleHeight = height - this.height;
  }

  public double getHeight() { return height; }
  public double getWidth() { return width; }


  public void setRadius(double radius) {
    this.radius = radius;
  }

  public Circle makeShape() {
    Circle shape = new Circle(radius);
    shape.setFill(fillColor);
    shape.setStroke(strokeColor);
    shape.setLayoutX(ThreadLocalRandom.current().nextDouble()*accessibleWidth);
    shape.setLayoutY(ThreadLocalRandom.current().nextDouble()*accessibleHeight);

    return shape;
  }
}
