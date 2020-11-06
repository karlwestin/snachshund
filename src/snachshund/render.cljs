(ns snachshund.render)

(def size-factor 20)

(defn create-div [{x :x y :y} className]
  (let [el (. js/document createElement "div")]
    (set! (.. el -className) (str "game-element" " " className))
    (set! (.. el -style -top) (str (* y size-factor) "px"))
    (set! (.. el -style -left) (str (* x size-factor) "px"))
    el))

(defn draw! [element coords className]
  (doseq [item coords]
    (let [el (create-div item className)]
      (. element appendChild el))))

(defn clear! [element]
  (doseq [child (. element -children)]
    (. child remove)))

(defn render! [element game-state]
  (clear! element)
  (draw! element (:snake game-state) "snake"))
