package inetserver.csc435.com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
/** File is: InetClient.java, Version 1.8
 A client to send requests to InetServer.java
 Jennifer Haywood after Elliott,Hughes, Shoffner, Winslow
 ----------------------------------------------------------------------*/
public class InetClient {
    public static void main(String[] args) {
        //default serverName is localhost if user doesn't provide args
        String serverName;
        if (args.length < 1) {
            serverName = "localhost";
        } else {
            serverName = args[0];
        }
        System.out.println("Jen's Inent Client::");
        System.out.println("Servername is: " + serverName + ". Should be on port 1600");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        try {
            String hostName;
            do {
                //Get user input and run program until the user types "quit"
                System.out.println("Enter the hostname or IP address. Type 'quit' to end");
                System.out.flush();
                hostName = input.readLine();
                //get the IP address and display to user;
                getRemoteAdress(hostName, serverName);
            } while (!hostName.contains("quit"));
            //alert user that the program will be cancelled and use 0 exit code to abort it
            System.out.println("Connection being cancelled");
            System.exit(0);

        } catch (IOException e) {
            System.out.println("An error has occurred " + e.getMessage());
        }

    }

    static void getRemoteAdress(String name, String hostName) {
        //create variables for Socket, BufferedReader, Printstream and server text
        Socket connectionSocket;
        BufferedReader inputFromServer;
        PrintStream textToServer;
        String serverText;
        try {
            //assign variables the approriate values via instantiation
            connectionSocket = new Socket(hostName, 1600);
            inputFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            textToServer = new PrintStream(connectionSocket.getOutputStream());
            textToServer.println(name);
            textToServer.flush();
            //print 3 lines of text
            int i = 0;
            while (i < 3) {
                serverText = inputFromServer.readLine();
                if (serverText != null) {
                    System.out.println(serverText);
                }
                i++;
            }
            //close the socket
            connectionSocket.close();
        } catch (IOException e) {
            //print any exceptions here
            System.out.println("An error has occurred: " + e.getMessage());
        }


    }
}

