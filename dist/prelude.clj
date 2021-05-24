
(defn send-message [^String contents]
  (let [message (sh/make :c.a.w.c.chat.console.command.VicinityContentCommand)]
    (sh/call message :execute [nil, nil, (java.util.ArrayList. [contents, contents, contents])])))
