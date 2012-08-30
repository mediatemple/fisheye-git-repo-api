# Developing 

## Getting started

First, install the [Atlassian Plugin SDK](https://developer.atlassian.com/display/DOCS/Set+up+the+Atlassian+Plugin+SDK+and+Build+a+Project).

Change directory into a clone of this repo and run Fisheye (available at http://<machinename>:3990/fecru):

    $ atlas-run

Or to run against a specific version ([list of available versions](https://maven.atlassian.com/content/repositories/atlassian-public/com/atlassian/crucible/atlassian-crucible/)) :


    $ atlas-run --version 2.7.13-20120517072828

## Packaging

Run the `atlas-package` command.  See [full documentation](https://developer.atlassian.com/display/DOCS/Packaging+and+Releasing+your+Plugin).

## Other available commands

* `atlas-debug` - same as atlas-run, but allows a debugger to attach at port 5005
* `atlas-cli` - after atlas-run or atlas-debug, opens a Maven command line window;'pi' reinstalls the plugin into the running Fisheye/Crucible instance
* `atlas-help` - prints description for all commands in the SDK

# Resources

Inspiration

* [FishEye plugin to synchronize Gerrit repositories](https://github.com/garaio/fisheye-gerrit-sync)

JavaDoc

* [RepositoryAdminService](http://docs.atlassian.com/fisheye-crucible/2.7.13/javadoc/com/atlassian/fisheye/spi/admin/services/RepositoryAdminService.html)
* [RepositoryIndexer](http://docs.atlassian.com/fisheye-crucible/2.7.13/javadoc/com/atlassian/fisheye/spi/admin/services/RepositoryIndexer.html)
* [All JavaDoc](http://docs.atlassian.com/fisheye-crucible/2.7.13/javadoc/)

Atlas Plugin SDK Documentation

* [Introduction](https://developer.atlassian.com/display/DOCS/Introduction+to+the+Atlassian+Plugin+SDK)
* [Maven Repositories](https://developer.atlassian.com/display/DOCS/Atlassian+Maven+Repositories)
* [Atlassian Template Renderer](https://developer.atlassian.com/display/ATR/Atlassian+Template+Renderer)
* [Shared Access Layer](https://developer.atlassian.com/display/SAL/Shared+Access+Layer+Documentation)

FishEye and Crucible Plugin Tutorials

* [Tutorials](https://developer.atlassian.com/display/FECRUDEV/FishEye+and+Crucible+Plugin+Tutorials)
