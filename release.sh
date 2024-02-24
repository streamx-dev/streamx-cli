./mvnw versions:set -DnewVersion=0.0.1-alpha2
./mvnw versions:commit
./mvnw clean install
./mvnw jreleaser:assemble jreleaser:full-release -Djreleaser.dry.run=false
