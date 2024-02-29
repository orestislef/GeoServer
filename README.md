# GeoServer

GeoServer is an Android app that functions as a server. When a request is made to the phone's IP:PORT using the GET method, it performs certain actions.

## Main App Screen:

- On the top, there are 2 bubbles: Green/Red light with text indicating whether everything is okay.
- A button is provided to START/STOP the server.
- IP:PORT information is displayed in the middle of the screen.
- The last request is shown at the bottom.

## Request Examples:

### Example 1:

- URL: `http://<IP:PORT>/?lat=40.631165&lng=22.945489&max=5`
- Parameters:
  - `lat`
  - `lng`
  - `max`
- Returns address names:

```json
{
  "address": [
    "π.μελλα, Τσιμισκή, Θεσσαλονίκη 546 22, Ελλάδα",
    "Τσιμισκή 79, Θεσσαλονίκη 546 22, Ελλάδα",
    "Ικτίνου 2, Θεσσαλονίκη 546 22, Ελλάδα",
    "Τσιμισκή 79-65, Θεσσαλονίκη 546 22, Ελλάδα",
    "Θεσσαλονίκη 546 22, Ελλάδα"
  ]
}
```

### Example 2:

- URL: `http://<IP:PORT>/?address=karaikskaki%2027%20sykies&max=5`
- Parameters:
  - `address`
  - `max`
- Returns location with address names and latitudes/longitudes:

```json
{
  "location": [
    {
      "address": "Καραϊσκάκη, Συκιές 566 26, Ελλάδα",
      "lat": 40.6464861,
      "lng": 22.9647357
    }
  ]
}
```

## Additional Parameter:

In any request, you can add a `locale` parameter.

Example 3: `http://<IP:PORT>?lat=40.631165&lng=22.945489&max=5&locale=el`
