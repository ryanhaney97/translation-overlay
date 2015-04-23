# Translation Overlay

An overlay for translating (or "subtitling") games. Currently in-dev, and works for the first game in the Lenen Project: Evanescent Existence.

## Usage

To use the program, run this and the game in question (as of now, Lenen Project: Evanescent Existence). On windows, it should be good to go immediately. On mac the game will become unfocused when the game starts, so use mission control (swipe up with three fingers) to put the window back into focus, which should make it work so long as you don't click on the game window.

This program can't pick up what goes on in game, so the user needs to hit certain keys in order to control the overlay. By default, these keys are:

c key: shows/hides the dialogue box. Press this upon reaching a boss or another place with dialogue.

z key: displays the next line in the dialogue. Overwrites the normal dialogue advancing key, so that the game's dialogue moves as well as the overlay's dialogue. Only works when dialogue is visible.

a key: moves dialogue back one stage without affecting the game. Only works when dialogue is visible.

s key: moves the dialogue forward one stage without affecting the game. Only works when dialogue is visible.

Currently only supports Yabusame's main scenario. Other scenarios will be supported in time. Also only supports windowed mode. Fullscreen mode will also hopefully be supported in time. Windows should not be moved from their default positions, as the overlay does not move yet.

I have many plans for this overlay, which will be implemented in time!

## Changelog

Version 0.1.0 - Very large update. I've added much support to the game, and added .edn files to the resources folder so that one without coding knowledge can somewhat translate (hopefully). There are still many things to do, but for now it works with Yabusame's Scenario in Evanescent Existance. Many things were added, such as keys for the dialogue box, as well as support for .edn files, which can be used to edit the translation, dialogue size and position, amoung other things. Still somewhat limited, but is presentable for now.

Version 0.0.1 - Initial Commit. Currently displays the word "On!" when the 'c' key is pressed. Should even work when window is not in focus, which technically should be all the time. Using "jnativehook" for handling unfocused key presses.

## License

Copyright © 2015 Ryan Haney (yoshiquest)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
