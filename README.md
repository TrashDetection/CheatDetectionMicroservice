# Cheat Detection Microservice

Minecraft server-side anti-cheat as SaaS (Software as a Service). Production ready and has server thousands of automated bans with almost no false bans. This repository is provided as a reference for developers. Setting this up in production requires some understadning of how the system works internally and has some hardcored values.

## Design (in short)

The microservice itself does all the processing related to determining whatever the player is flagged and when to serve punishments. PostgreSQL is used to store players who have been flagged. The microservice receives player packets (raw bytes) from the BungeeCord plugin which it processes and stores.

### BungeeCord
BungeeCord is used for caching raw bytes in the network layer for about 15s, then they are compressed and sent over the network to the microservice. Players are identified by mapping UUID to internal ID which is unique for every proxy. The microservice is unable to determine players UUID.

### Players & Evaluation
Everytime player connects to the server they get unique session ID which is used to determine the current state of the game for particular player. When the microservice receives player data it stores (to disk) and processes it. Relevant data, like players gamemode, is kept in memory until the player disconnects.

### Packets
With the help of Mixins its more than easy to use vanilla Minecraft classes to help serializing and interacting with packets from multiple different versions. Minecraft clients are added inside the "clients" folder with all of their libraries. The microservice then loads these JARs and injects to them by using special hook mod that loads up the Mixin and fully loads the client in headless mode.

The master branch requires a little bit manual setup to get everything to load up nicely.

The 1.13 branch is cabable of loading Minecraft launcher data, version.json (from clients/versions) and to load and download all the required libraries (to clients/libraries) by itself and requires just adding the versions you want to load.

### Ban Waves
Automatic bans are handed out in groups, called as a ban wave. Normal ban wave is executed every minute which bans the most blatant cheaters immediately. There are few special ban waves that are executed more rarely to target people who are trying to hide that they are cheating.