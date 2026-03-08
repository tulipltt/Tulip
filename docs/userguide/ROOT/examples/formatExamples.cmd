set GJF_JAR=com.google.googlejavaformat:google-java-format:1.28.0

call jbang run %GJF_JAR% --aosp -r App.java
call jbang run FixJBangComments.java App.java

call jbang run %GJF_JAR% --aosp -r FixJBangComments.java
call jbang run FixJBangComments.java FixJBangComments.java
