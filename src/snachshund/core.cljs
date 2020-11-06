(ns snachshund.core
  (:require [snachshund.render :as render]))
;; clj --main cljs.main --compile snachshund.core --repl
;; (require '[snachshund.core :as snack] :reload)
(def board-size 20)

(def movements
  {:left {:x -1 :y 0}
   :right {:x 1 :y 0}
   :up {:x 0 :y -1}
   :down{:x 0 :y 1}})

(def legit-moves
  {:left #{:up :down :left}
   :right #{:up :down :right}
   :up #{:up :left :right}
   :down #{:down :left :right}})

(defn legit-move? [current-direction next-direction]
  (not (nil? (next-direction (current-direction legit-moves)))))

(defn step [position direction]
  (mod (+ position direction) board-size))

(defn vector-step [position direction]
  {:x (step (:x position) (:x direction))
   :y (step (:y position) (:y direction))})

(defn move-snake [snake direction]
  (let [next-step (vector-step (last snake) direction)]
    (conj (subvec snake 1) next-step)))

(def new-game
  {:snake [{:x 9 :y 11} {:x 9 :y 10} {:x 9 :y 9}]
   :direction :up
   :interval 1000
   :board-size board-size})

(defn calculate-next-state [state step? new-direction]
  (let [old-snake (:snake state)
        direction (or new-direction (:direction state))
        new-snake (move-snake old-snake (direction movements))
        done (= 0 (:y (last new-snake)))]
    (merge
      state
      {:snake new-snake
       :direction direction
       :done done})))

(defn key-press [state new-direction]
  (if (not (legit-move? (:direction state) new-direction))
    nil
    (calculate-next-state state false new-direction)))


(def state
  (atom
    {:game new-game
     :element (. js/document getElementById "game")}))

(defn game-loop
  ([]
    (game-loop nil))
  ([direction]
    (let [element (:element @state)
          prev-state (:game @state)
          ;; direction => user event, don't "step" the world
          step (not direction)
          new-state (calculate-next-state prev-state step direction)
          interval (:interval new-state)
          done? false
          _ (render/clear! element)
          _ (render/render! element new-state)
          _ (swap! state assoc :game new-state)]
      (if-not (or done? (not step))
        (js/setTimeout game-loop interval)))))

(def key-directions
  {:37 :left
   :38 :up
   :39 :right
   :40 :down})

(defn keypress [event]
  (let [new-direction ((keyword (str event.keyCode)) key-directions)
        prev-state (:game @state)]
    (if (and
          new-direction
          (legit-move? (:direction prev-state) new-direction))
      (game-loop new-direction))))

(defn main! []
  (println "running the app")
  (. js/document addEventListener "keydown" keypress)
  (game-loop))
