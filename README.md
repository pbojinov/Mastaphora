# Mastaphora Web Crawler

## About

A web crawler for finding the size of chunks of the web.

Mastaphora likes to go to a website, count the size of the current site and continue to all linking nodes (all external a hrefs).

The size of the site is calculated by adding up the file size of html, css, js, and images.

## Usage

    java -cp .:jsoup.jar Mastaphora http://google.com 0

## Compiling

    javac -cp soup.jar Mastaphora.java


## Author

@pbojinov

## License

The MIT Licnese, do as you please :) 
