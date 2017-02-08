# scala-4chan-random

Get a random attachment from given 4chan's board.

### Instructions
Provide the board's symbol as the only argument, eg. `sbt "run zzz"` (where `zzz` is a board of your choice's symbol, without trailing or leading slashes).
Unless the argument is provided, the tool will default to `/o/` board (for autos).

It is only your responsibility which boards you will fetch attachments from ;-)

### Building fat jar
Just run `sbt assembly`.
