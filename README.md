## wakfu-repl

### usage
```clojure
clojure.core=> (import 'java.util.ArrayList)
java.util.ArrayList
clojure.core=> (re/make :c.a.w.c.chat.console.command.VicinityContentCommand)
#object[com.ankamagames.wakfu.client.chat.console.command.VicinityContentCommand]
clojure.core=> (re/call *1 :execute [nil, nil, (ArrayList. ["", "", ""])])
nil
```
