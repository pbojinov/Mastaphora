# Mastaphora Web Crawler

## About

A web crawler for finding the size of chunks of the web.

Mastaphora will find the size of a site and continue to all external a hrefs (based on link depth provided).

The size of the site is calculated by adding up the file size of html, css, js, and images.

## Compiling

    javac -cp soup.jar Mastaphora.java
    
## Usage

    java -cp .:jsoup.jar Mastaphora http://google.com 0
    
    arg[0] = site, the root site to start your crawl on
    arg[1] = linkDepth, the depth to crawl from the root. passing in 0 will crawl the current site. 
    
## Example

    java -cp .:jsoup.jar Mastaphora http://google.com 1
    
    Mastaphora will find the size of Google and all external URLs on Google.
    
## Heads Up

A link depth of greater than 2 on most sites will result in a huge amount of content to crawl as the amount of sites grows exponentially.

Eg. It took me 45+ minutes to crawl Facebook with a link depth of 4.

###

## Author

@pbojinov

## License

The MIT Licnese, do as you please :) 
