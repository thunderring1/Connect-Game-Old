public class Timer {
//  Thread t;

  private int seconds;
  private int minutes;
  private int hours;

  //private boolean stopTime;

  public Timer() {
  //  t = new Thread(this);

    seconds = 0;
    minutes = 0;
    hours = 0;
  }

  /*public void startTimer() {
    t.start();
  }

  public void run() {
    try {
      while (true) {
        synchronized (this) {
          wait(1000);
          if (stopTime)
            return;
        }

        refreshTime();
      } catch (Exception e) {}
    }
  }*/

  public void refreshTime() {
    if (seconds == 59) {
      seconds = 0;

      if (minutes == 59) {
        hours++;
        minutes = 0;
      }
      else {
        minutes++;
      }
    }
    else
      seconds++;
  }

  public void reset() {
    seconds = minutes = hours = 0;
  }

  public String toString() {
    String time = "";
    if (hours < 10) {
      time += "0" + hours;
    }
    else
      time += hours;

    time += ":";

    if (minutes < 10) {
      time += "0" + minutes;
    }
    else {
      time += minutes;
    }

    time += ":";

    if (seconds < 10) {
      time += "0" + seconds;
    }
    else {
      time += seconds;
    }


    return time;
  }


}
