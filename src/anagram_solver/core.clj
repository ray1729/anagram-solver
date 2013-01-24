(ns anagram-solver.core
  ^{:author "Ray Miller"
    :doc "A simple anagram solver, written to demonstrate some elements of functional programming"}
  (:gen-class)
  (:require [clojure.tools.cli :refer [cli]]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(defn word->key
  "Convert a dictionary word to a key to the anagram dictionary."
  [w]
  (str/join (sort (str/lower-case w))))

(defn build-anagram-dict
  "Build the anagram dictionary from a word list using loop/recur."
  [dict-path]
  (with-open [r (io/reader dict-path)]
    (loop [lines (line-seq r) dict {}]
      (if (empty? lines)
        dict
        (let [word (first lines)
              key-for-word (word->key word)
              current-anagrams (get dict key-for-word [])]
          (recur (rest lines) (assoc dict key-for-word (conj current-anagrams word))))))))

(defn build-anagram-dict-with-reduce
  "Build the anagram dictionary from a word list using reduce."
  [dict-path]
  (with-open [r (io/reader dict-path)]
    (reduce (fn [dict next-word]
              (let [key-for-word (word->key next-word)]
                (assoc dict key-for-word (conj (dict key-for-word) next-word))))
            {}
            (line-seq r))))

(defn build-anagram-solver
  "Build an angram solver from a word list. The anagram solver is a
  function that takes a word and returns a list of words from the
  anagram dictionary that are anagrams."
  [dict-path]
  (let [dict (build-anagram-dict dict-path)]
    (fn [word]
      (get dict (word->key word)))))

(defn build-anagram-solver-with-compose
  "Build an anagram solver from a word list. This is identical to
  build-anagram-solver, but rather than returning an anonymous
  function it uses comp to compose two existing functions"
  [dict-path]
  (let [dict (build-anagram-dict dict-path)]
    (comp dict word->key)))

(defn -main
  "Takes an optional --dict PATH and list of words, and prints
  anagrams of those words."
  [& args]
  (let [[opts args _] (cli args
                           ["-d" "--dict" "Specify the word list" :default "/usr/share/dict/words"])
        anagram-solver (build-anagram-solver-with-compose (opts :dict))]
    (doseq [w args]
      (println w "=>" (str/join ", " (anagram-solver w))))))