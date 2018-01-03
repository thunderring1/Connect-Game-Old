import javafx.scene.image.ImageView;
import java.util.concurrent.ThreadLocalRandom;

class ItemFactory {
  private String url;

  private double height, width;

  private double accessibleWidth, accessibleHeight;

  public ItemFactory() {
    url = null;
    height = width = 0;
    accessibleWidth = accessibleHeight = 0;
  }

  public void setDimensions(double width, double height) {
    //Polygon shape = new Polygon(polCoor);

    this.height = 100;
    this.width = 100;

    accessibleWidth = width - this.width;
    accessibleHeight = height - this.height;
  }

  public void setItem(String url) {
    this.url = url;
  }

  public ImageView makeItem() {
    ImageView item = new ImageView(url);
    item.setPreserveRatio(true);
    item.setFitHeight(25);

    item.setLayoutX(ThreadLocalRandom.current().nextDouble()*accessibleWidth);
    item.setLayoutY(ThreadLocalRandom.current().nextDouble()*accessibleHeight);

    return item;
  }
}
