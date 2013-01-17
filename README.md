# Overview

This is a plugin for Fisheye/Crucible that provides an API for creating/indexing and deleting git repositories.

# Usage

To use it:

1. Build the jar (a built version is included in this git repo)
1. Add it to Fisheye/Crucible
1. Populate the git private key in the `Git Repo API Config` section in the admin interface.
1. Use the URLs below to interact with the API.

## Adding/indexing a git repository

This will add the repository if not present and then initiate an indexing run.  If the repository already exists, only the indexing will be started.

    $ curl https://fish.domain/plugins/servlet/git-repo-api/update?url=ssh://git.domain/repo.git&name=repo-name

## Deleting a git repository

This will delete the git repository.

    $ curl https://fish.domain/plugins/servlet/git-repo-api/delete?name=repo-name

## Return information

If the action is successful, a json response similar to the following will be returned:

    {
      "response": "create/update successful"
    }

If it fails, a json response similar to the following will be returned:

    {
      "error": "delete failed, repo refused to stop"
    }

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
