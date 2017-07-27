(ns oss-world.gui-test
  (:require  [clojure.test :refer [deftest is] :as t]
             [oss-world.gui :as g])
  (:import (javafx.scene.image Image)))


(let [orig-fav ["aa" "bb"]
      orig-act ["cc" "dd"]]
  (deftest remove-favorite
    (is (= [:favorites-list ["aa"]
            :activity-list ["bb" "cc" "dd"]]
           (g/remove-favorite "bb" orig-fav orig-act)))
    (is (= [:favorites-list orig-fav
            :activity-list orig-act]
           (g/remove-favorite "" orig-fav orig-act)))
    (is (= [:favorites-list orig-fav
            :activity-list orig-act]
           (g/remove-favorite "nan" orig-fav orig-act))))

  (deftest add-favorite
    (is (= [:favorites-list ["aa" "bb" "dd"]
            :activity-list ["cc"]]
           (g/add-favorite "dd" orig-fav orig-act)))
    (is (= [:favorites-list orig-fav
            :activity-list orig-act]
           (g/add-favorite "" orig-fav orig-act)))))

(let [awesome [:awesome (Image. "https://www.clker.com/cliparts/f/8/5/8/1314063746963744001red%20square-md.png") "awesome" #(println 3)]
      cool [:cool (Image. "http://www.freeiconspng.com/uploads/square-frame-png-32.png") "cool" #(println 2)]
      fantastic [:fantastic (Image. "http://www.freeiconspng.com/uploads/square-frame-png-32.png") "fantastic" #(println 1)]
      favorites [awesome cool fantastic]]
  (deftest get-favorites-for-names
    (is (= [cool fantastic]
           (g/get-favorites-for-names ["cool" "fantastic"] favorites)))
    (is (= [cool]
           (g/get-favorites-for-names ["nan" "cool"] favorites)))))
