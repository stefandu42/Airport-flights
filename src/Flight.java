/**
 * @author : Stefan Mircovici, Nicolas Poppe
 */
public class Flight {
  private final String airline;
  private final Airport source;
  private final Airport destination;
  private double distance;

  public Flight(String nomCompagnie, Airport source, Airport destination) {
    this.airline = nomCompagnie;
    this.source = source;
    this.destination = destination;
    setDistance();
  }

  private void setDistance(){
    this.distance = Util.distance(source.getLatitude(),source.getLongitude(),
        destination.getLatitude(),destination.getLongitude());
  }

  public String getAirline() {
    return airline;
  }

  public Airport getSource() {
    return source;
  }

  public Airport getDestination() {
    return destination;
  }

  public double getDistance() {
    return distance;
  }

  @Override
  public String toString() {
    return "Vol[" +
        "source=" + source +
        ", destination=" + destination +
        ", airline=" + airline +
        ", distance=" + distance +
        ']';
  }
}
