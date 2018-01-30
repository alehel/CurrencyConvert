package currencyconverter;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple TCP/IP server application which converts between currencies. 
 * Currency rates are based on data retrieved from the European Central Bank - Eurosystem.
 *
 * @author Aleksander Helgaker s199846
 */
public class Server {

    private static int activeConnections = 0;

    public static void main(String[] args) {
        int portNumber = determinePortNumber(args);
        String[][] currencyRates = getCurrencyRates();
        startServerApplication(portNumber, currencyRates);
    }

    /**
     * Takes the args array and returns the user specified port number if one has been provided during launch. If not, resort to default, 5050.
     */
    private static int determinePortNumber(String[] args) {
        int defaultPort = 5050;

        if (args.length == 0) {
            return defaultPort;
        } else if (args.length == 1) {
            try {
                return Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                System.err.printf(args[0] + " is not a valid port number. Resorting to port " + defaultPort);
                return defaultPort;
            }
        } else {
            System.err.printf("Correct usage: java CurrencyConverterServer [<port number>]");
            System.exit(1);
            return defaultPort;
        }
    }

    /**
     * Parses the csv file containing currency rates. Currencies are stored in String[][] currancyRates.
     */
    private static String[][] getCurrencyRates() {
        String[][] currencies = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader("src/currencyconverter/eurofxref.csv"));

            // Get currency codes
            String line = br.readLine();
            if (line == null) {
                throw new IOException("End of file reached while parsing!");
            }
            String[] country = line.split(", ");
            country[0] = "EURO";

            // Get conversion rates
            line = br.readLine();
            if (line == null) {
                throw new IOException("End of file reached while parsing!");
            }
            String[] conversion = line.split(", ");
            conversion[0] = "1";

            // Create the currencies array
            currencies = new String[2][country.length];
            currencies[0] = country;
            currencies[1] = conversion;

        } catch (FileNotFoundException e) {
            System.err.println("Unable to read currency rate file!");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("There was a problem reading the currency information!");
            System.exit(1);
        }

        return currencies;
    }

    /**
     * Listen for and answer TCP requests. Create a separate thread of 
     * ClientHandler class for each connected client.
     */
    private static void startServerApplication(int hostPort, String[][] currencyRates) {
        System.out.println("Listening for requests on port: " + hostPort);

        try (ServerSocket serverSocket = new ServerSocket(hostPort);) {
            while (true) {
                ClientHandler clientHandler = new ClientHandler(serverSocket.accept(), currencyRates);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    /**
     * Used by ClientHandler to increase connection count.
     */
    public static void increaseConnectionCount() {
        activeConnections++;
    }
    
    /**
     * Used by ClientHandler to decrease connection count.
     */
    public static void decreaseConnectionCount() {
        activeConnections--;
    }
    
    /**
     * 
     * @return an int specifying the number of active
     * connections.
     */
    public static int getConnectionCount() {
        return activeConnections;
    }

}
