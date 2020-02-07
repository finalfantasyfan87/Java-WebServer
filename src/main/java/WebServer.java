import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

//this is a worker class that extends thread to use run
//this class will  hold the methods that will read file in the directory, decide which HTTP method to use
class WorkerServer extends Thread {
    Socket client;

    public WorkerServer(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        System.out.println(getFileExtension("hi.txt"));

        try {
            BufferedReader request = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintStream response = new PrintStream(client.getOutputStream());
            String requestString = request.readLine();
            String filePath = "";
            StringTokenizer parser = new StringTokenizer(requestString," ");
            String httpMethod = parser.nextToken();
            filePath = parser.nextToken();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getFileExtension(String fileName) {
        String fileExt = null;
        if (fileName != null || !fileName.isEmpty()) {
            int extIndex = fileName.lastIndexOf('.');
            if (extIndex > 0) {
                fileExt = fileName.substring(extIndex + 1);
            }
        }
        return fileExt;
    }


    public String getContentType(String fileExtension) {
        String contentType = null;
        while (fileExtension != null) {
            switch (fileExtension) {
                case ".html":
                    contentType = "text/html";
                case ".htm":
                    contentType = "text/html";
                case "./":
                    contentType = "text/html";
                case "fake-cg":
                    contentType = "fake-cg";
                case "text/plain":
                    contentType = "text/plain";
            }

        }
        return contentType;
    }

    public void getServerDirectory(String directory, String contentType, PrintStream output) throws IOException {
        String myDirectory = "";
        BufferedWriter directoryWriter = new BufferedWriter(new FileWriter("directory.html"));
        File root = new File("./" + directory + "/");
        File[] files = root.listFiles();
        directoryWriter.write("<html><head></head>");
        directoryWriter.write("<body link='purple'>");
        directoryWriter.write("<font size=200><font color = purple> Directory: " + myDirectory + "</font></font>" + "<br>");
        //create a link back to the home directory
        directoryWriter.write("<a href=\"" + "http://localhost:2540" + "/\">" + "Back to Home Directory" + "</a>");

        if (directory.endsWith("/") && !directory.equals("/")) {
            int spliceDir = 0;
            String[] substrings = directory.split("/");
            spliceDir = substrings[1].length() + 1;
            myDirectory = myDirectory.substring(0, myDirectory.length() - spliceDir - 1);
        }
        for (File file : files) {
            String nameOfFile = file.getName();
            createDirectoryLinks(directoryWriter, file, nameOfFile);

        }
        directoryWriter.flush();
        directoryWriter.write("</body></html>");
        File tempFile = new File("displayDirectory.html");
        //create instance of File Input Stream
        InputStream fileInputStream = new FileInputStream("displayDirectory.html");
        //send header information to browser
        output.println("HTTP/1.1 200 OK" + "Content-Length: " + tempFile.length() + "Content-Type: " + contentType + "\r\n\r\n");
        //allocate bytes and have the input stream read them
        byte[] fileBytes = new byte[18000];
        int bytesLength = fileInputStream.read(fileBytes);
        output.write(fileBytes, 0, bytesLength);
        //close the file writer
        directoryWriter.close();
        //flush the server output
        output.flush();
        //close the file input stream
        fileInputStream.close();
        //delete the temp file
        tempFile.delete();
    }

    private void createDirectoryLinks(BufferedWriter directoryWriter, File file, String nameOfFile) throws IOException {
        if (nameOfFile.startsWith(".")) {
            return;
        }
        if (nameOfFile.startsWith("directory.html")) {
            return;
        }
        if (file.isDirectory()) {
            directoryWriter.write("<a href=\"" + nameOfFile + "/\">/" + nameOfFile + "</a><br>");
        }
        if (file.isFile()) {
            directoryWriter.write("<a href=\"" + nameOfFile + "/\">" + nameOfFile + "</a><br>");
        }
    }

    public void downloadFile() {

    }

}

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
            new WorkerServer(client).start();
        }

    }
}
