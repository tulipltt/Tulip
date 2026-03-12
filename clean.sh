./gradlew  --stop

rm -f -r .gradle
#m -f -r .idea
rm -f -r .kotlin

pushd ./tulip-runtime || exit
source clean.sh
popd || exit

pushd ./tulip-main || exit
source clean.sh
popd || exit

# Clean
mkdir -p ~/.m2
find ~/.m2 -name 'tulip-runtime*.jar' -print | sort
rm -f -r ~/.m2/repository/io/github/tulipltt/tulip-runtime

rm -f -r ~/.jbang
rm -f -r ~/.groovy
