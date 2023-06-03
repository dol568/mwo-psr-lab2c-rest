// Kod klasy utworzony na podstawie tutoriala: http://www.tutorialspoint.com/restful/restful_first_application.htm

package mwo.psr.restclient;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import mwo.ttss.Actual;
import mwo.ttss.Departures;
import mwo.ttss.Stop;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.*;

public class ClientJ2 {

    private Client client;
    private static final String SUCCESS_RESULT = "<result>success</result>";
    private static final String PASS = "pass";
    private static final String FAIL = "fail";
    private static final String BASE_URL = "http://www.ttss.krakow.pl/internetservice/services";
    private static final String AUTOCOMPLETE_URL = BASE_URL + "/lookup/autocomplete/json";
    private static final String PASSAGE_INFO_URL = BASE_URL + "/passageInfo/stopPassages/stop";

    public static void main(String[] args) {
        ClientJ2 c = new ClientJ2();
        c.init();
        c.interactiveUI(c);
    }

    public void interactiveUI(ClientJ2 client) {
        String line = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        do {
            try {
                System.out.print("==> ");
                System.out.flush();
                line = in.readLine();
                if (line == null) {
                    break;
                } else if (line.equals("getUser")) {
                    client.getUser();
                } else if (line.equals("getDepartures")) {
                    client.getDepartures(in);
                } else if (line.equals("raw")) {
                    client.getRawJson();
                } else if (line.equals("x")) {
                } else {
                    System.out.println("unknown command `" + line + "'");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        while (!line.equals("x"));
    }

    private void init() {
        client = ClientBuilder.newClient();
        //client.register(new Authenticator("pum", "pum123"));
        client.register(JacksonJsonProvider.class);
    }

    public void getRawJson() {
        System.out.println("\n\n*** Starting test case '" + Thread.currentThread().getStackTrace()[1].getMethodName() + "'...");

        String res = client
                .target("http://www.ttss.krakow.pl/internetservice/services/passageInfo/stopPassages/stop?stop=681&mode=departure&language=pl")
                .request(MediaType.APPLICATION_JSON).get().readEntity(String.class);

        String result = PASS;
        if (res == null) result = FAIL;

        System.out.println("Result: " + res);
        System.out.println("\n\n*** Test case '" + Thread.currentThread().getStackTrace()[1].getMethodName() + "' finished");
    }

    public void getUser() {
        System.out.println("\n\n*** Starting test case '" + Thread.currentThread().getStackTrace()[1].getMethodName() + "'...");

        User user = client
                .target("http://localhost:8080/SampleRestService/rest/UserManagementService/usersj/2")
                .request(MediaType.APPLICATION_JSON)
                .get(User.class);

        String result = PASS;
        if (user == null) result = FAIL;

        System.out.println("Result: " + result);
        System.out.println("\n\n*** Test case '" + Thread.currentThread().getStackTrace()[1].getMethodName() + "' finished");
    }

    /**
     * Umożliwia użytkownikom pobranie informacji o odjazdach lub przyjazdach z określonego
     * przystanku komunikacji publicznej.
     *
     * @param in Obiekt BufferedReader używany do odczytu danych wprowadzanych przez użytkownika.
     * @throws IOException Jeśli wystąpi błąd podczas odczytu danych wprowadzanych przez użytkownika.
     */
    public void getDepartures(BufferedReader in) throws IOException {
        int input;
        boolean flag = false;
        do {
            System.out.print("Wybierz: \n" +
                    "1 - Podaj nazwe przystanku\n" +
                    "2 - Podaj id przystanku\n" +
                    "3 - exit\n");
            input = Integer.parseInt(in.readLine());
            if (input > 0 && input < 4) {
                flag = true;
            }
        } while (!flag);

        switch (input) {
            case 1:
                System.out.println("Podaj nazwe przystanku: ");
                String stopName = in.readLine();
                Map<String, String> stop = getStopIdAndName(stopName);

                if (!stop.isEmpty()) {
                    if (stop.size() > 1) {
                        for (Map.Entry<String, String> entry : stop.entrySet()) {
                            System.out.println();
                            System.out.println("Id: " + entry.getKey() + ", nazwa: " + entry.getValue());
                        }
                        System.out.println("\nPodaj Id przystanku: ");
                        String choice = in.readLine();
                        System.out.println("Podaj kierunek (O -odjazdy, P -przyjazdy): ");
                        String direction = in.readLine();
                        if (direction.equalsIgnoreCase("O")) {
                            displayDepartures(choice, "departure");
                        } else {
                            displayDepartures(choice, "arrival");
                        }
                    } else {
                        for (Map.Entry<String, String> entry1 : stop.entrySet()) {
                            String key = entry1.getKey();
                            System.out.println("Podaj kierunek (O -odjazdy, P -przyjazdy): ");
                            String direction = in.readLine();
                            if (direction.equalsIgnoreCase("O")) {
                                displayDepartures(key, "departure");
                            } else {
                                displayDepartures(key, "arrival");
                            }
                        }
                    }
                } else {
                    System.out.println("Przystanek nie istnieje");
                }
                break;
            case 2:
                System.out.println("Podaj id przystanku: ");
                String stopName2 = in.readLine();
                System.out.println("Podaj kierunek (O -odjazdy, P -przyjazdy): ");
                String direction = in.readLine();
                if (direction.equalsIgnoreCase("O")) {
                    displayDepartures(stopName2, "departure");
                } else {
                    displayDepartures(stopName2, "arrival");
                }
                break;
            default:
                break;
        }
    }

    /**
     * Wyswietla informacje o odjazdach lub przyjazdach z okreslonego przystanku
     *
     * @param stopId Id przystanku
     * @param direction Odjazdy lub przyjazdy
     */
    private void displayDepartures(String stopId, String direction) {
        System.out.println("\n*** Starting test case '" + Thread.currentThread().getStackTrace()[1].getMethodName() + "'...");

        Departures departures = client
                .target(PASSAGE_INFO_URL)
                .queryParam("stop", stopId)
                .queryParam("mode", direction)
                .queryParam("language", "pl")
                .request(MediaType.APPLICATION_JSON)
                .get(Departures.class);

        String result = PASS;
        if (departures != null) {
            printer(departures, direction);
        } else {
            result = FAIL;
        }
        System.out.println("\nResult: " + result);
        System.out.println("\n*** Test case '" + Thread.currentThread().getStackTrace()[1].getMethodName() + "' finished");
    }

    /**
     * Zwraca mape zawierajaca Id i nazwe przystanku na podstawie podanej nazwy przystanku
     *
     * @param stopName Cala lub czesciowa nazwa przystanku
     * @return  Mapa z Id i nazwa przystanku
     */
    private Map<String, String> getStopIdAndName(String stopName) {
        System.out.println("\n\n*** Starting test case '" + Thread.currentThread().getStackTrace()[1].getMethodName() + "'...");
        Map<String, String> st = new HashMap<>();
        Stop[] stops = client.target(AUTOCOMPLETE_URL)
                .queryParam("query", stopName)
                .queryParam("language", "pl")
                .request(MediaType.APPLICATION_JSON)
                .get(Stop[].class);
        String result = PASS;
        if (stops != null) {
            for (int i = 1; i < stops.length; i++) {
                st.put(stops[i].getId(), stops[i].getName());
            }
        } else {
            result = FAIL;
        }
        System.out.println("\nResult: " + result);
        System.out.println("\n*** Test case '" + Thread.currentThread().getStackTrace()[1].getMethodName() + "' finished");
        return st;
    }

    /**
     * Wyswietla informacje o odjazdach lub przyjazdach dla danego przystanku
     *
     * @param departures Obiekt zawierajacy informacje o odjazdach lub przyjazdach
     * @param direction Odjazdy lub przyjazdy
     */
    private void printer(Departures departures, String direction) {
        String stopDirection = direction.equals("departure") ? "Odjazdy" : "Przyjazdy";
        String stopName = departures.getStopName();
        String stopId = departures.getStopShortName();
        System.out.println("\n" + stopDirection + " dla przystanku o id '" + stopId +
                "'" + "i nazwie: " + stopName);
        List<Actual> actualDepartures = departures.getActual();

        if (actualDepartures.isEmpty()) {
            System.out.println("Brak najblizszych odjazdow dla tego przystanku.");
        } else {
            System.out.printf("%-15s %-25s %-15s%n", "Linia", "Kierunek", stopDirection);
            System.out.println("----------------------------------------------------------");
            for (Actual actual : actualDepartures) {
                int time = (int) (actual.getActualRelativeTime() / 60);
                System.out.printf("%-15s %-25s %-15s%n", actual.getPatternText(),
                        actual.getDirection(),
                        time < 1 ? "mniej niz minuta" : time + " min");
            }
        }
    }
}
