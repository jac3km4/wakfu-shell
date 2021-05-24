(ns wakfu-shell.core
  (:require [clojure.main :as clj])
  (:require [clojure.java.io :as io])
  (:require [clj-cbor.core :as cbor])
  (:require [wakfu-shell.jvm :as jvm])
  (:gen-class)
  (:import (clojure.lang Symbol)
           (java.lang.reflect Method Field)
           (clojure.core Vec)
           (java.nio.file Paths)))

(def classes (-> "classes.cbor" io/resource cbor/decode))

(def obfuscated-classes
  (zipmap (map #(-> % (nth 0) (nth 0)) classes) classes))

(def real-classes
  (zipmap (map #(-> % (nth 1) (nth 0)) classes) classes))

(defn- find-class
  ([^String class-name, source]
   (get source (jvm/native-name class-name))))

(defn- find-member
  ([^String class-name ^String member-name, ^Symbol kind]
   (let [index (condp = kind :field 1 :method 2)
         [obfuscated, unobfuscated] (find-class class-name real-classes)
         [i] (->> (nth unobfuscated index)
                  (map-indexed vector)
                  (filter #(= member-name (-> % second (nth 0))))
                  first)]
     (-> obfuscated (nth index) (nth i)))))

(defn- call-with
  ([obj, ^Class class, ^String class-name, ^String method-name, ^Vec args]
   (let [[name, descriptor] (find-member class-name method-name :method)
         ^Method method (->> (.getMethods class)
                             seq
                             (filter #(= name (.getName %)))
                             (filter #(= descriptor (jvm/method-descriptor %)))
                             first)]
     (.invoke method obj (into-array Object args)))))

(defn- get-with
  ([obj, ^Class class, ^String class-name, ^String field-name]
   (let [[name, descriptor] (find-member class-name field-name :field)
         ^Field field (->> (.getFields class)
                           seq
                           (filter #(= name (.getName %)))
                           (filter #(= descriptor (jvm/class-descriptor (.getType %))))
                           first)]
     (.get field obj))))

(defn make
  ([^Symbol class-sym]
   (let [[[obfuscated-name]] (find-class class-sym real-classes)
         java-name (.replace obfuscated-name "/" ".")]
     (-> (ClassLoader/getSystemClassLoader) (.loadClass java-name) .newInstance))))

(defn call
  ([obj, ^Symbol method-sym, ^Vec args]
   (let [class (class obj)
         [_, [real-name]] (find-class (.getName class) obfuscated-classes)]
     (call-with obj class real-name (name method-sym) args))))

(defn call-static
  ([^Symbol class-sym, ^Symbol method-sym, ^Vec args]
   (let [[[obfuscated-name]] (find-class class-sym real-classes)
         class (.loadClass (ClassLoader/getSystemClassLoader) obfuscated-name)]
     (call-with nil class (name class-sym) (name method-sym) args))))

(defn get-field
  ([obj, ^Symbol field-sym]
   (let [class (class obj)
         [_, [real-name]] (find-class (.getName class) obfuscated-classes)]
     (get-with obj class real-name (name field-sym)))))

(defn get-field-static
  ([^Symbol class-sym, ^Symbol field-sym]
   (let [[[obfuscated-name]] (find-class class-sym real-classes)
         class (.loadClass (ClassLoader/getSystemClassLoader) obfuscated-name)]
     (get-with nil class (name class-sym) (name field-sym)))))

(defn reload []
  (clj/load-script "prelude.clj"))

(defn -main [& args]
  (doseq [file (-> (System/getProperty "user.dir")
                   (Paths/get (into-array String ["lib"]))
                   .toFile
                   file-seq)]
    (jvm/add-system-classpath (.toURL file)))

  (let [client (.loadClass (ClassLoader/getSystemClassLoader) "com.ankamagames.wakfu.client.WakfuClient")
        main (.getMethod client "main" (into-array Class [(class (make-array String 0))]))]
    (.invoke main nil (into-array Object [(into-array String [])])))
  (clj/repl :init #(do (require '[wakfu-shell.core :as sh])
                       (reload))))
