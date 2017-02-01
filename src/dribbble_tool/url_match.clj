(ns dribbble-tool.url-match
  (:gen-class))

(defn url-parts [url]
  (let [[_ scheme host path query] (re-matches #"([^:]+)://([^/]+)/?([^\?]+)?\??(.+)" url)]
    {:scheme scheme
     :host host
     :path path
     :queryparam query}))

(defn pattern-parts [pattern]
  (map #(.trim %) (.split pattern ";")))

(defn matcher-from-pattern-part [pattern-part]
  (let [[part-key part-value] (map first (re-seq #"([^\(\)]+)" pattern-part))
         binds (map second (re-seq #"\?([^/]+)" part-value))
         regex (.replaceAll part-value "\\?([^/]+)" "([^&]+)")]
     {:part-key part-key :binds binds :regex regex}))

(defn get-values [url pattern-part]
  (let [urlparts (url-parts url)
        {:keys [part-key binds regex]} (matcher-from-pattern-part pattern-part)
        url-part ((keyword part-key) urlparts)
        [match & groups] (re-find (re-pattern regex) url-part)
        bind-value-map (zipmap (map keyword binds) groups)]
    (if-not (nil? match) bind-value-map)))

(defn combine [mapped]
  (if-not (some nil? mapped)
    (apply concat mapped)))

(defn reducer [combined]
  (if-not (nil? combined)
    (remove empty? combined)))

(defn map-reduce [mapper reducer args-seq]
  (->> (map mapper args-seq)
       (combine)
       (reducer)))

(defprotocol Recognize
  (recognize [pattertn url]))

(defrecord Pattern [pattern]
  Recognize
  (recognize [this url]
    (map-reduce #(get-values url %) reducer (pattern-parts (:pattern this)))))
