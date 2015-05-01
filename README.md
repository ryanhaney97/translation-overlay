# Translation Overlay

An overlay for translating (or "subtitling") games. Currently in-dev, and works for the first and second games in the Lenen Project: Evanescent Existence and Earthen Miraculous Sword. Should be able to support other translations as well now.

## Usage

To use the program, run this and the game in question (as of now, Lenen Project: Evanescent Existence or Lenen Project: Earthen Miraculous Sword). Select the game you are playing with the drop-down menu upon startup. On windows, it should be good to go immediately. On mac the game will become unfocused when the game starts, so use mission control (swipe up with three fingers) to put the window back into focus, which should make it work so long as you don't click on the game window.

This program can't pick up what goes on in game, so the user needs to hit certain keys in order to control the overlay. By default, these keys are:

c key: shows/hides the dialogue box. Press this upon reaching a boss or another place with dialogue.

z key: displays the next line in the dialogue. Overwrites the normal dialogue advancing key, so that the game's dialogue moves as well as the overlay's dialogue. Only works when dialogue is visible.

a key: moves dialogue back one stage without affecting the game. Only works when dialogue is visible.

s key: moves the dialogue forward one stage without affecting the game. Only works when dialogue is visible.

keys 1-7: skips dialogue to the respective stage, with 7 being the extra stage.

g key: skips dialogue to good ending. Dialogue proceeds to bad ending by default, so use this if you get the good ending.

b key: skips dialogue to bad ending.

bracket keys ([ or ]): Used to swap between different characters' dialogue (Yabusame and Tsubakura by default). Starts with Yabusame's by default.

Currently only supports windowed mode. Fullscreen mode will also hopefully be supported in time. Windows should not be moved from their default positions, as the overlay does not move yet.

I have many plans for this overlay, which will be implemented in time!

## Editting/Adding Translations

If you want to edit the overlay, you simply need to edit the files in the "translations" folder included with the download. Feel free to use the existing translation files as a reference.

## Downloads

[Version 0.3.1](http://bit.ly/1zkKK5B "Version 0.3.1 Download")

[Version 0.3.0](http://bit.ly/1OKLd86 "Version 0.3.0 Download")

[Version 0.2.1](http://bit.ly/1OA4IAc "Version 0.2.1 Download")

[Version 0.2.0](http://bit.ly/1zZqG3z "Version 0.2.0 Download")

[Version 0.1.2](http://bit.ly/1DrQ7MK "Version 0.1.2 Download")

[Version 0.1.1](http://bit.ly/1d9BY0x "Version 0.1.1 Download")

## Changelog

Version 0.3.1 - Fixed a very annoying bug involving the game failing to read files. Also involved moving the translation data outside of the .jar file, so I went ahead and knocked out 2 things at once.

Version 0.3.0 - Added support for multiple games! Use the drop-down menu on startup in order to select the game of choice. Fully supports Evanescent Existance, while having Yabusame's main scenario in Earthen Miraculous Sword translated as well. More translations for Earthen Miraculous Sword will be added after those respective files have been translated on the wiki.

Version 0.2.1 - Fixed the darn bracket keys. They should switch scenarios properly now. Also changed the color of the text again.

Version 0.2.0 - Lots of things were done in this one. I added the rest of Evanescent Existence's Dialogue to the overlay, as well as several new keybindings to allow for this (so re-read the usage section!). Changed the color of the dialogue a bit to hopefully make it a little bit easier to see (this is temporary, I have a better idea in mind), as well as changed it so that the overlay activates upon pressing the key, rather than releasing it. Also fixed bugs.

Version 0.1.2 - Fixed the :hide keyword showing up when backing up through text with the "a" key. Also updated the version name in code and added some settings to project.clj.

Version 0.1.1 - Fixed an annoying bug with files not being found.

Version 0.1.0 - Very large update. I've added much support to the game, and added .edn files to the resources folder so that one without coding knowledge can somewhat translate (hopefully). There are still many things to do, but for now it works with Yabusame's Scenario in Evanescent Existance. Many things were added, such as keys for the dialogue box, as well as support for .edn files, which can be used to edit the translation, dialogue size and position, amoung other things. Still somewhat limited, but is presentable for now.

Version 0.0.1 - Initial Commit. Currently displays the word "On!" when the 'c' key is pressed. Should even work when window is not in focus, which technically should be all the time. Using "jnativehook" for handling unfocused key presses.

## Todo

(Not necessarily in any order)

- Translate the other Lenen Project games. (Awaiting Translations)
- Add a black outline around labels to help visibility.
- Add labels to handle other GUI elements, such as UI items and the pause menu.
- Add fullscreen support.
- Make a separate application (or incorporate into this app as a special "mode"), that will allow a graphical way of positioning things such as dialogue boxes, the window location and size, etc.
- Squish bugs.

## License

Copyright © 2015 Ryan Haney (yoshiquest)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
