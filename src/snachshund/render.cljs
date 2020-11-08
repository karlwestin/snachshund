(ns snachshund.render)

(def size-factor 20)

(defn create-div [{x :x y :y :as item} className]
  (let [rendered-class (if (fn? className) (className item) className)
        el (. js/document createElement "div")]
    (set! (.. el -className) (str "game-element" " " rendered-class))
    (set! (.. el -style -top) (str (* y size-factor) "px"))
    (set! (.. el -style -left) (str (* x size-factor) "px"))
    el))

(defn draw-content!
  ([element content className]
    (draw-content! element content className "div"))
  ([element content className tag]
    (let [rendered-element (. js/document createElement tag)
          nodes (if (string? content)
                 [(. js/document createTextNode content)]
                 content)]
      (doseq [node nodes]
        (. rendered-element appendChild node))
      (set! (. rendered-element -className) className)
      (if (nil? element)
        rendered-element
        (. element appendChild rendered-element)))))

(defn draw-seq! [element coords className]
  (doseq [item coords]
    (let [el (create-div item className)]
      (. element appendChild el))))

(defn clear! [element]
  ;; this was a nice example of how the DOM and JS leaks into CLJS:
  ;; element.children and element.childNodes are live nodelists
  ;; so removing elements while iterating over the nodelist
  ;; will "modify the sequence i'm iterating over", and
  ;; caused doseq to skip elements
  ;; converting the live nodelist to an array made it work
  (let [children (. js/Array from (. element -children))]
    (doseq [child children]
      (. child remove))))

(defn render! [element game-state]
  (clear! element)
  (draw-content! element (str "Score: " (:score game-state)) "score")
  (draw-seq! element (:fruit game-state) #(str "fruit fruit-age-" (:age %)))
  (draw-seq! element (:snake game-state) "snake")
  (when (:done game-state)
    (let [button (draw-content! nil "TRY AGAIN" "js-restart restart" "button")
          sign (draw-content! nil "!!!!GAME OVER!!!!" "game-over")]
      (draw-content! element [sign button] "game-over-sign"))))
