SBT plugin for TextTest
=======================

Use this plugin in your sbt project to run your texttests. For more information about texttest see http://texttest.org.
For more information about sbt see http://www.scala-sbt.org/.

There is a sample project that uses this plugin under src/it/texttest/myapp. Documentation is available in sbt:

    sbt> help texttest
    
For Developers
--------------

Set an environment variable $SOURCES_ROOT pointing at the folder above this one, so that the path in src/it/texttest/myapp/project/plugins.sbt will be resolved correctly.

Most of the test cases have a 'myapp' folder, which is a test project used to test this plugin. You can start sbt in any
of these 'myapp' folders. This will compile this plugin and load it into that project. There you can test the plugin interactively.

Running the self-tests
----------------------

1. Set up a texttest personal config file (see below). 
2. Install this test suite under $TEXTTEST_HOME:

    $> cd $TEXTTEST_HOME
    $> ln -s ${HOME}/workspace/sbt-texttest/src/it/texttest sbt-texttest

3. Run all the tests using the texttest console:

    $> texttest -a sbttt -con

Set up a personal config file
-----------------------------

Your texttest personal config file is kept on this path by default, create this file if it doesn't exist:

    ~/.texttest/config

In that file, you need a setting like this, pointing out the location of your clone of this project:

    [checkout_location]
    sbt-texttest:${HOME}/workspace/sbt-texttest
    [end]

There is more documentation on the personal config file on the texttest website: [http://texttest.sourceforge.net/index.php?page=documentation_3_27&n=personalpreffile](link)
