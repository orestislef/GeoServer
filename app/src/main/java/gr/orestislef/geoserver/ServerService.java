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
import java.util.Iterator;
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

                        String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Connection: close\r\n\r\n";

                        boolean foundAddressKey = false;
                        for (String mParam : getJSONKeys(jsonParams)) {
                            if (mParam.equals(MainActivity.ADDRESS_KEY)) {
                                foundAddressKey = true;
                                break;
                            }
                        }
                        if (foundAddressKey) {
                            Geocoder geocoder = new Geocoder(context);
                            List<Address> addressList = geocoder.getFromLocationName(jsonParams.getString(MainActivity.ADDRESS_KEY), jsonParams.getInt(MainActivity.MAX_RESULTS_KEY));
                            List<MyLocation> address = new ArrayList<>();
                            if (addressList.size() > 0) {
                                for (int i = 0; i < addressList.size(); i++) {
                                    address.add(new MyLocation(
                                            addressList.get(i).getAddressLine(0),
                                            addressList.get(i).getLatitude(),
                                            addressList.get(i).getLongitude()));
                                }
                            } else {
                                address.add(null);
                            }
                            JSONArray jsonArray = new JSONArray();
                            for (int i = 0; i < address.size(); i++) {
                                if (address.get(i) != null)
                                    addMyLocationToJSONArray(
                                            jsonArray,
                                            address.get(i).getAddress(),
                                            address.get(i).getLat(),
                                            address.get(i).getLng());
                            }
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(MainActivity.LOCATION_KEY, jsonArray);

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
                            intent.putExtra(MainActivity.RESPONSE_KEY, jsonResponse);

                            // Send the broadcast
                            context.sendBroadcast(intent);

                        } else {
                            Geocoder geocoder = new Geocoder(context);

                            List<Address> addressList;
                            if (!isInBoundLatLng(
                                    jsonParams.getDouble(MainActivity.LAT_KEY),
                                    jsonParams.getDouble(MainActivity.LNG_KEY))
                                    || !isInBoundMaxResults(jsonParams.getInt(MainActivity.MAX_RESULTS_KEY))) {
                                addressList = new ArrayList<>();
                            } else {
                                addressList = geocoder.getFromLocation(
                                        jsonParams.getDouble(MainActivity.LAT_KEY),
                                        jsonParams.getDouble(MainActivity.LNG_KEY),
                                        jsonParams.getInt(MainActivity.MAX_RESULTS_KEY));
                            }
                            List<String> address = new ArrayList<>();
                            if (addressList.size() > 0) {
                                for (int i = 0; i < addressList.size(); i++) {
                                    address.add(addressList.get(i).getAddressLine(0));
                                }
                            } else {
                                address.add("No address");
                            }

                            JSONArray jsonArray = new JSONArray();
                            for (int i = 0; i < address.size(); i++) {
                                jsonArray.put(i, address.get(i));
                            }
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("address", jsonArray);

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
                            intent.putExtra(MainActivity.MAX_RESULTS_KEY, Integer.toString(jsonParams.getInt(MainActivity.MAX_RESULTS_KEY)));
                            intent.putExtra(MainActivity.RESPONSE_KEY, jsonResponse);

                            // Send the broadcast
                            context.sendBroadcast(intent);

                        }
                    }
                }
            }

            // Close the client socket
            clientSocket.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isInBoundMaxResults(int maxResults) {
        return maxResults < Integer.MAX_VALUE && maxResults > 0;
    }

    private boolean isInBoundLatLng(double lat, double lng) {
        // Check if latitude is out of bounds
        if (lat < -90 || lat > 90) {
            return false; // Latitude is out of bounds
        }

        // Check if longitude is out of bounds
        if (lng < -180 || lng > 180) {
            return false; // Longitude is out of bounds
        }

        // Latitude and longitude are within bounds
        return true;
    }

    public List<String> getJSONKeys(JSONObject jsonObject) {
        Iterator<String> keys = jsonObject.keys();
        List<String> keysList = new ArrayList<>();
        while (keys.hasNext()) {
            keysList.add(keys.next());
        }
        return keysList;
    }

    public void addMyLocationToJSONArray(JSONArray jsonArray, String address, double latitude, double longitude) {
        try {
            JSONObject myLocationObject = new JSONObject();
            myLocationObject.put(MainActivity.ADDRESS_KEY, address);
            myLocationObject.put(MainActivity.LAT_KEY, latitude);
            myLocationObject.put(MainActivity.LNG_KEY, longitude);
            jsonArray.put(myLocationObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}