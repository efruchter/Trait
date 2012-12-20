JAVAC=javac
SRCDIR=src
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
runServer: build 
	java -classpath dep/*:src efruchter.tp.TraitProjectServer
