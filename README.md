# wakfu-shell
Clojure-based script runtime for Wakfu.

## usage
You need to unpack the shell archive in the main game directory and then run the startup script from there (`wakfu-shell.bat`).

The shell loads the `prelude.clj` file on startup. It contains some basic utility functions and can be extended to fit your needs. You can even reload this file while the shell is running via `(sh/reload)`.

From there you can try running some commands and see what happens!
```clojure
; open the game debug bar
clojure.core=> (prelude/toggle-debug-bar)
nil
; open the mix debugger
clojure.core=> (prelude/toggle-mix-debugger)
nil
; open the render tree debug view
clojure.core=> (prelude/toggle-render-tree-debug)
nil
; send a message in the chat
clojure.core=> (prelude/send-message :body "hello")
nil
; link an item link in the chat
clojure.core=> (prelude/send-item-link :id 28675)
nil
```

## deobfuscated functions
This shell relies on a deobfuscator to make it possible to interact with obfuscated code as if it was not obfuscated. You can make use of it through several functions in the shell (sh) namespace.
These functions are:
- `sh/make` - construct a class by name
- `sh/call` - call a method on an instance of an object
- `sh/call-static` - call a static method
- `sh/get-field` - get a field from instance of an object
- `sh/get-field-static` - get a static field

Example usage:
```clojure
; print the game version
clojure.core=> (sh/call-static :com.ankamagames.wakfu.client.WakfuClientVersion :main [(into-array String [])])
; create a vicinity message object
clojure.core=> (sh/make :com.ankamagames.wakfu.client.chat.console.command.VicinityContentCommand)
#object[com.ankamagames.wakfu.client.chat.console.command.VicinityContentCommand]
; call the execute method to send it
clojure.core=> (sh/call *1 :execute [nil, nil, (java.util.ArrayList. ["test", "test", "test"])])
nil
```

## available debug menus
&nbsp;
![Debug menus](https://i.imgur.com/j6nQOba.png)
