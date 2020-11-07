(ns snachshund.render)

(def size-factor 20)

(defn create-div [{x :x y :y :as item} className]
  (let [rendered-class (if (fn? className) (className item) className)
        el (. js/document createElement "div")]
    (set! (.. el -className) (str "game-element" " " rendered-class))
    (set! (.. el -style -top) (str (* y size-factor) "px"))
    (set! (.. el -style -left) (str (* x size-factor) "px"))
    el))

(defn draw-score! [element score]
  (let [el (create-div {:x 19 :y 18} "score")]
    (set! (. el -innerText) (str "Score: " score))
    (. element appendChild el)))

(defn draw! [element coords className]
  (doseq [item coords]
    (let [el (create-div item className)]
      (. element appendChild el))))

(defn clear! [element]
  (doseq [child (. element -children)]
    (. child remove)))

(defn render! [element game-state]
  (clear! element)
  (draw-score! element (:score game-state))
  (draw! element (:fruit game-state) #(str "fruit fruit-age-" (:age %)))
  (draw! element (:snake game-state) "snake")
  (if (:done game-state)
    (print "!!!GAME OVER!!!")))
