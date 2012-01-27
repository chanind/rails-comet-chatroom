== Rails Comet Chatroom

This is a demo application for using true comet (ajax push) within rails. This uses
JRuby with the atmosphere plugin to handle comet. This is a rails-based rewrite of the
atmosphere chat demo application which can be found here:
https://oss.sonatype.org/content/repositories/releases/org/atmosphere/samples/atmosphere-chat/0.8.2/

This works currently on jboss as7, and hasn't been tested on other web servers. There is a prebuilt war in the repo
which can be used to directly deploy on a java web server. Jboss AS7 also requires that you use the native apr connector for this to work. There's a guide 
to doing that here:
http://stackoverflow.com/questions/7342926/how-to-load-apr-connector-native-in-jboss-7

To build this project, just type "warble" in the base directory.
If you make changes to the "ChatHandler.rb" jruby file you need to build it into a java
class again. You can do this by moving to the "lib" directory and running the "build_jruby_handler.sh"
script. 

The next step is to create a gem which will let the atmosphere plugin be easily integrated into jruby on rails
projects for true comet and websocket support in rails!

More info about the atmosphere plugin can be found here:
https://github.com/Atmosphere/atmosphere
