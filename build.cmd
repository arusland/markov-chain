call mvn clean package -DskipTests
SET JARFILE=
FOR %%I in (%~dp0\target\markov-*-dependencies.jar) DO SET JARFILE=%%I
copy %JARFILE% .\dist\markov.jar /Y