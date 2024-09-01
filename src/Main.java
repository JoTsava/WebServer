
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

/**
 * @author Joseph Kainda Tshava
 * @version WebServer
 */
public class Main {
    static ServerSocket serverSocket;
    static Socket clientSocket;
    public static void main(String[] args) { // application entry point
        try {
            serverSocket = new ServerSocket(4321); // Establishing connection on port 4321
            System.out.println("\nEstablishing connection to web server (port : 4321)\r\nTry \"http://localhost:4321/Joburg.html\"\n");
            while (true) {// infinite loop awaiting client connection
                clientSocket = serverSocket.accept(); System.out.print("\nConnection established ");


                Main main = new Main();
                Main.ClientThread client = new ClientThread(clientSocket);
                client.start(); // Client handler
            }
        } catch (Exception e) {
            System.err.printf("Connection failed: %s\n", e.getMessage());
        }
        finally { // Closing sockets 
            try {
                if (serverSocket != null) serverSocket.close();
                if (clientSocket != null) clientSocket.close();
            } catch (Exception e) { System.err.printf("Closing connection failed: %s\n", e.getMessage());}
        }

    }
    // Class handles client threads
    static class ClientThread extends Thread {
        private final Socket clientSocket;
        BufferedReader bufferedReader;
        DataOutputStream dataOutputStream;
        public ClientThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
        // Method obtaining file type
        private String getFile(File file) {
            String name = file.getName();
            if (name.endsWith(".html")) return "text/html";
             else if (name.endsWith(".jpg")) return "image/jpeg";
             else return "No such file exists!!!";
        }
        @Override
        public void run() {
            try {
                // input/output streams
                bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                String prompt = bufferedReader.readLine(); // Reading client prompts
                System.out.println("\nClient received: " + prompt);
                //dataOutputStream.writeUTF(prompt);
                String[] parts = prompt.split(" ");// parsing prompts. Method->0, url->1
                String method = parts[0];
                String url = parts[1];

                // HTTP GET request
                // TODO : Files if they exist
                if (method.equals("GET")) {
                    //String filepath = url.substring(url.lastIndexOf("/") + 1);
                    String filePath = url.substring(1);
                    File file = new File("data/" + filePath);
                    if (file.exists()) { // Display
                        byte[] fileContent = Files.readAllBytes(file.toPath());

                        dataOutputStream.writeBytes("HTTP/1.1 200 OK\r\n");
                        dataOutputStream.writeBytes("Content-Length: " + fileContent.length + "\r\n");
                        dataOutputStream.writeBytes("Content-Type: " + getFile(file) + "\r\n\r\n");
                        dataOutputStream.write(fileContent);
                    } else if (filePath.equals("video")) {
                        String html = video();
                        dataOutputStream.writeBytes("HTTP/1.1 200 OK\r\n");
                        dataOutputStream.writeBytes("Content-Type: text/html\r\n\r\n");
                        dataOutputStream.writeBytes(html);
                    } else {
                        dataOutputStream.writeBytes("HTTP/1.1 404 Not Found\r\n");
                        dataOutputStream.writeBytes("Content-Type: text/html\r\n\r\n");
                        dataOutputStream.writeBytes("\r\n<html><p1>Error 404 Not Found</p1></html>");
                    }
                }

            } catch (Exception e) {
                System.err.printf("Error handling client requests %s\n", e.getMessage());
            } finally { // Ensuring streams closure
                try {
                    if (bufferedReader != null) bufferedReader.close();
                    if (dataOutputStream != null) dataOutputStream.close();
                    if (clientSocket != null) clientSocket.close();
                } catch (Exception e){
                    System.err.printf("Closing connection failed: %s\n", e.getMessage());
                }
            }
        }
        // Bonus for video streaming
        private String video() {
            return "<html><body><video width=\"640\" height=\"480\" controls>" +
               "  <source src=\"video.mp4\" type=\"video/mp4\">" +
               "  Your browser does not support the video tag." +
               "</video>" +
               "<script>" +
               "  var video = document.getElementById('video');" +
               "  video.addEventListener('play', function() {" +
               "    console.log('Video playing');" +
               "  });" +
               "  video.addEventListener('pause', function() {" +
               "    console.log('Video paused');" +
               "  });" +
               "  video.addEventListener('seeked', function() {" +
               "    console.log('Video seeked');" +
               "  });" +
               "</script>" +
               "</body></html>";
        }
    }
}
