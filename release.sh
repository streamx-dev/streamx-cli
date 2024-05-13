./mvnw versions:set -DnewVersion=0.2.5
./mvnw versions:commit
./mvnw clean install
./mvnw jreleaser:assemble jreleaser:full-release -Djreleaser.dry.run=false
