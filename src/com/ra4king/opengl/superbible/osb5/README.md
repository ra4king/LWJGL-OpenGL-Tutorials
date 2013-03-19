# README #

## A note on porting the OSB code ##

The code quality for the OpenGL Superbible 5th edition is, in a word, _terrible_.  It uses Hungarian notation.  It
shoves essential functionality into "stock shader" and geometry batching abstractions which defeat the entire purpose
of teaching OpenGL as an API (It's not the "3D Graphics SuperBible" after all).  Even the indentation style sucks.  All
in all, it's a great leap backward from the imperfect but still excellent 4th edition. Therefore, these ports are more
of an _adaptation_ of the OSB examples, but not a perfect replica of them.  The shaders will be kept as similar as
possible to the originals (sans Hungarian notation), but not tucked away in a library.  Geometry batching won't be
dropped in from word go, but brought in gradually.
