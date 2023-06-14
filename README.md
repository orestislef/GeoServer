# GeoServer
Is an android app that runs a service like server
and if you make a request to phones IP:PORT with GET method

### In main app screen on top you have 2 boubles -> Green/Red light with text that tells you is everything is ok

### Next you have a button to START/STOP server

### IP:PORT can be found on mid of screen

### At the bottom you can see the last request


example 1: http://<IP:PORT>/?lat=40.631165&lng=22.945489&max=5

with params:  
> * lat, 
> * lng, 
> * max 

returns address names

>     {
>      "address": [
>          "π.μελλα, Τσιμισκή, Θεσσαλονίκη 546 22, Ελλάδα",
>          "Τσιμισκή 79, Θεσσαλονίκη 546 22, Ελλάδα",
>          "Ικτίνου 2, Θεσσαλονίκη 546 22, Ελλάδα",
>          "Τσιμισκή 79-65, Θεσσαλονίκη 546 22, Ελλάδα",
>          "Θεσσαλονίκη 546 22, Ελλάδα"
>       ]
>     }

------------------

example 2: http://<IP:PORT>/?address=karaikskaki%2027%20sykies&max=5

with params:  
> * address, 
> * max 

returns location with address names and lats lngs

>     {
>       "location": [
>         {
>            "address": "Καραϊσκάκη, Συκιές 566 26, Ελλάδα",
>            "lat": 40.6464861,
>            "lng": 22.9647357
>         }
>       ] 
>     }

## in eny request you can add a locale param 
example 3: http://<IP:PORT>?lat=40.631165&lng=22.945489&max=5&locale=el