package gr.orestislef.geoserver;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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
                        List<Address> addressList =
                                geocoder.getFromLocation(
                                        jsonParams.getDouble(MainActivity.LAT_KEY),
                                        jsonParams.getDouble(MainActivity.LNG_KEY),
                                        jsonParams.getInt(MainActivity.MAX_RESULTS));
                        List<String> address = new ArrayList<>();
                        if (addressList.size() > 0) {
                            for (int i = 0; i < addressList.size(); i++) {
                                address.add(addressList.get(i).getAddressLine(0));
                            }
                        } else {
                            address.add("No address");
                        }

                        String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Connection: close\r\n\r\n";

                        JSONArray jsonArray = new JSONArray();
                        for (int i = 0; i < address.size(); i++) {
                            jsonArray.put(i,address.get(i));
                        }
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("address",jsonArray);

                        // Convert the JSONArray to a JSON string
                        String jsonResponse = jsonObject.toString();
                        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

                        // Prepare the response with the headers and bytes
                        byte[] response = new byte[responseHeaders.length() + bytes.length];
                        System.arraycopy(responseHeaders.getBytes(), 0, response, 0, responseHeaders.length());
                        System.arraycopy(bytes, 0, response, responseHeaders.length(), bytes.length);

                        // Send the response back to the client
                        OutputStream outputStream = clientSocket.getOutputStream();
                        outputStream.write(response);
                        outputStream.flush();
                        System.out.println("Sent response: " + Arrays.toString(response));

                        // Create an intent with the broadcast action
                        Intent intent = new Intent("com.example.ACTION_VIEW_CHANGE");
                        intent.putExtra(MainActivity.LAT_KEY, Double.toString(jsonParams.getDouble(MainActivity.LAT_KEY)));
                        intent.putExtra(MainActivity.LNG_KEY, Double.toString(jsonParams.getDouble(MainActivity.LNG_KEY)));
                        intent.putExtra(MainActivity.RESPONSE_KEY, jsonResponse);

                        // Send the broadcast
                        context.sendBroadcast(intent);
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