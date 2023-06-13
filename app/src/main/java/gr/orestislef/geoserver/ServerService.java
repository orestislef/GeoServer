package gr.orestislef.geoserver;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ServerService extends Thread {

    Context context;

    public ServerService(Context context) {
        this.context = context;
    }

    public static final int PORT = 8080;
    private boolean isRunning;
    private ServerSocket serverSocket;


    @Override
    public void run() {
        isRunning = true;

        try {
            // Create the server socket
            serverSocket = new ServerSocket(PORT);

            while (isRunning) {
                // Listen for client connections
                Socket clientSocket = serverSocket.accept();

                // Create a new thread to handle the client's request
                Thread clientThread = new Thread(() -> handleClientRequest(context, clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        isRunning = false;

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClientRequest(Context context, Socket clientSocket) {
        try {
            // Read the client's request
            InputStream inputStream = clientSocket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);

            if (bytesRead > 0) {
                String request = new String(buffer, 0, bytesRead);
                System.out.println("Received request: " + request);

                // Extract GET parameters from the request
                String[] requestLines = request.split("\r\n");
                String[] requestParts = requestLines[0].split(" ");
                String requestMethod = requestParts[0];
                String requestUrl = requestParts[1];

                if (requestMethod.equals("GET")) {
                    String[] urlParts = requestUrl.split("\\?");
                    if (urlParts.length > 1) {
                        String query = urlParts[1];
                        String[] queryParams = query.split("&");

                        // Create a JSONObject to store the parameters
                        JSONObject jsonParams = new JSONObject();

                        for (String param : queryParams) {
                            String[] paramParts = param.split("=");
                            if (paramParts.length > 1) {
                                String paramName = paramParts[0];
                                String paramValue = paramParts[1];
                                System.out.println("GET parameter: " + paramName + "=" + paramValue);

                                // Add the parameter to the JSONObject
                                jsonParams.put(paramName, paramValue);
                            }
                        }

                        Geocoder geocoder = new Geocoder(context);
                        List<Address> addressList = geocoder.getFromLocation(jsonParams.getDouble("lat"), jsonParams.getDouble("lng"), 1);
                        String address ;
                        if (addressList.size() > 0) {
                            address = addressList.get(0).getAddressLine(0);
                        } else {
                            address = "No address";
                        }

                        String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Connection: close\r\n\r\n";


                        JSONObject jsonParams2 = new JSONObject();
                        jsonParams2.put("address", address);

                        // Convert the JSONObject to a JSON string
                        String jsonResponse = jsonParams2.toString();

                        // Prepare the response with the JSON string
                        String response = responseHeaders + jsonResponse;

                        // Send the response back to the client
                        OutputStream outputStream = clientSocket.getOutputStream();
                        outputStream.write(response.getBytes());
                        outputStream.flush();
                        System.out.println("Sent response: " + response);
                    }
                }
            }

            // Close the client socket
            clientSocket.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}