# console_downloader

###build:
    sbt assembly

###run:
    java -jar console-downloader-$BUILD_DATE.jar -n 5 -l 2000k -f links.txt -o current

For example BUILD_DATE=20150301
