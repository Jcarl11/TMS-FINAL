#!/bin/bash
input="./sm-iot.txt"
while IFS= read -r line
do
  echo "$line"
curl -X POST -d "$line" \
'https://my-first-project-45061.firebaseio.com/cars/cars.json' 
done < $1

