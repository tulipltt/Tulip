mvn gpg:sign-and-deploy-file ^
  -Durl=https://central.sonatype.com/ ^
  -DrepositoryId=central ^
  -DpomFile=tulip.xml ^
  -Dfile=tulip.xml