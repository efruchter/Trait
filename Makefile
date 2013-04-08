JAVAC=javac
SRCDIR=src
SERVERCLASS=efruchter/tp/TraitProjectServer.java
MAINCLASS=efruchter/tp/TraitProject.java


all: clean build

build:
	$(SRCDIR)/build 

clean:
	find $(SRCDIR) -type f -name "*.class" | xargs rm -f

jar: clean build myKeystore.txt
	jar cvf TraitProject.jar -C src/ .
	jarsigner -keystore myKeystore.txt TraitProject.jar myself
	mv TraitProject.jar website

myKeystore.txt:
	keytool -genkey -keystore myKeystore.txt -alias myself
	keytool -selfcert -alias myself -keystore myKeystore.txt