package Server;

import java.io.*;
import java.net.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;

/**
 * A simple TCP/IP server application which converts between currencies.
 * Currency rates are based on data retrieved from the European Central Bank -
 * Eurosystem.
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
     * Takes the args array and returns the user specified port number if one
     * has been provided during launch. If not, resort to default, 5050.
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
     * Gets the latest currency conversion rates from Eurosystem and returns it
     * as a csv file in a BufferedReader. If the latest file cannot be aquired
     * from the Eurosystem server, a previous version is used.
     *
     * @return a BufferedReader containing the currency conversion rates in CSV
     * format.
     * @throws IOException
     */
    private static BufferedReader downloadCurrenciesAsBufferedReader() throws IOException {
        // Download the file.
        try {
            URL url = new URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref.zip");
            File zip = new File("eurofxref.zip"); // temporary storage location
            FileUtils.copyURLToFile(url, zip);
        } catch (IOException e) {
            System.out.println("Unable to download latest currency rates. Resorting to older numbers.");
        }

        // Get the csv file from the zip.
        ZipFile zipFile = new ZipFile("eurofxref.zip");
        ZipEntry csvFile = zipFile.getEntry("eurofxref.csv");

        // Place the csv file in a BufferedReader and return to caller.
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(csvFile)));
        return br;
    }

    /**
     * Parses the csv file containing currency rates. Currencies are stored in
     * String[][] currancyRates.
     */
    private static String[][] getCurrencyRates() {
        String[][] currencies = null;
        try {
            // Get the latest CSV of the currency as a buffered reader.
            BufferedReader br = downloadCurrenciesAsBufferedReader();

            // Get currency codes
            String line = br.readLine();
            if (line == null) {
                throw new IOException("End of file reached while parsing!");
            }
            String[] country = line.split(", ");

            // Get conversion rates
            line = br.readLine();
            if (line == null) {
                throw new IOException("End of file reached while parsing!");
            }
            String[] conversion = line.split(", ");

            // Create the currencies array
            currencies = new String[2][country.length];
            currencies[0] = country;
            currencies[1] = conversion;

            // CSV file does not contain information on the base currency.
            // Add this to the table. 
            currencies = addBaseCurrencyToTable("EURO", currencies);
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
     * Adds the base currency to the table. Base currency always has 1 as its
     * currency.
     *
     * @param currencies a String specifying the name of the base currency.
     * @return
     */
    private static String[][] addBaseCurrencyToTable(String baseCurrency, String[][] currencies) {
        int length = currencies[0].length + 1;
        String[][] newCurrencies = new String[2][length];

        for (int i = 0; i < length - 1; i++) {
            newCurrencies[0][i] = currencies[0][i];
        }

        for (int i = 0; i < length - 1; i++) {
            newCurrencies[1][i] = currencies[1][i];
        }

        newCurrencies[0][length - 1] = baseCurrency;
        newCurrencies[1][length - 1] = "1";

        return newCurrencies;
    }

    /**
     * Listen for and answer TCP requests. Create a separate thread of
     * ClientHandler class for each connected client.
     */
    private static void startServerApplication(int hostPort, String[][] currencyRates) {
        System.out.println("Currencies last updated: " + currencyRates[1][0]);
        System.out.println("Listening for requests on port: " + hostPort);

        try (ServerSocket serverSocket = new ServerSocket(hostPort);) {
            while (true) {
                ClientHandler clientHandler = new ClientHandler(serverSocket.accept(), currencyRates);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.err.println("Could not aquire files from Eurosystem. Resorting old data.");
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
     * @return an int specifying the number of active connections.
     */
    public static int getConnectionCount() {
        return activeConnections;
    }

}
