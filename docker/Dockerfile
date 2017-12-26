FROM java:8
VOLUME /tmp
ADD dataset_converter-0.0.1.jar app.jar
ADD classes/patterns.csv /patterns.csv
ADD classes/competitions.csv /competitions.csv
ADD classes/teams.csv /teams.csv
ADD classes/players.csv /players.csv
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-server","-Xms256M","-Xmx512M","-XX:MaxDirectMemorySize=256M","-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar", \
"--data.csv.patterns.filepath=/patterns.csv", \
"--data.csv.competitions.filepath=/competitions.csv", \
"--data.csv.teams.filepath=/teams.csv", \
"--data.csv.players.filepath=/players.csv"]
