/**
 * @author : Stefan Mircovici, Nicolas Poppe
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Graph {

  private Map<String, Airport> mapIataAeroport;
  private Map<Airport, Set<Flight>> grapheInitial;

  public Graph(File aeroports, File vols) {
    this.mapIataAeroport = new HashMap<String, Airport>();
    this.grapheInitial = new HashMap<Airport, Set<Flight>>();
    try {
      // Lire tous les aéroports
      FileReader frAeroport = new FileReader(aeroports);
      BufferedReader brAeroport = new BufferedReader(frAeroport);
      String ligneAeroportActuel;
      while ((ligneAeroportActuel = brAeroport.readLine()) != null) {
        String[] aeroportInformation = ligneAeroportActuel.split(",");
        Airport aeroportActuel = new Airport(aeroportInformation[0], aeroportInformation[1],
            aeroportInformation[2], aeroportInformation[3],
            Double.parseDouble(aeroportInformation[4]),
            Double.parseDouble(aeroportInformation[5]));
        ajouterSommet(aeroportActuel);
        this.mapIataAeroport.put(aeroportInformation[0], aeroportActuel);
      }
      frAeroport.close();
      brAeroport.close();

      // Lire tous les vols
      FileReader frVol = new FileReader(vols);
      BufferedReader brVol = new BufferedReader(frVol);
      String ligneVolActuel;
      while ((ligneVolActuel = brVol.readLine()) != null) {
        String[] flightInformation = ligneVolActuel.split(",");
        Flight actualFlight = new Flight(flightInformation[0],
            mapIataAeroport.get(flightInformation[1]), mapIataAeroport.get(flightInformation[2]));
        ajouterArc(actualFlight);
      }
      frVol.close();
      brVol.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Créer le chemin pour arriver d'un aéroport de début à un final pour minimiser le nombre de vols
   * en utilisant le BFS
   *
   * @param aeroportSource code iata aéroport source
   * @param aeroportDestination code iata aéroport destination
   */
  public void calculerItineraireMinimisantNombreVol(String aeroportSource,
      String aeroportDestination) {

    HashSet<Airport> aeroportsDejaVisite = new HashSet<>();
    Map<Airport, Set<Flight>> cheminsOptis = new HashMap<>();
    ArrayDeque<Airport> fileAeroports = new ArrayDeque<>();
    Airport aeroportInitial = mapIataAeroport.get(aeroportSource);

    //ajouter l'aéroport initial dans les déjà pris et dans la file
    fileAeroports.addLast(aeroportInitial);
    aeroportsDejaVisite.add(aeroportInitial);

    //tant que la file n'est pas finie
    while(!fileAeroports.isEmpty()){
      //prendre le premier dans la file et lui créer un set de vols
      Airport aeroportActuel=fileAeroports.poll();
      cheminsOptis.put(aeroportActuel, new HashSet<>());
      //pour tout les arcs sortant de l'aéroport actuel
      for(Flight flight : arcsSortants(aeroportActuel)){
        //aéroport de destination
        Airport destination = flight.getDestination();
        //si l'aéroport de destination n'a jamais été pris (analysé)
        if(!aeroportsDejaVisite.contains(destination)){
          //ajouter la destination dans la file des aéroports à analyser
          fileAeroports.addLast(destination);
          //ajouter dans la liste des analysés
          aeroportsDejaVisite.add(destination);
          //ajouter les vols à l'aéroport qu'on analyse
          cheminsOptis.get(aeroportActuel).add(flight);
        }
      }
    }
    ArrayDeque<Flight> cheminVolsAvecMoinsDAeroports = new ArrayDeque<>();
    double distanceFinale = creerCheminAvecLeMoinsDAeroports(cheminsOptis, aeroportInitial,
        aeroportDestination, cheminVolsAvecMoinsDAeroports);
    //si la distance est de 0
    if(distanceFinale == 0){
      System.out.println("Il n'existe aucun itinéraire.");
      return;
    }
    System.out.println("distance : "+distanceFinale);
    for (Flight f : cheminVolsAvecMoinsDAeroports){
      System.out.println(f);
    }
  }

  /**
   * Créer le chemin pour arriver d'un aéroport de début à un final
   *
   * @param grapheBFS graphe contenant le bfs
   * @param debut aéroport de début
   * @param fin code iata de l'aéroport d'arrivée
   * @param chemin chemin des vols pour arriver à la destination
   * @return distance entre l'aéroport début et final
   */
  private double creerCheminAvecLeMoinsDAeroports( Map<Airport, Set<Flight>> grapheBFS, Airport debut,String fin,
      ArrayDeque<Flight> chemin){
    //si l'aéroport actuel a des vols
    if(grapheBFS.get(debut).isEmpty()) return 0;
    //distance totale
    double dis = 0;

    //analyse tout les vols
    for(Flight f : grapheBFS.get(debut)){
      //stock la destination du vol actuel
      Airport destination = f.getDestination();
      //si la destination est la bonne
      if(destination.getCodeIata().equals(fin)) {
        //distance = distance entre la source et la destination du vol (f)
        dis = Util.distance(debut.getLatitude(), debut.getLongitude(), destination.getLatitude(),
            destination.getLongitude());
        //ajout au chemin
        chemin.addFirst(f);
        //arret car inutile d'analyser les suivants
        break;
      }
      //sinon, analyse des autres vols
      double nb = creerCheminAvecLeMoinsDAeroports(grapheBFS, f.getDestination(), fin, chemin);
      //si un des sous aéroport a trouver la destination, on ajoute notre distance à celle trouvée
      if(nb>0) {
        dis = nb + Util.distance(debut.getLatitude(), debut.getLongitude(), destination.getLatitude(), destination.getLongitude());
        chemin.addFirst(f);
      }
    }
    return dis;
  }


  /**
   * Créer le chemin pour arriver d'un aéroport de début à un final pour minimiser la distance en
   * utilisant l'algorithme de Djikstra
   *
   * @param aeroportSource : le code Iata de l'aeroport source
   * @param aeroportDestination : le code Iata de l'aeroport de destination
   */
  public void calculerItineraireMiniminantDistance(String aeroportSource,
      String aeroportDestination) {

    HashMap<Airport, ArrayDeque<Flight>> etiquetteProv = new HashMap<>();
    HashMap<Airport, ArrayDeque<Flight>> etiquetteDefinitive = new HashMap<>();
    TreeMap<Double, Airport> distances = new TreeMap<>();
    HashMap<Airport, Double> distancesInverses = new HashMap<>();

    Airport aeroportBaladeur = mapIataAeroport.get(aeroportSource);
    do {
      for (Flight f : arcsSortants(aeroportBaladeur)) { // pour chaque vol sortant
        Airport fSource = f.getSource();
        Airport fDestination = f.getDestination();
        //si on a jamais ajouté de vols à la destination
        if (etiquetteProv.get(fDestination) == null) {
          //si on a jamais ajouté de vols à la source
          if (etiquetteProv.get(fSource) == null) {
            // on lui ajoute son premier vol
            ArrayDeque<Flight> nouveauChemin = new ArrayDeque<>();
            nouveauChemin.addLast(f);
            etiquetteProv.put(fDestination, nouveauChemin);
            distances.put(f.getDistance(), fDestination);
            distancesInverses.put(fDestination, f.getDistance());
          }
          //si on a déjà ajouté au moins un vol à la source
          else {
            // on récupère la liste et on lui ajoute le vol courant
            ArrayDeque<Flight> nouveauChemin = new ArrayDeque<>(etiquetteProv.get(fSource));
            nouveauChemin.addLast(f);
            double distanceSource = distancesInverses.get(fSource);
            // on remplace les précédentes valeurs
            etiquetteProv.put(fDestination, nouveauChemin);
            distances.put(f.getDistance() + distanceSource, fDestination);
            distancesInverses.put(fDestination, f.getDistance() + distanceSource);

          }

        }
        //si on a déjà ajouté au moins un vol à la destination
        else {
          // on va récupérer tous les vols de la destination
          double nouvelleDistance = distancesInverses.get(fDestination);
          double sourceDistance;
          if (distancesInverses.get(fSource) == null) {
            sourceDistance = f.getDistance();
          } else {
            sourceDistance = distancesInverses.get(fSource) + f.getDistance();
          }

          // Si on a trouvé un chemin plus court, on remplace l'ancien
          if (sourceDistance < nouvelleDistance) {
            ArrayDeque<Flight> nouveauChemin = new ArrayDeque<>(etiquetteProv.get(fSource));
            nouveauChemin.addLast(f);
            // on remplace dans l'étiquette
            etiquetteProv.put(fDestination, nouveauChemin);
            // on remplace dans les distances
            distances.remove(nouvelleDistance, fDestination);
            distancesInverses.remove(fDestination, nouvelleDistance);
            distances.put(sourceDistance, fDestination);
            distancesInverses.put(fDestination, sourceDistance);
          }
        }

      }
      // le baladeur devient l'aeroport avec la plus petite distance
      aeroportBaladeur = distances.firstEntry().getValue();
      double distanceASupprimer = distances.firstEntry().getKey();
      distances.remove(distanceASupprimer, aeroportBaladeur);
      // on ajoute à l'étiquette définitive
      etiquetteDefinitive.put(aeroportBaladeur, etiquetteProv.get(aeroportBaladeur));

    } while (!distances.isEmpty());

    // Affichage
    try {
      double distanceFinale = distancesInverses.get(mapIataAeroport.get(aeroportDestination));
      System.out.println("distance : " + distanceFinale);
      ArrayDeque<Flight> cheminOpti = etiquetteDefinitive.get(
          mapIataAeroport.get(aeroportDestination));
      while (!cheminOpti.isEmpty()) {
        System.out.println(cheminOpti.pollFirst());
      }
    } catch (NullPointerException e) {
      System.out.println("Il n'existe aucun itinéraire.");
    }
  }

  /**
   * Ajoute un sommet au graphe initial
   *
   * @param a : aeroport à ajouter
   */
  private void ajouterSommet(Airport a) {
    grapheInitial.put(a, new HashSet<Flight>());
  }

  /**
   * Ajoute un arc au graphe initial
   *
   * @param f : le vol à ajouter
   */
  private void ajouterArc(Flight f) {
    Airport a = f.getSource();
    this.grapheInitial.get(a).add(f);
  }

  /**
   * Donne les arcs sortants d'un aeroport
   *
   * @param a : aeroport duquel on veut les arcs sortants
   * @return set contenant tous les arcs sortants
   */
  private Set<Flight> arcsSortants(Airport a) {
    return this.grapheInitial.get(a);
  }
}
