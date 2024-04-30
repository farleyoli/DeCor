# TODO
* Sanitize string in question when it has breaklines
* Add title to main window (should be easy)
* Create card directly, instead of having to press ctrl+A
* Deal with case when Anki is not connected (show status continually?)
* Add command to delete pdf files (and corresponding images [and decks?])
* Write Github README

* Release Version 1

* Add buttons to do stuff in UI (including to add pdf files)
* Separate .deck from images in image folder
* Do not save images to folder if user does not want (option to delete image files on request?)
* Do not use blank pages and just leave the 
* After first click when creating card, dynamically fill vertical bar on left
* Fix Anki not going to position of blue mark when opening answer
* Zoom into pages!
* Check if it is possible to have it work on Android
* Pages with two columns
* Add support for same card in two different locations
* Throttle change event

# DONE
* Don't do anything when we press cancel in the card creating dialog
* Make input dialog better
* Use additional thread to create images instead of blocking scroll
* Create images lazily 
* Make AnkiConnect Create the Model for DeCor
* Deal with case where user doesn't have any file in pdf folder (currently breaking for user)
* Bug when endPage < begPage (click below before above)
* Add support to an arbitrary number of pages natively
* Loading pdf... dialogue (harder than I though :-))
* Get folders right in Linux and Windows and Mac
* Deal with LaTeX bug in input (JSON escape character)
* Fix OS detection function (use substrings with win and mac)
