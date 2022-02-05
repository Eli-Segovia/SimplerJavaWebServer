/***********************************************************************

   SimpleWebServer.java


   This toy web server is used to illustrate security vulnerabilities.
   This web server only supports extremely simple HTTP GET requests.

   This file is also available at http://www.learnsecurity.com/ntk
 
***********************************************************************/

package com.learnsecurity;

import java.io.*;
import java.net.*;
import java.util.*;

public class SimpleWebServer {

	/* Run the HTTP server on this TCP port. */

	private static final int PORT = 3000;

	/*
	 * The socket used to process incoming connections
	 * from web clients
	 */
	private static ServerSocket dServerSocket;

	public SimpleWebServer() throws Exception {
		dServerSocket = new ServerSocket(PORT);
		System.out.println("Running socket on port " + PORT);
	}

	public void run() throws Exception {
		while (true) {
			/* wait for a connection from a client */
			System.out.println("Waiting For Request...");
			Socket s = dServerSocket.accept();
			/* then process the client's request */
			processRequest(s);
			System.out.println("Request Processed");

		}
	}

	/*
	 * Reads the HTTP request from the client, and
	 * responds with the file the user requested or
	 * a HTTP error code.
	 */
	public void processRequest(Socket s) throws Exception {
		/* used to read data from the client */
		InputStream inputStream = s.getInputStream();

		System.out.println(inputStream);
		// DataInputStream in = new DataInputStream(
		// new BufferedInputStream(s.getInputStream()));

		// BufferedReader d = new BufferedReader(new InputStreamReader(in));

		// /* used to write data to the client */
		OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());

		String request = readAll(inputStream);

		/* read the HTTP request from the client */
		// String request = d.readLine();
		System.out.println("This is the reqeust: " + request);

		String command = null;
		String pathname = null;

		/* parse the HTTP request */
		if (!(request == null)) {
			StringTokenizer st = new StringTokenizer(request, " ");

			command = st.nextToken();
			pathname = st.nextToken();
			System.out.println("got " + command + " command");
			System.out.println("got " + pathname + " for pathname");

			if (command.equals("GET")) {
				/*
				 * if the request is a GET
				 * try to respond with the file
				 * the user is requesting
				 */
				serveFile(osw, pathname);
			} else {
				/*
				 * if the request is a NOT a GET,
				 * return an error saying this server
				 * does not implement the requested command
				 */
				osw.write("HTTP/1.0 501 Not Implemented\n\n");
			}

			/* close the connection to the client */
		}

		System.out.println("We were able to close connection");
		osw.close();
		return;
	}

	public void serveFile(OutputStreamWriter osw,
			String pathname) throws Exception {
		FileReader fr = null;
		int c = -1;
		StringBuffer sb = new StringBuffer();

		/*
		 * remove the initial slash at the beginning
		 * of the pathname in the request
		 */
		if (pathname.charAt(0) == '/')
			pathname = pathname.substring(1);

		/*
		 * if there was no filename specified by the
		 * client, serve the "index.html" file
		 */
		if (pathname.equals(""))
			pathname = "index.html";

		/* try to open file specified by pathname */
		try {
			fr = new FileReader(pathname);
			c = fr.read();
		} catch (Exception e) {
			/*
			 * if the file is not found,return the
			 * appropriate HTTP response code
			 */
			osw.write("HTTP/1.0 404 Not Found\n\n");
			return;
		}

		/*
		 * if the requested file can be successfully opened
		 * and read, then return an OK response code and
		 * send the contents of the file
		 */
		osw.write("HTTP/1.0 200 OK\n\n");
		while (c != -1) {
			sb.append((char) c);
			c = fr.read();
		}
		fr.close();
		osw.write(sb.toString());
	}

	public static String readAll(InputStream input) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		StringBuffer buffer = new StringBuffer();
		System.out.println("Started reading");
		while (true) {
			String line;
			try {
				line = reader.readLine();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			if (line == null) {
				break;
			}
			String noWhitespace = line.replaceAll("(?m)^[ \t]*\r?\n", "");
			if (noWhitespace == "") {
				System.out.println("We should be here");
				break;
			} else {
				buffer.append(line);
				buffer.append("\n");
			}

		}
		System.out.println("we are here");
		return buffer.toString();
	}

	/*
	 * This method is called when the program is run from
	 * the command line.
	 */
	public static void main(String argv[]) throws Exception {

		/* Create a SimpleWebServer object, and run it */
		SimpleWebServer sws = new SimpleWebServer();
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		sws.run();
	}
}
