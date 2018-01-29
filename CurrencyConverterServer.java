import java.io.*;
import java.util.Arrays;
import java.net.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * A simple TCP/IP server application which converts between currencies. Currency rates are based
 * on data retrieved from the European Central Bank - Eurosystem.
 *
 * @author Aleksander Helgaker s199846
 */
public class CurrencyConverterServer {
	private static int activeConnections = 0;

    public static void main(String[] args) {
		int portNumber = determinePortNumber(args); // determines if a user defined port is to be used.
        String[][] currencyRates = getCurrencyRates(); // get currency conversion rates.
		startServerApplication(portNumber, currencyRates);
    }

	/**
	* Takes the args array and returns the user specified port number if one has been
	* provided during launch.
	*/
    private static int determinePortNumber(String[] args) {
		int defaultPort = 5050;

		if(args.length == 0) {
			return defaultPort;
		} else if (args.length == 1) {
			try {
				return Integer.valueOf(args[0]);
			} catch(NumberFormatException e) {
				System.err.printf(args[0] + " is not a valid port number. Resorting to port " +
						defaultPort);
				return defaultPort;
			}
		} else {
			System.err.printf("Correct usage: java CurrencyConverterServer [<port number>]");
			System.exit(1);
			return defaultPort;
		}
	}

	/**
	* Parses the csv file containing currency rates. Currencies are
	* stored in String[][] currancyRates.
	*/
	private static String[][] getCurrencyRates() {
		String[][] currencies = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader("eurofxref.csv"));

			// Get currency codes
			String line = br.readLine();
			if(line == null) throw new IOException("End of file reached while parsing!");
			String[] country = line.split(", ");
			country[0] = "EURO";

			// Get conversion rates
			line = br.readLine();
			if(line == null) throw new IOException("End of file reached while parsing!");
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
    * Listen for and answer TCP requests.
    */
    private static void startServerApplication(int hostPort, String[][] currencyRates) {
		System.out.println("Listening for requests on port: " + hostPort);

		try (
			// create server socket
			ServerSocket serverSocket = new ServerSocket(hostPort);
		)
		{
			// Continously listen for client connections and create individuel threads for each one.
			while(true) {
				ClientHandler clientHandler =
					new CurrencyConverterServer.ClientHandler(serverSocket.accept(), currencyRates);
				clientHandler.start();
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}


	}

	/**
	* Inner class for the client threads.
	*/
	static class ClientHandler extends Thread {
		Socket connectSocket;
		String clientAddr;
		int serverPort,clientPort;
		String[][] currencyRates;

		public ClientHandler(Socket connectSocket, String[][] currencyRates) {
			this.connectSocket = connectSocket;
			clientAddr = connectSocket.getInetAddress().getHostAddress();
			clientPort = connectSocket.getPort();
			serverPort = connectSocket.getLocalPort();
			this.currencyRates = currencyRates;
		}

		public void run() {
			try (
				PrintWriter outputStream =
					new PrintWriter(connectSocket.getOutputStream(), true);
				BufferedReader inputStream =
					new BufferedReader(new InputStreamReader(connectSocket.getInputStream()));
			)
			{
				// New client has connected.
				activeConnections++;
				System.out.println("[" + clientAddr + "] connected to server. " + activeConnections +
					" active connection(s)");

				// Handle requests from client
				String clientRequest;
				while((clientRequest = inputStream.readLine()) != null) {
					System.out.println("["+clientAddr + "] Received request: " + clientRequest);
					String result = processRequest(clientRequest);
					System.out.println("[" + clientAddr + "] Returning result: " + result);
					outputStream.println(result);
				}

				// Client has disconnected
				--activeConnections;
				System.out.println("[" + clientAddr + "] disconnected from server. " + activeConnections +
					" active connection(s) remain");
				connectSocket.close();
			} catch (IOException e) {
				System.err.println("There was an I/O exception with client " + clientAddr + ".");
			}
		}

		/**
		* Checks if the client request is in a valid format.
		*/
		private boolean requestInValidFormat(String request) {
			String regex = "^[0-9]+[A-Z]+[2][A-Z]+$"; // example 500USD2NOK
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(request);
			if(m.find()) {
				return true;
			} else {
				return false;
			}
		}

		/**
		* Checks to see if a currency is listed.
		*/
		private boolean validCurrency(String currency) {
			// we  start looking at indek 1 because index 0 is the date.
			for(int i = 0; i < currencyRates[0].length; i++) {
				if(currencyRates[0][i].equals(currency)) {
					return true;
				}
			}
			return false;
		}

		/**
		* Processes the request and returns the result to be delivered to the client. The result
		* could either be a currency conversion, or an error message if something went wrong.
		*/
		private String processRequest(String request) {
			if(request.equals("kill")) {
				System.out.println("Server shutdown in progress.");
				System.exit(0);
			}

			// Check if request is valid
			String regex = "^([0-9]+)([A-Z]+)[2]([A-Z]+)$"; // example 500USD2NOK
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(request);

			if(!m.find()) {
				return "Request could not be processed. Invalid syntax.";
			}

			float amount = Float.valueOf(m.group(1));

			if(!validCurrency(m.group(2))) {
				return m.group(2) + " is not a recognized currency.";
			}

			if(!validCurrency(m.group(3))) {
				return m.group(3) + " is not a recognized currency.";
			}

			float result = convertCurrency(amount, m.group(2), m.group(3));
			return amount + " " + m.group(2) + " in " + m.group(3) + " = " + result + ".";

		}

		/**
		* Converts between two different currencies, returning the result of the conversion.
		*/
		private float convertCurrency(float amount, String from, String to) {
			float fromCurrency = getConversionRateOf(from);
			float toCurrency = getConversionRateOf(to);
			return (amount/fromCurrency) * toCurrency;
		}

		/**
		* Gets the current conversion rate for the specified currency.
		*/
		private float getConversionRateOf(String currency) {
			float returnValue = 1.0f;

			for(int i = 0; i < currencyRates[0].length; i++) {
				if(currencyRates[0][i].equals(currency)) {
					returnValue = Float.valueOf(currencyRates[1][i]);
				}
			}

			return returnValue;
		}
	}
}
