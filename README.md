SBT plugin for TextTest
=======================

Use this plugin in your sbt project to run your texttests.

There is a sample project that uses this plugin under src/it/texttest/myapp. Documentation is available in sbt:

    sbt> help texttest
    
For Developers
--------------

Set an environment variable $SOURCES_ROOT pointing at the folder above this one, so that the path in src/it/texttest/myapp/project/plugins.sbt will be resolved correctly.

Start sbt in the 'myapp' folder. This will compile this plugin and load it into that project. Then you can install and run the sample tests there.
