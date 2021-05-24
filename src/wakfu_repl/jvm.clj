(ns wakfu-repl.jvm
  (:import (java.net URLClassLoader URL)
           (java.lang.reflect Method)))

(defn add-system-classpath
  [^URL url]
  (let [field (aget (.getDeclaredFields URLClassLoader) 0)]
    (.setAccessible field true)
    (let [ucp (.get field (ClassLoader/getSystemClassLoader))]
      (.addURL ucp url))))

(defn native-name
  [^String class-sym]
  (.replace (name class-sym) "." "/"))

(defn class-descriptor
  [^Class class]
  (cond
    (.isArray class) (native-name (.getName class))
    (not (.isPrimitive class)) (str "L" (native-name (.getName class)) ";")
    :else (condp = class
            Byte/TYPE "B"
            Character/TYPE "C"
            Double/TYPE "D"
            Float/TYPE "F"
            Integer/TYPE "I"
            Long/TYPE "J"
            Short/TYPE "S"
            Boolean/TYPE "Z"
            Void/TYPE "V")))

(defn method-descriptor
  [^Method method]
  (let [args (->> (.getParameterTypes method) (map class-descriptor))]
    (str "(" (apply str args) ")" (class-descriptor (.getReturnType method)))))
