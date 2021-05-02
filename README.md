# sfw2jpg
This project provides tools to convert Seattle FilmWorks (SFW) image formats to JPEG.

This is essentially a kotlin port of some old DOS freeware code I found from Bengt Cyren.

## Build Instructions
The hardest part of building anything is to make sure your development environment
is able to build.  I recommend for beginners to just install IntelliJ and open this 
project from there.

If, however, you have everything needed to build already set up, you can simply call
`./gradlew jar` and this will produce the output in build/libs.

## Running
from the commandline, you simply
`java -jar sfw2jpg.jar <sfw filenames to convert>`

The converted files will append ".jpg" to the end.  Therefore, abc.sfw will produce abc.sfw.jpg

### Examples
#### Show help
    java -jar sfw.jar -h

#### Test a file to see if it is valid
    java -jar sfw2jpg.jar -t 29196_17.sfw

#### Convert a file
    java -jar sfw2jpg.jar 29196_17.sfw
    or
    java -jar sfw2jpg.jar -c 29196_17.sfw

#### Convert a batch of files
    java -jar sfw2jpg.jar "*.sfw"

## Notes
* Note that SFW files are flipped both horizontal and vertical.  This convertor doesn't auto-flip
images as there are plenty of better tools out there for that.  I use ImageMagick `mogrify` tool 
to flip, flop, and rotate as needed.
  
* Note that wildcards are supported, but linux will often auto-resolve to the first
filename that matches.  To use wildcards as input you should quote the wildcard expression.