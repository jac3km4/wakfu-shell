(ns prelude
  (:require [wakfu-shell.core :as sh]))

; removes game log spam
(defn disable-loggers []
  (doseq [appender (enumeration-seq (.getAllAppenders (org.apache.log4j.Logger/getRootLogger)))]
    (when (instance? org.apache.log4j.ConsoleAppender appender)
      (.removeAppender (org.apache.log4j.Logger/getRootLogger) appender))))

(defn print-game-version []
  (sh/call-static :com.ankamagames.wakfu.client.WakfuClientVersion :main [(into-array String [])]))

(defn toggle-render-tree-debug []
  (let [instance (sh/get-field-static :com.ankamagames.baseImpl.graphics.alea.rendertreee.RenderTreeDebug :INSTANCE)]
    (sh/call instance :initialize [])))

(defn toggle-debug-bar []
  (let [message (com.ankamagames.wakfu.client.console.command.debug.DebugBarCommand.)]
    (.a message nil nil (java.util.ArrayList. []))))

(defn toggle-mix-debugger []
  (let [message (com.ankamagames.wakfu.client.console.command.debug.MixDebuggerCommand.)]
    (.a message nil nil (java.util.ArrayList. []))))

(defn send-message [^String contents]
  (let [message (com.ankamagames.wakfu.client.chat.console.command.VicinityContentCommand.)]
    (.a message nil, nil (java.util.ArrayList. [contents, contents, contents]))))

(defn send-item-link [^String name, ^Integer id]
  (let [message (com.ankamagames.wakfu.client.chat.console.command.VicinityContentCommand.)
        contents (str "<u id=\"item_[0]" id "\">" name "</u>")]
    (.a message nil, nil (java.util.ArrayList. [contents, contents, contents]))))

(disable-loggers)
