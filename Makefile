JAVAC=javac
SRCDIR=src
MAINCLASS=efruchter/tp/TraitProject.java
SERVERCLASS=efruchter/tp/TraitProjectServer.java

.PHONY: all
all: clean build

.PHONY: build
build:
	$(SRCDIR)/build.sh

.PHONY: clean
clean:
	find $(SRCDIR) -type f -name "*.class" | xargs rm -f

.PHONY: runServer
runServer: all
	java -cp $(SRCDIR) efruchter.tp.TraitProjectServer
