package inetserver.csc435.com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Arrays;

/** File is: InetServer.java, Version 1.8
 A multithreaded server for InetClient.java.
 Jennifer Haywood after Elliott,Hughes, Shoffner, Winslow
 ----------------------------------------------------------------------*/
public class InetServer{
    public static void main(String[] args) throws IOException {
        //queue requests
        int qRequests=6;
        //allocated port numbers
        int port=1600;
        // define a client socket
        Socket clientSocket;
        //instantiate a serversocket object with the port and number of queue requestis
        ServerSocket serverSocket = new ServerSocket(port,qRequests);
        System.out.println("Jen's Inet Server starting up on port 1600");
        //inifinite loop that will begin to client socket socket
        do {
            //Listens for a connection to be made to this socket and accepts it.
             clientSocket = serverSocket.accept();
             //start the worker thread
            new Worker(clientSocket).start();
        } while (true);
    }

    public static class Worker extends Thread {
        //known as the client socket
        Socket socket;

        //Worker constructor that will take in the client socket
        public Worker(Socket socket) {
            this.socket = socket;
        }

        //lets run this baby
        @Override
        public void run() {
            PrintStream output = null;
            String serverName;
            try {
                // declare BufferedReader object to read input for socket
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //declare object to
                output = new PrintStream(socket.getOutputStream());
                serverName = input.readLine();
                System.out.println("SEARCHING FOR the following serverName: "  + serverName);
                displayRemoteAddress(serverName, output);
            } catch (IOException e ) {
                //alert the user of the exception
                output.println("The following exception occurred: " + e.getMessage());
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

         void displayRemoteAddress(String hostName, PrintStream output) {

            try {
                //lets get the hostMachine's name
                InetAddress hostMachine = InetAddress.getByName(hostName);
                //display pertinent information to the user
                output.println("Looking for:: " + hostName);
                output.println("HostName is " + hostMachine.getHostName());
                output.printf(MessageFormat.format("Host IP is {0}", hostMachine.getHostAddress()));
            } catch (UnknownHostException e) {
                //alert the user of the exception
                output.println("The following exception occurred: " + e.getMessage());
            }
        }

    }



}