import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/*
This class is the meat of the application. It extends Thread and kicks off the run method.
It is called in the main method.

Jennifer Haywood
Use  javac file to compile code
 1. Run WebServer.java
 2. Connect to http://localhost:2540/
 3.Navigate to FireFox
 4. View the root directory
 4. Click on the file to view it. If it doesn't load, the fileType isn't supported

Please note, you can right click and download files such as .class, png, docx.
 Included Files:
 a. checklist-mywebserver.html
 b. WebServer.java
 c. logs.txt
 d. directory.html
 e. webServerPic.png
* */
class ServerWorker extends Thread {

    Logger myLogger = Logger.getLogger(ServerWorker.class.getName());

    Socket client;

    public ServerWorker(Socket client) {
        this.client = client;
    }

    {
        FileHandler fileHandler = null;
        try {
            fileHandler = new FileHandler("server.txt", false);
            myLogger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
    The run method starts by using a StringTokenizer object to parse the request. it also makes sure the user doesn't try to access
    a directory other than this one. Based on the request, it will get the appropriate file.
    * */
    public void run() {
        try {

            PrintStream serverOutput = new PrintStream(client.getOutputStream());
            BufferedReader
                    bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String request = bufferedReader.readLine();
            String file;
            StringTokenizer requestParser = new StringTokenizer(request, " ");

            String httpMethod = requestParser.nextToken();

            file = processTheRequest(requestParser, httpMethod);

            String extension = extractFileExtension(file);

            String contentType = extractContentTypeFromExt(file);
            myLogger.info("Requested File " + file);
            myLogger.info("The file extension is:  " + extension);
            myLogger.info("File content type is  " + contentType);

            handleFileRequest(serverOutput, file, extension, contentType);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
/*
* This method will as it states, process the request
* */
    private String processTheRequest(StringTokenizer requestParser, String httpMethod) {
        String file;
        if (httpMethod.equalsIgnoreCase("GET")) {
            file = requestParser.nextToken();
            if (file.contains("..") || file.indexOf(':') >= 0
                    || file.indexOf('|') >= 0) {
                throw new RuntimeException();
            }
        } else {
            file = null;
        }
        if (file == null) {
            myLogger.info("An error has occurred!!This is not a get request.");
            throw new RuntimeException();
        }
        return file;
    }

    /*
     *This method attempts to process a .cgi form that should input nums. You should see that it attempts to get the query parameters from the request/
     * Using a series of parsing mechanisms from, the parameters are extracted and put into a map.
     * */
    public void processForm(String request, PrintStream response, String contentType) throws Exception {
        response.println("HTTP/1.1 200 OK Content-Type: " + contentType + "\r\n\r\n");
        response.println("<html><head></head><body>");
        Map<String, String> paramMap = new LinkedHashMap<String, String>();

        URL url = new URL("http:/" + request);
        String queryString = url.getQuery();

        String[] numbers = queryString.split("&");
        myLogger.info("Numbers in query params  " + numbers);
        int equal;
        for (String numFromForm : numbers) {
            equal = numFromForm.indexOf("=");
            paramMap.put(URLDecoder.decode(numFromForm.substring(0, equal), "UTF-8"), URLDecoder.decode(numFromForm.substring(equal + 1), "UTF-8"));
        }
        try {
            int sum = Integer.parseInt(paramMap.get("firstNum")) + Integer.parseInt(paramMap.get("secondNum"));
            String printedSum = "<font size =200><font color = 'MediumVioletRed'> </font><font color='MediumVioletRed'></font></font>\n" + paramMap.get("firstNum") + paramMap.get("secondNum") + sum;
            response.println(printedSum);
            System.out.println("The two numbers are:  " + paramMap.get("firstNum") + "+" + paramMap.get("secondNum"));
        } catch (java.lang.NumberFormatException e) {
            response.println("<p><b>One of the inputs is not a number or is too big!</b></p>");
            response.println("</body></html>");
            response.flush();
        }

    }

    /*
    This string utility method will extract the file's extension.
    * */
    public String extractFileExtension(String fileName) {
        String fileExtension = "";
        int stringIndex = fileName.lastIndexOf('.');
        if (stringIndex > 0) {
            fileExtension = fileName.substring(stringIndex + 1);
        }
        return fileExtension;

    }

    /*
    This method will return a string of the contentType based on the file
    As you can see it just uses string manipulation methods to extract the contentType.
    * */
    public String extractContentTypeFromExt(String file) {
        //the server contentType based on the if branch
        String contentType;
        if (file.endsWith(".html") || file.endsWith(".htm") || file.endsWith("/")) {
            contentType = "text/html";
        }
        if (file.contains("fake-cgi")) {
            contentType = "fake-cgi";
        }
        contentType = "text/plain";
        return contentType;
    }

    /*
    This method shows the file. It uses String methods to extract the content type is successfully sent, the file is ready to be displayed.
    Number of bytes are allocated to write the file and the response-output stream writes it.
    If its a word document, download it because you will not be able to see it in the browser.
    * */
    public void showFile(String fileName, PrintStream response, String contentType) throws IOException {
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        InputStream inputStream = new FileInputStream(fileName);
        File namedFile = new File(fileName);

        response.print("HTTP/1.1 200 OK" + "Content-Length: " + namedFile.length() + "Content-Type: " + contentType + "\r\n\r\n");
        System.out.println("Sending the file that you asked for. Name is " + fileName);
        byte[] bytes = new byte[24000];
        int lengthOfBytes = inputStream.read(bytes);
        response.write(bytes, 0, lengthOfBytes);
        response.flush();
        inputStream.close();
    }


    /*
    This method writes the directory as html so that a user can navigate through the directory
    All the exising files in the directory will be listed here. If there is only a file, myWriter will
    write the file as a link.
    * */
    public void displayDirectory(String directoryName, PrintStream response, String contentType) throws
            IOException {
        String directoryRoot = directoryName;
        BufferedWriter myWriter = new BufferedWriter(new FileWriter("directory.html"));
        //get the directory
        File firstFile = new File("./" + directoryRoot + "/");
        File[] directoryFiles = firstFile.listFiles();

        myWriter.write("<html><head></head>");
        myWriter.write("<body link='MediumVioletRed'>");
        myWriter.write("<font size=200><font color = MediumVioletRed> Directory: " + directoryRoot + "</font></font>" + "<br>");
        myWriter.write("<a href=\"" + "http://localhost:2540" + "/\">" + "Back to Home Directory" + "</a>");
        if (directoryRoot.endsWith("/") && !directoryRoot.equals("/")) {
            int spliceDir = 0;
            String[] substrings = directoryRoot.split("/");
            spliceDir = substrings[1].length() + 1;
            directoryRoot = directoryRoot.substring(0, directoryRoot.length() - spliceDir - 1);
        }
        myWriter.write("<br></br>");

        if (directoryFiles != null) {
            for (File file : directoryFiles) {
                String fileName = file.getName();
                if (fileName.startsWith(".") || fileName.startsWith("directory.html")) {
                    continue;
                }
                if (file.isDirectory()) {
                    myWriter.write("<a href=\"" + fileName + "/\">/" + fileName + "</a> <br>");
                }
                if (file.isFile()) {
                    myWriter.write("<a href=\"" + fileName + "\" >" + fileName + "</a> <br>");

                }
                myWriter.flush();
            }
        }
        myWriter.write("</body></html>");
        File tempServerFile = new File("directory.html");

        InputStream fileInputStream = new FileInputStream("directory.html");
        response.println("HTTP/1.1 200 OK" + "Content-Length: " + tempServerFile.length() + "Content-Type: " + contentType + "\r\n\r\n");
        byte[] bytesInFile = new byte[24000];
        int bytesLength = fileInputStream.read(bytesInFile);
        response.write(bytesInFile, 0, bytesLength);
        myWriter.close();
        response.flush();
        fileInputStream.close();
        tempServerFile.delete();
    }

    /*
    This method will handle each file request based on its file extension.
    * */
    private void handleFileRequest(PrintStream serverOutput, String file, String extension, String contentType) throws Exception {
        switch (extension.toLowerCase()) {
            case "html":
                showFile(file, serverOutput, contentType);
                break;
            case "txt":
                showFile(file, serverOutput, contentType);
                break;
            case "/":
                displayDirectory(file, serverOutput, contentType);
                break;
            case "fake-cgi":
                processForm(file, serverOutput, contentType);
                break;
            case "java":
                showFile(file, serverOutput, contentType);
                break;
            case "class":
                showFile(file, serverOutput, contentType);
                break;
            default:
                displayDirectory(file, serverOutput, contentType);
                break;
        }
    }
}

/*
This class kicks off the ServerWorker class. It instantiates a Socket server,
starts an infinite loop which will begin a connection to start the ServerWorker thread.
* */
public class WebServer {
    static int port = 2540;
    static int numOfQueue = 6;

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(port, numOfQueue);
        System.out.println("Webserver starting on port " + port);
        //infinite loop
        while (true) {
            //create client and assign to server.accept
            Socket client = server.accept();
            //start the WorkerServer thread
            new ServerWorker(client).start();
        }

    }
}
