(ns wakfu-repl.core
  (:require [clojure.main :as clj])
  (:require [clojure.java.io :as io])
  (:require [clj-cbor.core :as cbor])
  (:gen-class)
  (:import (java.net URLClassLoader URL)
           (clojure.lang Symbol)
           (wakfu_repl Utils)
           (java.lang.reflect Method)
           (clojure.core Vec)
           (java.nio.file Paths)))

(defn add-system-classpath
  [^URL url]
  (let [field (aget (.getDeclaredFields URLClassLoader) 0)]
    (.setAccessible field true)
    (let [ucp (.get field (ClassLoader/getSystemClassLoader))]
      (.addURL ucp url))))

(def mappings
  (let [values (-> "classes.cbor" io/resource cbor/decode)
        keys (map #(-> % (nth 1) (nth 0)) values)]
    (zipmap keys values)))

(def class-shorthand "c.a.w.c")

(defn find-class
  ([^Symbol class-sym]
   (let [escaped (.replace (.replaceFirst (name class-sym) class-shorthand "com.ankamagames.wakfu.client") "." "/")]
     (get mappings escaped))))

(defn find-method
  ([^Symbol class-sym ^Symbol method-sym]
   (let [[[_, _, obfuscated-methods], [_, _, methods]] (find-class class-sym)
         [i] (->> (map-indexed vector methods)
                  (filter #(= (name method-sym) (-> % second (nth 0))))
                  first)]
     (nth obfuscated-methods i))))

(defn make
  ([^Symbol class-sym]
   (let [[[obfuscated-name]] (find-class class-sym)
         java-name (.replace obfuscated-name "/" ".")]
     (-> (ClassLoader/getSystemClassLoader) (.loadClass java-name) (.newInstance)))))

(defn call
  ([obj, ^Symbol method-sym, ^Vec args]
   (let [class (class obj)
         class-sym (-> class .getName symbol)
         [name, descriptor] (find-method class-sym method-sym)
         ^Method method (->> (.getMethods class)
                             seq
                             (filter #(= name (.getName %)))
                             (filter #(= descriptor (Utils/getMethodDescriptor %)))
                             first)]
     (.invoke method obj (into-array Object args)))))

(defn -main [& args]
  (doseq [file (-> (System/getProperty "user.dir")
                   (Paths/get (into-array String ["lib"]))
                   .toFile
                   file-seq)]
    (add-system-classpath (.toURL file)))

  (let [client (-> (ClassLoader/getSystemClassLoader) (.loadClass "com.ankamagames.wakfu.client.WakfuClient"))
        main (.getMethod client "main" (into-array Class [(class (make-array String 0))]))]
    (.invoke main nil (into-array Object [(into-array String [])])))
  (clj/repl :init #(require '[wakfu-repl.core :as re])))
