clear
chmod +x gradlew
./gradlew jar
clear
./gradlew shadowJar
clear
java -jar build/libs/RecipieFinder-1-all.jar