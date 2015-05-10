# What ? 

Cities are designed for cars.  We even use maps drawn for cars. 
In cities like Paris, they are, however a minor means of transportation : more than 60% of travel is by walk, and more than 25% by public transport.
http://www.paris.fr/pratique/deplacements-voirie/dossier/bilan-des-deplacements-a-paris/le-bilan-des-deplacements-a-paris-en-2013/rub_7096_dossier_103374_port_16333_sheet_25892

This is what a street in Paris looks like on maps :



[image]

The road isn't so tiny, nor does it span the whole street width. 
What's missing here is the pavement width. 

# How it works 

# Running this project 

Install mongodb 3 : http://docs.mongodb.org/manual/installation/
For ubuntu :
```
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
echo "deb http://repo.mongodb.org/apt/ubuntu "$(lsb_release -sc)"/mongodb-org/3.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.0.list
sudo apt-get update
sudo apt-get install -y mongodb-org
```




Download the 'trottoirs' (pavements) geojson features, and transform it for import. 

```
curl 'http://parisdata.opendatasoft.com/explore/dataset/trottoirs_des_rues_de_paris/download/?format=geojson&timezone=Europe/Berlin' > trottoirs_des_rues_de_paris.geojson
sed 's/{"type":"FeatureCollection","features"://g' trottoirs_des_rues_de_paris.geojson > tmpjson
sed '$s/.$//' tmpjson > mongoimport.json
```

BUT the Paris trottoirs geojson file has a weird offset. Correct that :

```
node d.js
mongoimport --db agreable --collection t --file d.ok.json --jsonArray
```


We should repeat the operation for the 'Volumes Batis' (buildings) [file](http://parisdata.opendatasoft.com/explore/dataset/volumesbatisparis2011/download/?format=geojson&timezone=Europe/Berlin). **Unfortunately**, some of them can't be indexed by mongoDB (malformed geojson polygons).
Download and import instead an export of the buildings filtered (~50 of them are missing).

```
curl https://copy.com/MaFNvfd7SoLTNEHn?download=1 > v-correct.tar.bz2
tar -jxvf v-correct.tar.bz2
mongoimport --db agreable --collection v --file v-correct.json 
```

Finally, create an index for each collection :

```
mongo #enters mongo shell
use agreable
db.v.createIndex( { geometry : "2dsphere" } )
db.t.createIndex( { geometry : "2dsphere" } )
```





[Archive] failed attempt with topojson :

```
topojson -o t.topo.json trottoirs_des_rues_de_paris.geojson -q 1e6
# Change the topojson translation property, then back to geojson
tail -c 200 t.topo.json
sed "s/2.226665275505943/2.236665275505943/g" t.topo.json > t2.topo.json
topojson-geojson t2.topo.json

sed 's/{"type":"FeatureCollection","features"://g' trottoirs_des_rues_de_paris.json > tmpjson
sed '$s/.$//' tmpjson > mongoimport.topo.json

mongoimport --db agreable --collection t2 --file mongoimport.topo.json --jsonArray

# Mongo fails at indexing these topojson to geojson features...
```
