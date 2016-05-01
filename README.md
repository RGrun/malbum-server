# malbum-server

This is the server component to malbum. It's written in Clojure and uses
a RESTful interface to communicate with [an Android client](https://github.com/RGrun/malbum-android)
I wrote. The server and client communicate using JSON.

Malbum is a bit like a private Instagram. It organizes photos uploaded to it
into albums for each user account, and presents an administration interface for management.

You can learn more about malbum at the page concerning it on my personal website:
http://furu.guru/projects/malbum.

It should be up and running here: http://malbum.furu.guru.


## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.


## Running

To start a web server for the application, run:

    lein ring server


