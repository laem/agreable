(ns trotte.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [clojure.data.json :as json]
            [selmer.parser :refer [render-file]]
            [prone.middleware :refer [wrap-exceptions]]
            [environ.core :refer [env]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all])
  (:import [com.mongodb MongoOptions ServerAddress]))

(def withinQuery { :geometry { $geoWithin
                             { "$geometry"
                               {
                                :type "Polygon"
                                :coordinates [[[2.3788082599639893 48.84600796705691]
                                               [2.3788082599639893 48.84652337993973]
                                               [2.3798999190330505 48.84652337993973]
                                               [2.3798999190330505 48.84600796705691]
                                               [2.3788082599639893 48.84600796705691]]]
                                } } } })

(def nearQuery { :geometry { $near
                             { "$geometry" {
                                :type "Point"
                                :coordinates [2.3788082599639893 48.84600796705691]
                                }
                               "$maxDistance" 400} } })



(defn objectId-reader [key value]
  (if (= key :_id)
    (str value)
    value))

(defn desTrottoirs []
  (let [conn (mg/connect) db (mg/get-db conn "agreable") coll "t"]
    (str (mc/count db coll))
    (let [res (mc/find-maps db coll nearQuery)]
    (json/write-str res :value-fn objectId-reader))
    ))

(defroutes routes
  (GET "/" [] (render-file "templates/index.html" {:dev (env :dev?)}))
  (GET "/sample" [] (desTrottoirs))
  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults routes site-defaults)]
    (if (env :dev?) (wrap-exceptions handler) handler)))
