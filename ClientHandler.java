package currencyconverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClientHandler class is responsible for all interaction between the server and
 * client. Instances of the ClientHandler class are created by the Server class.
 *
 * @author Aleksander Helgaker s199846
 */
public class ClientHandler extends Thread {

    Socket connectSocket;
    String clientAddr; // ip address of this client.
    int serverPort, clientPort;
    String[][] currencyRates;
    PrintWriter outputStream; // sends string information to the client
    BufferedReader inputStream; // gets string information from the client

    public ClientHandler(Socket connectSocket, String[][] currencyRates) {
        this.connectSocket = connectSocket;
        clientAddr = connectSocket.getInetAddress().getHostAddress();
        clientPort = connectSocket.getPort();
        serverPort = connectSocket.getLocalPort();
        this.currencyRates = currencyRates;

        Server.increaseConnectionCount();
    }

    public void run() {
        try (PrintWriter out = new PrintWriter(connectSocket.getOutputStream(), true);
                BufferedReader in = inputStream = new BufferedReader(
                        new InputStreamReader(connectSocket.getInputStream()));) {
            outputStream = out;
            inputStream = inputStream;
            
            System.out.println("[" + clientAddr + "] has connected to server.");
            out.println("Connected to server. Type help for information on how to "
                    + "use the application.");

            // Handle requests from client as long as input stream exists.
            String clientRequest;
            while ((clientRequest = inputStream.readLine()) != null) {
                System.out.println("[" + clientAddr + "] Received request: " + clientRequest);
                processRequest(clientRequest);
            }

            connectSocket.close();
        } catch (IOException e) {
            System.err.println("There was an I/O exception with client " + clientAddr + ".");
            System.err.println(e.getMessage());
        } finally {
            Server.decreaseConnectionCount();
        }
    }

    /**
     * Process the request. Performs a regex on the request to determine what
     * kind of request it is. The request is then passed on to the relevant
     * processing method for that request.
     */
    private void processRequest(String request) {
        if (request.equals("kill")) { // kill the server application
            kill();
        } else if (request.equals("active")) { // get number of active users
            printResult("Number of active clients: " + Server.getConnectionCount());
        } else if (request.matches("^([0-9]+)([A-Z]+)[2]([A-Z]+)$")) { // convert currencies
            performCurrencyConversion(request);
        } else { // invalid request
            printResult("Request could not be processed. Invalid syntax.");
        }
    }

    /**
     * Method for printing information to both server terminal and client
     * terminal.
     *
     * @param result the String to be written to the terminals.
     */
    private void printResult(String result) {
        outputStream.println(result);
        System.out.println("[" + clientAddr + "] Returning result: " + result);
    }

    /**
     * Shuts down the server.
     */
    private void kill() {
        printResult("Server shutdown command issued!");
        System.exit(0);
    }

    /**
     * Converts between two currencies. Prints a relevant error message on the
     * terminal if there was an issue performing the conversion.
     *
     * @param request a String specifying the conversion to perform. E.g.
     * 100USD2NOK.
     */
    private void performCurrencyConversion(String request) {
        String regex = "^([0-9]+)([A-Za-z]+)[2]([A-Za-z]+)$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(request);
        m.find();

        float amount = Float.valueOf(m.group(1));

        String returnValue;
        if (!validCurrency(m.group(2)) && !validCurrency(m.group(3))) { // both currencies are not valid
            returnValue = m.group(2) + " and " + m.group(3) + " are not valid currencies.";
        } else if (!validCurrency(m.group(2))) { // first currency is not valid
            returnValue = m.group(2) + " is not a recognized currency.";
        } else if (!validCurrency(m.group(3))) { // second currancy is not valid
            returnValue = m.group(3) + " is not a recognized currency.";
        } else { // valid currencies, calculate result.
            float result = convertCurrency(amount, m.group(2), m.group(3));
            returnValue = amount + " " + m.group(2) + " in " + m.group(3) + " = " + result + ".";
        }

        printResult(returnValue);
    }

    /**
     * Checks if the client request is in a valid format.
     */
    private boolean regexCurrencyConversion(String request) {
        String regex = "^[0-9]+[A-Za-z]+[2][A-Za-z]+$"; // example 500USD2NOK
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(request);
        if (m.find()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if a currency is listed in the currencyRates array.
     *
     * @param currency
     * @return a boolean specifying if the currency was found.
     */
    private boolean validCurrency(String currency) {
        // we start looking at indek 1 because index 0 is the date.
        for (int i = 0; i < currencyRates[0].length; i++) {
            if (currencyRates[0][i].equals(currency)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts between two currencies.
     *
     * @param amount the amount to convert.
     * @param from the currency to convert from.
     * @param to the currency to convert to.
     * @return the result of the currency conversion.
     */
    private float convertCurrency(float amount, String from, String to) {
        float fromCurrency = getConversionRateOf(from);
        float toCurrency = getConversionRateOf(to);
        return (amount / fromCurrency) * toCurrency;
    }

    /**
     * Gets the conversion rate of the specified currency.
     *
     * @param currency
     * @return a float specifying the conversion rate.
     */
    private float getConversionRateOf(String currency) {
        float returnValue = 1.0f;

        for (int i = 0; i < currencyRates[0].length; i++) {
            if (currencyRates[0][i].equals(currency)) {
                returnValue = Float.valueOf(currencyRates[1][i]);
            }
        }

        return returnValue;
    }
}
