## wakfu-shell

### usage
```clojure
clojure.core=> (sh/make :c.a.w.c.chat.console.command.VicinityContentCommand)
#object[com.ankamagames.wakfu.client.chat.console.command.VicinityContentCommand]
clojure.core=> (sh/call *1 :execute [nil, nil, (java.util.ArrayList. ["", "", ""])])
nil
```
