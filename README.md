# RPCommands
You can make your own simple (and not so simple) commands that will make your chat more interesting.

Everything is explained in config.yml.
```yaml
# Here you can create your own commands.
# {player} and {message} are replaced with player's name and message is everything after /command
# For example, Iaiao ran /me is working, format is "* {player} {message}", then it should say "* Iaiao is working"
# {random} is replaced with a random number.
# if you have {random} more than once, the numbers will be different.
# To make them same, use {fixedrandom}
# If you use {random} and/or {fixedrandom}, you need to set the "random" parameter (example: /roll command)
# {finalMessage} in hover and click is the final displayed message (you can make use of this in /try)
me:
  type: "text"
  format: "* {player} {message}"
  hover: "&a{player} {message}. Do the same: /me" # hover text
  click: "/me clicked at {player}'s message" # if you click on the message, this command will be suggested
  range: 50
it:
  type: "text"
  format: "&7{message}"
  range: 150
try:
  type: "random_text"
  range: 50
  hover: "&a{finalMessage}. Do the same: /try"
  click: "/try click here"
  # Chances (%)
  chances:
    "{player} tried to {message} &a(success)": 49
    "{player} tried to {message} &c(failure)": 49
    "Iaiao was here": 2

# split: you can have multiple arguments to do something like "/todo bye * left the party" => "Iaiao said bye and left the party."
todo:
  type: "split"
  split-by: " \\* " # Regex. Test here -> https://regexr.com/
  format: "{player} said {message 1} and {message 2}."
  hover: "{player} said {message 1} and {message 2}. Do the same - /todo <message> * <action>"
  click: "/todo message * action"

roll:
  type: "text"
  format: "Roll: {random}"
  range: 50
  random:
    default-min: 0
    default-max: 100
    # If input-range is on, you can type /roll <min> <max> <message> instead of /roll <message>
    input-range: on
    error: "Usage: /roll or /roll <min> <max>"
    # players can't enter minimal number less than -1000 and maximal number bigger than 1000
    player-min: -1000
    player-max: 1000
    invalid-player-range: "&cYou can specify range only from -1000 to 1000"

use permissions: on # Do we need rpcommands.<command name> permission?
# note: you still rpcommands.<command name>.hear permission
```

This plugin should work on almost all Minecraft versions, I just haven't tested them all.
