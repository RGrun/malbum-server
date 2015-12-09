# malbum-server

## TODO:

* Add "Description" column to photos table
* Allow description to be entered during photo upload

* see internal TODS throughout project for now

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

First, you're going to need to download and install Foundation 6, and place it in the project's
`resources/public` folder as `malbum-foundation`. 

Then, you'll also need to install [Foundation 6][2]'s build tool and run
`foundation watch` to compile the scss before running the server.

Instructions on setting up the Postgres database will come soon.

[1]: https://github.com/technomancy/leiningen
[2]: http://foundation.zurb.com/sites/docs/installation.html#command-line-tool.html

## Running

To start a web server for the application, run:

    lein ring server

## License



