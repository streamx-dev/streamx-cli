./mvnw versions:set -DnewVersion=0.1.1
./mvnw versions:commit
./mvnw clean install
./mvnw jreleaser:assemble jreleaser:full-release -Djreleaser.dry.run=false
