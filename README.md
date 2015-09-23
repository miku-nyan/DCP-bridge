## DCP bridge

The local HTTP proxy server sends all HTTP (but not HTTPS) traffic through Chrome Data Compression Proxy server.  
Taking into account some features for [Overchan](https://github.com/miku-nyan/Overchan-Android) (imageboard client).

## Building desktop version

`ant -f build-desktop.xml`

Running  
`java -jar bin/dcpbridge.jar ...`

## Building android version

`ant -Dsdk.dir=/path/to/android-sdk debug` (sign with the debug key)

(or import the project into Eclipse)

## License

DCP bridge is licensed under the [GPLv3](http://www.gnu.org/licenses/gpl-3.0.txt).