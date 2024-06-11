#./mvnw versions:set -DnewVersion=0.3.0
#./mvnw versions:commit
./mvnw clean install
cd distribution
../mvnw jreleaser:assemble jreleaser:full-release -Djreleaser.dry.run=false -P preview
