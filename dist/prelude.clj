(ns prelude
  (:require [wakfu-shell.core :as sh]))

; removes game log spam
(defn disable-loggers []
  (doseq [appender (enumeration-seq (.getAllAppenders (org.apache.log4j.Logger/getRootLogger)))]
    (when (instance? org.apache.log4j.ConsoleAppender appender)
      (.removeAppender (org.apache.log4j.Logger/getRootLogger) appender))))

(defn get-translation [^Integer cat, ^Integer id]
  (let [translator (sh/call-static :com.ankama.wakfu.utils.translator.Translator :getInstance [])]
    (.a translator cat id (into-array Object []))))

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

(defn send-message [& {:keys [body chat]}]
  (let [message (case chat
                  :guild (com.ankamagames.wakfu.client.chat.console.command.GuildMessageCommand.)
                  :team (com.ankamagames.wakfu.client.chat.console.command.TeamMessageCommand.)
                  :party (com.ankamagames.wakfu.client.chat.console.command.PartyPrivateMessageCommand.)
                  (com.ankamagames.wakfu.client.chat.console.command.VicinityContentCommand.))]
    (.a message nil, nil (java.util.ArrayList. [body, body, body]))))

(defn send-item-link [& {:keys [id name chat]}]
  (let [actual-name (if (nil? name) (get-translation 15 id) name)
        body (str "<u id=\"item_[0]" id "\">" actual-name "</u>")]
    (send-message :body body :chat chat)))

(disable-loggers)
