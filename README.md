# spring-cloud

app import --uri http://bit.ly/stream-applications-rabbit-maven
stream create --name files-to-reservations2 --definition " file --file.consumer.mode=lines --file.directory=C:\\Softwares\\Spring-Cloud\\Input > :reservations " --deploy

curl -d{} http://localhost:8000/refresh
curl -d'{"reservationName":"Aravind"}' -H"content-type:application/json" http://localhost:9999/reservations

curl -X POST -H "Accept: application/json" -H "Authorization: Basic YWNtZTphY21lc2VjcmV0" -H "Cache-Control: no-cache" -H "Postman-Token: 442d3183-13b6-8de0-d1d7-ebf20d2a832b" -H "Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW" -F "password=password2" -F "username=user2" -F "grant_type=password" -F "scope=openid" -F "client_secret=acmesecret" -F "client_id=acme" "http://localhost:9191/uaa/oauth/token"
curl -H"authorization: bearer f6d3d55c-636a-4a60-a413-cf4ddeeff168" http://localhost:9999/reservations/names
