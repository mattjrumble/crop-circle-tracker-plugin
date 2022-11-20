# Crop Circle Tracker Plugin

A RuneLite plugin for crowdsourcing crop circle tracking. Nearby crop circles are detected and reported to a
backend server ([default server](https://github.com/mattjrumble/crop-circle-tracker-server)). The server combines
sightings to calculate the location of crop circles in different worlds. The plugin gets these locations from the
server and displays them in a panel so users can quick-hop to a world where a crop circle is present. Mainly useful
for area-locked/snowflake accounts or for getting the Farmer's Affinity speed boost in Puro-Puro.

### Scouting

The best way to scout crop circle locations is using the Wandering Impling in Zanaris. Just hop through worlds and use
the Wandering Impling each time, and the plugin will detect the crop circle and send that information to the backend
server. Multiple sightings on the same world help to narrow down the timings and make the plugin more accurate.
The default backend server resets sightings every Wednesday at 11:30AM UK time (to match the weekly game update -
which resets all the crop circles). So I wouldn't bother scouting on Wednesday morning.

### Improvements

See [this gist](https://gist.github.com/mattjrumble/757f46d00162d05f2b5748e045830721) for the original design of this
plugin and some research-y notes. The plugin is pretty much at "Server Implementation v2" currently. "Server
Implementation v3" has some features I'd like to add in the future. The main one is "Use sightings of empty crop
circles to narrow down timings" - it's annoying when you visit a high likelihood location and there's no crop circle
there, but the likelihood doesn't change to reflect that.
