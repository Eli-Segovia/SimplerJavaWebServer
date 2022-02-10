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
		}
	}

	/*
	 * Reads the HTTP request from the client, and
	 * responds with the file the user requested or
	 * a HTTP error code.
	 */
	public void processRequest(Socket s) throws Exception {

		DataInputStream in = new DataInputStream(
				new BufferedInputStream(s.getInputStream()));

		/* used to read data from the client */
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());

		String command = null;
		String pathname = null;
		StringBuilder sb = new StringBuilder();
		String request = null;
		int tries = 0;
		int LIMIT = 10;
		int delay = 1000;
		String line;
		boolean terminate = false;
		while (true) {
			if (!br.ready())
				break;
			if (br.ready()) {
				line = br.readLine();
				System.out.println("line: " + line);
				if (line.length() != 0) {
					sb.append(line);
					sb.append("\n");
				} else {
					request = sb.toString();
					System.out.println("we quit");
					break;
				}
			} else if (br.readLine() == null) {
				continue;
			} else {
				if (tries < LIMIT) {
					tries++;
					Thread.sleep(delay);
				} else {
					System.out.println("stuck here?");
					break;
				}
			}
		}

		System.out.println("request: " + request);
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
			}

			else if (command.equals("PUT")) {
				osw.flush();
				logEntry("server.log", "PUT " + pathname);
				osw = new OutputStreamWriter(s.getOutputStream());
				storeFile(br, osw, pathname);
			}

			else {
				/*
				 * if the request is a NOT a GET,
				 * return an error saying this server
				 * does not implement the requested command
				 */
				osw.write("HTTP/1.0 501 Not Implemented\n\n");
			}

			/* close the connection to the client */
			// osw.close();

			System.out.println("We were able to close connection");

		} else {

		}
		osw.close();
		br.close();
		in.close();
		s.close();
		return;
	}

	public void serveFile(OutputStreamWriter osw,
			String pathname) throws Exception {
		FileReader fr = null;
		int c = -1;
		StringBuilder sb = new StringBuilder();
		long fileSize = 0;

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

			fileSize = new File(pathname).length();

		} catch (Exception e) {
			/*
			 * if the file is not found,return the
			 * appropriate HTTP response code
			 */
			osw.write("HTTP/1.0 404 Not Found\n\n");
			return;
		}

		if (fileSize > 4000) {
			osw.write("HTTP/1.0 403 Forbidden\n\n");
		}
		/*
		 * if the requested file can be successfully opened
		 * and read, then return an OK response code and
		 * send the contents of the file
		 */
		else {
			osw.write("HTTP/1.0 200 OK\n\n");
			while (c != -1) {
				sb.append((char) c);
				c = fr.read();
			}
			osw.write(sb.toString());
		}
		fr.close();
	}

	public void storeFile(BufferedReader br, OutputStreamWriter osw, String pathname) throws Exception {
		FileWriter fw = null;
		StringBuilder sb = new StringBuilder();
		try {
			fw = new FileWriter(pathname);

			while (br.ready()) {
				char c = (char) br.read();
				sb.append(c);
			}
			String body = sb.toString();
			fw.write(body);
			fw.close();
			osw.write("HTTP/1.0 201 Created\n\n");
			osw.flush();
		} catch (Exception e) {
			osw.write("HTTP/1.0 500 Internal Server Error\n\n");
		}
	}

	public void logEntry(String filename, String record) {
		try {
			FileWriter fw = new FileWriter(filename, true);
			fw.write(getTimestamp() + " " + record);
			fw.close();

		} catch (Exception e) {

		}
	}

	public String getTimestamp() {
		return (new Date()).toString();
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
