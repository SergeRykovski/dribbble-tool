(ns dribbble-tool.url-match-test
  (:require [clojure.test :refer :all]
            [dribbble-tool.url-match :refer :all]))

(defn set= [& vectors] (apply = (map set vectors)))

(deftest test-url-match
  (testing "url-match"
    (let [twitter (dribbble_tool.url_match.Pattern. "host(twitter.com); path(?user/status/?id);")
          dribbble (dribbble_tool.url_match.Pattern. "host(dribbble.com); path(shots/?id); queryparam(offset=?offset);")
          dribbble2 (dribbble_tool.url_match.Pattern. "host(dribbble.com); path(shots/?id); queryparam(offset=?offset); queryparam(list=?type);")]

      (is (set= (recognize twitter "http://twitter.com/bradfitz/status/562360748727611392"))
                '([:user "bradfitz"] [:id 562360748727611392]))

      (is (set= (recognize dribbble "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users&offset=1")
                '([:id "1905065-Travel-Icons-pack"] [:offset "1"])))

      (is (set= (recognize dribbble "https://twitter.com/shots/1905065-Travel-Icons-pack?list=users&offset=1")
                nil))

      (is (set= (recognize dribbble "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users")
                nil))

      (is (set= (recognize dribbble2 "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users&offset=1")
                '([:id "1905065-Travel-Icons-pack"] [:type "users"] [:offset "1"]))))))
