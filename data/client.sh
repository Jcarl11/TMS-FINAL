#!/bin/bash
input="./sm-iot.txt"
while IFS= read -r line
do
  echo "$line"
  curl -X POST \
-H "X-Parse-Application-Id: nGEoEK8re6EaVhzGCv7CMx4Jan1hELA1iu1wRxSd" \
-H "X-Parse-REST-API-Key: c2tBFbHdVBaJkbqwXgQle1fziX3wXKl02cdG1wSa" \
-H "Content-Type: application/json" \
-d "$line" \
'https://project.back4app.io/classes/RAWDATA'
done < $1

