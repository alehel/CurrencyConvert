package currencyconverter;

import java.io.*;
import java.net.*;

/**
 * Sends requests to the server, and prints results to the
 * terminal.
 *
 * @author Aleksander Helgaker s199846
 */
public class Client {

    static String hostName = "10.253.9.87";
    static int portNumber = 5050;
    static String request;

    public static void main(String[] args) throws IOException {
        setConnectionValues(args);

        // BufferedReader for user input
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        
        try ( // auto close resources.
                Socket clientSocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) 
        {
            System.out.println(in.readLine());
            
            boolean keepRunning = true;
            while(keepRunning) {
                // Get the request from the user.
                System.out.print("Please enter currency query. Enter q to exit: ");
                request = stdIn.readLine();
                
                // If user entered q, disconnect from server and end the application
                if (!request.equals("q")) {
                    out.println(request);
                    System.out.println(in.readLine());
                } else {
                    keepRunning = false;
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Unknown host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Could not get I/O for the connection to " + hostName);
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Determines if custom connection values have been set using command line
     * arguments. If so, apply these values to this session.
     *
     * @param args String[] where args[0] contains a port number and args[1]
     * contains a host name, if specified.
     */
    private static void setConnectionValues(String[] args) {
        if (args.length == 1) {
            portNumber = Integer.valueOf(args[0]);
        } else if (args.length == 2) {
            portNumber = Integer.valueOf(args[0]);
            hostName = args[1];
        } else if (args.length > 2) {
            System.err.println("Usage: java CurrencyConverterClient [<port number>] [<host name>] ");
            System.exit(1);
        }
    }
}
