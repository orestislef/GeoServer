# GeoServer
Is an android app that runs a service
and if you browse phones IP:PORT with GET METHOD 
with lat, lng and max params it returns single address name

ex: http://10.0.2.2:8080/?lat=40.631165&lng=22.945489&max=5

returns 
{
  "address": [
    "π.μελλα, Τσιμισκή, Θεσσαλονίκη 546 22, Ελλάδα",
    "Τσιμισκή 79, Θεσσαλονίκη 546 22, Ελλάδα",
    "Ικτίνου 2, Θεσσαλονίκη 546 22, Ελλάδα",
    "Τσιμισκή 79-65, Θεσσαλονίκη 546 22, Ελλάδα",
    "Θεσσαλονίκη 546 22, Ελλάδα"
  ]
}