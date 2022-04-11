# Fingerpaint

This started out as a basic Android fingerpainting example (hence the name) so I could get a handle on how to handle drawing for part of a larger project I'm working on.  As I added more features, I decided to see how many of those features I could cram onto a single UI-Free screen with no menus by using gestures.  The end goal is to have a fully featured drawing app with a minimalist interface.  In the meantime, I've got a debug menu that I add features to first.

This is a proof of concept, and very much a work in progress, so pardon the dust!  I'm also new to GitHub, but I'll try to keep this updated as I add more features.

CURRENT MENU-FREE FEATURES:
  - Pinch to change cursor size
  - Tap the back button 11 times to bring up a debug menu (see those features below)
  - Splash Screen to show off my sweet logo! (Yeah, I know they're annoying... I'll get rid of it eventually)

CURRENT DEBUG MENU FEATURES:
  - RGBA Color Slider
  - New File
  - Layer Menu
    - Add Layer
    - Remove Layer
    - Move Layer Up/Down
  - Eraser Mode (Uses the alpha from the color slider for strength)
  - Save
  - Kid Mode (close the debug menu.  Tapping the back button a bunch again will also work)

PLANNED FEATURES:
  - Migrating debug menu features over (Obviously)
  - Color Wheel to replace sliders outside of debug mode
  - Cursor size/color/mode indicator
  - Adding at least one better-looking brush

PURPOSEFULLY OMITTED FEATURES:
  - Undo: It would be fairly easy to implement via either auto-saving or hidden layers, but I like the idea of a "No looking back" drawing app.
