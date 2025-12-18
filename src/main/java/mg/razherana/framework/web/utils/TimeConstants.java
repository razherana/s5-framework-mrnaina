package mg.razherana.framework.web.utils;

public enum TimeConstants {
  MILLISECOND(1),
  SECOND(MILLISECOND.getMilliseconds() * 1000),
  MINUTE(SECOND.getMilliseconds() * 60),
  HOUR(MINUTE.getMilliseconds() * 60),
  DAY(HOUR.getMilliseconds() * 24),
  WEEK(DAY.getMilliseconds() * 7);

  private long milliseconds = 0;

  TimeConstants(long milliseconds) {
    this.milliseconds = milliseconds;
  }

  public long getMilliseconds() {
    return milliseconds;
  }
}
