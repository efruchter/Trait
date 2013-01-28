JAVAC=javac
SRCDIR=src
SERVERCLASS=efruchter/tp/TraitProjectServer.java
MAINCLASS=efruchter/tp/TraitProject.java


all: clean build

build:
	$(SRCDIR)/build 

clean:
	find $(SRCDIR) -type f -name "*.class" | xargs rm -f
