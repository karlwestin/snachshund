(ns snachshund.core
  (:require [snachshund.render :as render]))
;; clj --main cljs.main --compile snachshund.core --repl
;; (require '[snachshund.core :as snack] :reload)
(def board-size 20)
(def base-speed 500)

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

(defn grow-snake [new-snake old-snake eaten]
  (if (not eaten)
    new-snake
    (into [(first old-snake)] new-snake)))

(defn collide-item [hitter target]
  (and (= (:x hitter) (:x target))
       (= (:y hitter) (:y target))))

(defn collide [hitter targets]
  (first
    (filter #(collide-item hitter %) targets)))

(defn create-fruit [excludes]
  (let [fruit {:x (rand-int board-size) :y (rand-int board-size) :age 0}
        add? (< 18 (rand-int 20))
        available? (not (collide fruit excludes))]
    (if (and add? available?)
      fruit)))

(defn remove-fruit [fruit eaten]
  (filterv #(and (not (= eaten %)) (< (:age %) 10)) fruit))

(defn age-fruit [fruit]
  (mapv #(assoc % :age (inc (:age %))) fruit))

(defn manage-fruit [eaten old-fruit excludes step?]
  (let [filtered-fruit (remove-fruit old-fruit eaten)
        filtered-fruit (if step? (age-fruit filtered-fruit) filtered-fruit)
        add-fruit (create-fruit (concat excludes filtered-fruit))]
    (if add-fruit
      (conj filtered-fruit add-fruit)
      filtered-fruit)))

(defn get-score [point old-score]
  (if point
    (inc old-score)
    old-score))

(defn get-interval [score]
  (max (- base-speed (* (int (/ score 10)) 200)) 100))

(def new-game
  {:snake [{:x 9 :y 11} {:x 9 :y 10} {:x 9 :y 9}]
   :direction :up
   :interval base-speed
   :score 0
   :fruit [{:x 3 :y 4 :age 0}]
   :board-size board-size})

(defn calculate-next-state [state step? new-direction]
  (let [old-snake (:snake state)
        old-fruit (:fruit state)
        direction (or new-direction (:direction state))
        new-snake (move-snake old-snake (direction movements))
        eaten (collide (last new-snake) old-fruit)
        ;; this was a bit of a catch 22: can't know if we're eating
        ;; until the snake has moved, can't know if i need the keep
        ;; the first square until i know if we've eaten
        new-snake (grow-snake new-snake old-snake eaten)
        new-fruit (manage-fruit eaten old-fruit new-snake step?)
        score (get-score eaten (:score state))
        done? (or
                (:done state)
                (not (nil? (collide
                             (last new-snake)
                             (butlast new-snake)))))]
    (merge
      state
      {:snake (if done? old-snake new-snake)
       :direction direction
       :score score
       :interval (get-interval score)
       :fruit (if done? old-fruit new-fruit)
       :done done?})))

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
          done? (:done new-state)
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
          (not (:done prev-state))
          new-direction
          (legit-move? (:direction prev-state) new-direction))
      (game-loop new-direction))))

(defn main! []
  (println "running the app")
  (. js/document addEventListener "keydown" keypress)
  (game-loop))
