mvn gpg:sign-and-deploy-file ^
  -Durl=https://central.sonatype.com/ ^
  -DrepositoryId=central ^
  -DpomFile=pom.xml ^
  -Dfile=pom.xml