/**
 * @author : Stefan Mircovici, Nicolas Poppe
 */
public class Airport {

  private final String codeIata;
  private final String nom;
  private final String ville;
  private final String pays;
  private final double longitude;
  private final double latitude;

  public Airport(String codeIata, String nom, String ville, String pays, double longitude,
      double latitude) {
    this.codeIata = codeIata;
    this.nom = nom;
    this.ville = ville;
    this.pays = pays;
    this.longitude = longitude;
    this.latitude = latitude;
  }

  public String getCodeIata() {
    return codeIata;
  }

  public String getNom() {
    return nom;
  }

  public String getVille() {
    return ville;
  }

  public String getPays() {
    return pays;
  }

  public double getLongitude() {
    return longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  @Override
  public String toString() {
    return "Airport[" +
        "codeIata=" + codeIata +
        ", nom=" + nom +
        ", ville=" + ville +
        ", pays=" + pays +
        ", longitude=" + longitude +
        ", latitude=" + latitude +
        ']';
  }
}
