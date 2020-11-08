# Snachshund

> Snake in clojurescript. A dachshund eating snacks.

Play it here [karlwestin.github.io/snachshund/](https://karlwestin.github.io/snachshund/)

### What is this?

I was just seeing if i could implement snake like a vector of maps `[{:x 10 :y 11} {:x 11 :y 11}]`
It seems like it worked out quite nice

### How to run

No dependencies.

I used [shadow-cljs](http://shadow-cljs.org/) for this.

```
# install
npm i -g shadow-cljs

# watch
shadow-cljs watch app

# build
shadow-cljs compile app

# serve with Python 3 SimpleHTTPServer
cd docs && python -m http.server
```
