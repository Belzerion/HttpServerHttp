///A Simple Web Server (WebServer.java)

package httpServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Example program from Chapter 1 Programming Spiders, Bots and Aggregators in
 * Java Copyright 2001 by Jeff Heaton
 *
 * WebServer is a very simple web-server. Any request is responded with a very
 * simple web-page.
 *
 * @author Jeff Heaton
 * @version 1.0
 */
public class WebServer {

    /**
     * WebServer constructor.
     */
    private String RESOURCES_PATH = "Resources";

    private String FILE_NOT_FOUND_HTML = "Resources/notFound.html";

    private String INDEX_HTML = "Resources/index.html";

    private String NOT_IMPLEMENTED_501 = "Resources/Not implemented.html";

    private String FORBIDDEN_403 = "Resources/forbidden.html";

    private String INTERNAL_500 = "Resources/InternalServerError.html";

    protected void start() throws IOException {
        ServerSocket s;

        System.out.println("Webserver starting up on port 80");
        System.out.println("(press ctrl-c to exit)");
        try {
            // create the main server socket
            s = new ServerSocket(3000);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return;
        }

        System.out.println("Waiting for connection");
        for (;;) {
            Socket remote = null;
            try {
                // wait for a connection
                remote = s.accept();
                // remote is now the connected socket
                System.out.println("Connection, sending data.");
                BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(remote.getOutputStream());

                // read the data sent. We basically ignore it,
                // stop reading once a blank line is hit. This
                // blank line signals the end of the client HTTP
                // headers.
                String str = ".";
                str = in.readLine();

                if(str == null)
                    continue;
                System.out.println("REQUEST: "+str);

                String[] request = str.split(" ");
                String resourceName = request[1];
                String requestType = request[0];
                resourceName = resourceName.substring(1);
                if(resourceName.isEmpty()){
                    GET(out,INDEX_HTML);
                }else if(resourceName.startsWith(RESOURCES_PATH)){
                    if(requestType.equals("GET")){
                        GET(out,resourceName);
                    }
                    else if(requestType.equals("HEAD")){
                        HEAD(out,resourceName);
                    }
                    else if(requestType.equals("POST")){
                        POST(in,out,resourceName);
                    }
                    else if(requestType.equals("PUT")){
                        PUT(in,out,resourceName);
                    }
                    else if(requestType.equals("DELETE")){
                        DELETE(out,resourceName);
                    }
                    else{
                        GET(out, NOT_IMPLEMENTED_501);
                    }
                }
                else{
                    GET(out, FORBIDDEN_403);
                }
                out.flush();
                remote.close();
            } catch (Exception e) {
                System.out.println("Error: " + e);
                remote.close();
            }
        }
    }

    private void DELETE(BufferedOutputStream out, String resourceName) {
        try {
            File resources = new File(RESOURCES_PATH);
            String[] file = resourceName.split("/");
            String resourceWanted = file[1];
            String[] files = resources.list();
            boolean exist = false;
            for (int i = 0; i < files.length; ++i) {
                if (files[i].equals(resourceWanted)) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                out.write(headerWithoutBody("404 Not Found").getBytes());
                BufferedInputStream fileNotFound = new BufferedInputStream(new FileInputStream(FILE_NOT_FOUND_HTML));
                int a;
                byte[] buf = new byte[256];
                while ((a = fileNotFound.read(buf)) != -1) {
                    out.write(buf, 0, a);
                }
                fileNotFound.close();
                out.flush();
                return;
            }
            out.write(headerWithBody("200 OK", RESOURCES_PATH + "/" + resourceWanted, new File(RESOURCES_PATH + "/" + resourceWanted).length()).getBytes());
            File fileToDelete = new File(RESOURCES_PATH + "/" + resourceWanted);
            fileToDelete.delete();
        }catch(Exception e){

        }
    }

    private void PUT(BufferedReader in, BufferedOutputStream out, String resourceName) {
        try {
            File resource = new File(resourceName);
            boolean exists = resource.exists();
            if(!exists)
                resource.createNewFile();
            else{
                resource.delete();
                resource.createNewFile();
            }
            System.out.println(exists);
            PrintWriter eraser = new PrintWriter(resource);
            eraser.close();
            BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(resource));
            while(in.ready()){
                fileWriter.write(in.read());
            }
            fileWriter.flush();
            fileWriter.close();
            if(exists){
                out.write(headerWithoutBody("204 No Content").getBytes());
            }else{
                out.write(headerWithoutBody("201 Created").getBytes());
            }
            out.flush();
        }catch(Exception e){
            e.printStackTrace();
            try{
                out.write(headerWithoutBody("500 Internal Server Error").getBytes());
                out.flush();
            }catch(IOException ebis){
                ebis.printStackTrace();
            }
        }
    }

    private void POST(BufferedReader in, BufferedOutputStream out, String resourceName) {
        try {
            File resource = new File(resourceName);
            boolean exists = resource.exists();
            if(!exists)
                resource.createNewFile();
            System.out.println(exists);
            PrintWriter eraser = new PrintWriter(resource);
            eraser.close();
            BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(resource,true));
            while(in.ready()){
                fileWriter.write(in.read());
            }
            fileWriter.flush();
            fileWriter.close();
            if(exists){
                out.write(headerWithoutBody("204 No Content").getBytes());
            }else{
                out.write(headerWithoutBody("201 Created").getBytes());
            }
            out.flush();
        }catch(Exception e){
            e.printStackTrace();
            try{
                out.write(headerWithoutBody("500 Internal Server Error").getBytes());
                out.flush();
            }catch(IOException ebis){
                ebis.printStackTrace();
            }
        }
    }

    private void HEAD(BufferedOutputStream out, String resourceName) throws IOException {
        try {
            File resources = new File(RESOURCES_PATH);
            String[] file = resourceName.split("/");
            String resourceWanted = file[1];
            String[] files = resources.list();
            boolean exist = false;
            for (int i = 0; i < files.length; ++i) {
                if (files[i].equals(resourceWanted)) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                out.write(headerWithoutBody("404 Not Found").getBytes());
                out.flush();
                return;
            }
            out.write(headerWithBody("200 OK", RESOURCES_PATH + "/" + resourceWanted, new File(RESOURCES_PATH + "/" + resourceWanted).length()).getBytes());
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
            try{
                out.write(headerWithoutBody("500 Internal Server Error").getBytes());
                out.flush();
            }catch(IOException ebis){
                ebis.printStackTrace();
            }
        }
    }

    private void GET(BufferedOutputStream out, String resourceName) {
        try {
            File resources = new File(RESOURCES_PATH);
            String[] file = resourceName.split("/");
            String resourceWanted = file[1];
            String[] files = resources.list();
            boolean exist = false;
            for (int i = 0; i < files.length; ++i) {
                if (files[i].equals(resourceWanted)) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                out.write(headerWithoutBody("404 Not Found").getBytes());
                BufferedInputStream fileNotFound = new BufferedInputStream(new FileInputStream(FILE_NOT_FOUND_HTML));
                int a;
                byte[] buf = new byte[256];
                while ((a = fileNotFound.read(buf)) != -1) {
                    out.write(buf, 0, a);
                }
                fileNotFound.close();
                out.flush();
                return;
            }
            out.write(headerWithBody("200 OK", RESOURCES_PATH + "/" + resourceWanted, new File(RESOURCES_PATH + "/" + resourceWanted).length()).getBytes());
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(resourceName));
            int a;
            byte[] buf = new byte[256];
            while ((a = in.read(buf)) != -1) {
                out.write(buf, 0, a);
            }
            in.close();
            out.flush();
        }catch (IOException e){
            e.printStackTrace();
            try{
                out.write(headerWithoutBody("500 Internal Server Error").getBytes());
                BufferedInputStream fileNotFound = new BufferedInputStream(new FileInputStream(INTERNAL_500));
                int a;
                byte[] buf = new byte[256];
                while ((a = fileNotFound.read(buf)) != -1) {
                    out.write(buf, 0, a);
                }
                fileNotFound.close();
                out.flush();
                out.flush();
            }catch(IOException ebis){
                ebis.printStackTrace();
            }
        }
    }

    protected String headerWithoutBody(String status) {
        String header = "HTTP/1.1 " + status + "\r\n";
        header += "Server: Bot\r\n";
        header += "\r\n";
        return header;
    }

    public String headerWithBody(String status,  String file, long length){
        String header = "HTTP/1.1 " + status + "\r\n";
        if(file.endsWith(".html"))
            header += "Content-Type: text/html\r\n";
        else if(file.endsWith(".jpeg") || file.endsWith(".jpg"))
            header += "Content-Type: image/jpeg\r\n";
        else if(file.endsWith(".png"))
            header += "Content-Type: image/png\r\n";
        else if(file.endsWith(".mp3")){
            header += "Content-Type: audio/mpeg\r\n";
            //header += "Content-Disposition: attachment; filename=\""+file+"\"\r\n";
        }
        else if(file.endsWith(".mp4"))
            header += "Content-Type: video/mpeg\r\n";
        header += "Content-Length: " + length + "\r\n";
        header += "Server: Bot\r\n";
        header += "\r\n";
        return header;
    }

    /**
     * Start the application.
     *
     * @param args
     *            Command line parameters are not used.
     */
    public static void main(String args[]) throws IOException {
        WebServer ws = new WebServer();
        ws.start();
    }
}

