trap 'kill $(jobs -p)' EXIT
java -classpath dep/*:src efruchter.tp.TraitProjectServer &
java -classpath dep/*:src -Djava.library.path=dep efruchter.tp.TraitProjectClient -l &
wait
