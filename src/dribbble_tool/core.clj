(ns dribbble-tool.core
  (:gen-class)
  (:require [muse.core :refer [run!! fmap traverse DataSource]]
            [org.httpkit.client :as http]
            [clojure.core.async :refer [chan put!]]
            [clojure.data.json :as json]))

(def verbose (atom true))
(defmacro my-println
  [expression]
  (list 'if @verbose
        (list 'println expression)
        nil))

(def access-token "Bearer fdfa088e67dad3d581567dbbcd60dbc943fb416bd2961547ff226252db8ded3a")
(def base-url-api "https://api.dribbble.com/v1")
(def test-user-id "KendrickKidd")
(def options {:headers {"Authorization" access-token}})


(defn async-get [url desc]
  (let [c (chan 1)]
    (my-println (str "Start " desc))
    (http/get url options (fn [{:keys [status headers body error]}]
                            (if error
                              (put! c error)
                              (do
                                (my-println (str "Done with " desc))
                                (put! c (json/read-str body :key-fn keyword)))
                              )))
    c))

(defn get-followers [id]
  (async-get (str base-url-api "/users/" id "/followers") (str "getting followers for " id)))
(defn get-shots [id]
  (async-get (str base-url-api "/users/" id "/shots") (str "getting shots for " id)))
(defn get-likes [id]
  (async-get (str base-url-api "/shots/" id "/likes") (str "getting likes for " id)))

(defrecord FollowersOf [id]
  DataSource
  (fetch [_] (get-followers id)))

(defrecord ShotsOf [id]
  DataSource
  (fetch [_] (get-shots id)))

(defrecord LikesOf [id]
  DataSource
  (fetch [_] (get-likes id)))

; Due to no rate limit handlind there are sometimes nil values in x.
(defn get-follower-ids [x]
  (filterv some? (map #(get-in % [:follower :id]) x)))

(defn get-shot-ids [x]
  (filterv some? (map #(get-in % [:id]) x)))

(defn get-liker-ids [x]
  (filterv some? (map #(get-in % [:user :id]) x)))

(defn likers-of-shots-of-followers [id]
  (->> (FollowersOf. id)
       (fmap get-follower-ids)
       (traverse #(ShotsOf. %))
       (fmap (comp vec flatten))
       (fmap get-shot-ids)
       (traverse #(LikesOf. %))
       (fmap (comp vec flatten))
       ))

(defn get-top-likers [x n]
  (take n
        (sort-by val >
                 (frequencies x))))
(defn get-top10-likers [x]
  (get-top-likers x 10))

(defn -main [& args]
  (let [username-or-id (first args)]
    (if (nil? username-or-id)
      (println "Please specify username or id.")
      (->> (likers-of-shots-of-followers username-or-id)
           run!!
           get-liker-ids
           get-top10-likers
           println))))
