./mvnw versions:set -DnewVersion=0.0.1-alpha4
./mvnw versions:commit
./mvnw clean install
./mvnw jreleaser:assemble jreleaser:full-release -Djreleaser.dry.run=false
