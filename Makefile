JAVAC=javac
SRCDIR=src
SERVERCLASS=efruchter/tp/TraitProjectServer.java
MAINCLASS=efruchter/tp/TraitProject.java
BASEPATH=$(dirname "$0")
CLASSPATH=$(echo dep/*.jar | tr ' ' ':')

all: clean build

build:
	set -v
	find $(BASEPATH) -name "*.java" | xargs javac -cp $(CLASSPATH)

clean:
	find $(SRCDIR) -type f -name "*.class" | xargs rm -f
