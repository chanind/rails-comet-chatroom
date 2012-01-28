jrubyc -t classes/ --javac -c '*' atmosphere/ChatHandler.rb;
javac -classpath '*' atmosphere/RailsAtmosphereServlet.java;
mv atmosphere/*.class classes/atmosphere/;
cd classes;
jar cf rails_atmosphere.jar atmosphere/*.class;
mv rails_atmosphere.jar ..;
cd ..;
