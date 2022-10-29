# Crop Circle Tracker Plugin

A RuneLite plugin for crowdsourcing crop circle tracking. Nearby crop circles are detected and reported to a
backend server ([default server](https://github.com/mattjrumble/crop-circle-tracker-server)). The server combines
sightings to calculate the location of crop circles in different worlds. The plugin gets these locations from the
server and displays them in a panel so users can quick-hop to a world where a crop circle is present. Mainly useful
for area-locked/snowflake accounts.

### Scouting

The best way to scout crop circle locations is using the Wandering Impling in Zanaris. Just hop through worlds and use the Wandering Impling each time, and the plugin will detect the crop circle and send that information to the backend server. Multiple sightings on the same world help to narrow down the timings and make the plugin more accurate. The default backend server resets sightings every Wednesday at 11:30AM UK time (to match the weekly game update).
