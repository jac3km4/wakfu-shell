(ns wakfu-shell.core
  (:require [clojure.main :as clj])
  (:require [clojure.java.io :as io])
  (:require [clj-cbor.core :as cbor])
  (:require [wakfu-shell.jvm :as jvm])
  (:gen-class)
  (:import (clojure.lang Symbol)
           (java.lang.reflect Method)
           (clojure.core Vec)
           (java.nio.file Paths)))

(def mappings
  (let [values (-> "classes.cbor" io/resource cbor/decode)
        keys (map #(-> % (nth 1) (nth 0)) values)]
    (zipmap keys values)))

(defn- find-class
  ([^Symbol class-sym]
   (let [native-name (jvm/native-name (name class-sym))
         class-shorthand "c/a/w/c"
         name (.replaceFirst native-name class-shorthand "com/ankamagames/wakfu/client")]
     (get mappings name))))

(defn- find-method
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
     (-> (ClassLoader/getSystemClassLoader) (.loadClass java-name) .newInstance))))

(defn call
  ([obj, ^Symbol method-sym, ^Vec args]
   (let [class (class obj)
         class-sym (-> class .getName symbol)
         [name, descriptor] (find-method class-sym method-sym)
         ^Method method (->> (.getMethods class)
                             seq
                             (filter #(= name (.getName %)))
                             (filter #(= descriptor (jvm/method-descriptor %)))
                             first)]
     (.invoke method obj (into-array Object args)))))

(defn reload []
  (clj/load-script "prelude.clj"))

(defn -main [& args]
  (doseq [file (-> (System/getProperty "user.dir")
                   (Paths/get (into-array String ["lib"]))
                   .toFile
                   file-seq)]
    (jvm/add-system-classpath (.toURL file)))

  (let [client (-> (ClassLoader/getSystemClassLoader) (.loadClass "com.ankamagames.wakfu.client.WakfuClient"))
        main (.getMethod client "main" (into-array Class [(class (make-array String 0))]))]
    (.invoke main nil (into-array Object [(into-array String [])])))
  (clj/repl :init #(do (require '[wakfu-shell.core :as sh])
                       (reload))))
